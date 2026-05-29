package com.hongqiao.lims.modules;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hongqiao.lims.common.AbstractCrudController;
import com.hongqiao.lims.common.ApiException;
import com.hongqiao.lims.security.PermissionChecker;
import com.hongqiao.lims.security.SecurityUtils;
import com.hongqiao.lims.user.User;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlled document module: generic CRUD plus an ISO 17025 style approval workflow
 * (draft -> under_review -> approved/rejected -> obsolete) with append-only review history.
 */
@RestController
@RequestMapping("/api/v1/documents")
public class DocumentController extends AbstractCrudController<Document> {

    static final String DRAFT = "draft";
    static final String UNDER_REVIEW = "under_review";
    static final String APPROVED = "approved";
    static final String REJECTED = "rejected";
    static final String OBSOLETE = "obsolete";

    private final DocumentReviewRepository reviewRepository;

    public DocumentController(
            DocumentRepository repository,
            DocumentReviewRepository reviewRepository,
            ObjectMapper objectMapper,
            PermissionChecker perm) {
        super(repository, Document.class, "document",
                List.of("docNo", "title", "category", "status"), objectMapper, perm);
        this.reviewRepository = reviewRepository;
    }

    private void requireApprove() {
        if (!perm.has("document:approve")) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Missing permission: document:approve");
        }
    }

    @PostMapping("/{id}/submit")
    public Document submit(@PathVariable Long id, @RequestBody(required = false) DocumentActionRequest req) {
        requireWrite();
        Document doc = find(id);
        if (!DRAFT.equals(doc.getStatus()) && !REJECTED.equals(doc.getStatus())) {
            throw ApiException.badRequest(
                    "Only draft or rejected documents can be submitted for review / 仅草稿或退回的文件可提交审核");
        }
        return transition(doc, "SUBMIT", UNDER_REVIEW, comment(req), d -> {
            User actor = SecurityUtils.currentUser();
            d.setSubmittedBy(actor.getId());
            d.setSubmittedByName(actor.getUsername());
            d.setSubmittedAt(Instant.now());
            d.setApprovedBy(null);
            d.setApprovedByName(null);
            d.setApprovedAt(null);
        });
    }

    @PostMapping("/{id}/approve")
    public Document approve(@PathVariable Long id, @RequestBody(required = false) DocumentActionRequest req) {
        requireApprove();
        Document doc = find(id);
        if (!UNDER_REVIEW.equals(doc.getStatus())) {
            throw ApiException.badRequest(
                    "Only documents under review can be approved / 仅审核中的文件可批准");
        }
        return transition(doc, "APPROVE", APPROVED, comment(req), d -> {
            User actor = SecurityUtils.currentUser();
            d.setApprovedBy(actor.getId());
            d.setApprovedByName(actor.getUsername());
            d.setApprovedAt(Instant.now());
            LocalDate effective = req != null && req.effectiveDate() != null
                    ? req.effectiveDate() : LocalDate.now();
            d.setEffectiveDate(effective);
        });
    }

    @PostMapping("/{id}/reject")
    public Document reject(@PathVariable Long id, @RequestBody(required = false) DocumentActionRequest req) {
        requireApprove();
        Document doc = find(id);
        if (!UNDER_REVIEW.equals(doc.getStatus())) {
            throw ApiException.badRequest(
                    "Only documents under review can be rejected / 仅审核中的文件可退回");
        }
        return transition(doc, "REJECT", REJECTED, comment(req), null);
    }

    @PostMapping("/{id}/obsolete")
    public Document obsolete(@PathVariable Long id, @RequestBody(required = false) DocumentActionRequest req) {
        requireApprove();
        Document doc = find(id);
        if (!APPROVED.equals(doc.getStatus())) {
            throw ApiException.badRequest(
                    "Only approved documents can be retired / 仅生效文件可作废");
        }
        return transition(doc, "OBSOLETE", OBSOLETE, comment(req), null);
    }

    @GetMapping("/{id}/reviews")
    public List<DocumentReview> reviews(@PathVariable Long id) {
        requireRead();
        find(id);
        return reviewRepository.findByDocumentIdOrderByIdDesc(id);
    }

    private Document find(Long id) {
        return repository.findById(id).orElseThrow(ApiException::notFound);
    }

    private String comment(DocumentActionRequest req) {
        return req != null ? req.comment() : null;
    }

    private Document transition(
            Document doc, String action, String toStatus, String comment, java.util.function.Consumer<Document> mutator) {
        Object before = objectMapper.convertValue(doc, Map.class);
        String fromStatus = doc.getStatus();
        if (mutator != null) {
            mutator.accept(doc);
        }
        doc.setStatus(toStatus);
        Document saved = repository.save(doc);
        recordReview(doc.getId(), action, fromStatus, toStatus, comment);
        auditService.recordChange(action, "Document", before, saved);
        return saved;
    }

    private void recordReview(Long documentId, String action, String fromStatus, String toStatus, String comment) {
        User actor = SecurityUtils.currentUser();
        DocumentReview review = new DocumentReview();
        review.setDocumentId(documentId);
        review.setAction(action);
        review.setFromStatus(fromStatus);
        review.setToStatus(toStatus);
        review.setActorId(actor.getId());
        review.setActorUsername(actor.getUsername());
        review.setComment(comment);
        review.setCreatedAt(Instant.now());
        reviewRepository.save(review);
    }
}
