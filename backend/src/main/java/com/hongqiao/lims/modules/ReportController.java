package com.hongqiao.lims.modules;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hongqiao.lims.common.AbstractCrudController;
import com.hongqiao.lims.common.ApiException;
import com.hongqiao.lims.security.PermissionChecker;
import com.hongqiao.lims.security.SecurityUtils;
import com.hongqiao.lims.user.User;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Report module: generic CRUD plus an ISO 17025 / 21 CFR Part 11 style workflow
 * (draft -> under_review -> approved -> signed -> issued, with a rejected branch).
 * Signing requires password re-authentication and produces an immutable signature hash.
 */
@RestController
@RequestMapping("/api/v1/reports")
public class ReportController extends AbstractCrudController<Report> {

    static final String DRAFT = "draft";
    static final String UNDER_REVIEW = "under_review";
    static final String APPROVED = "approved";
    static final String REJECTED = "rejected";
    static final String SIGNED = "signed";
    static final String ISSUED = "issued";

    private final ReportSignatureRepository signatureRepository;
    private final ReportPdfService pdfService;
    private final PasswordEncoder passwordEncoder;

    public ReportController(
            ReportRepository repository,
            ReportSignatureRepository signatureRepository,
            ReportPdfService pdfService,
            PasswordEncoder passwordEncoder,
            ObjectMapper objectMapper,
            PermissionChecker perm) {
        super(repository, Report.class, "report",
                List.of("reportNo", "title", "customer", "status"), objectMapper, perm);
        this.signatureRepository = signatureRepository;
        this.pdfService = pdfService;
        this.passwordEncoder = passwordEncoder;
    }

    private void requireApprove() {
        if (!perm.has("report:approve")) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Missing permission: report:approve");
        }
    }

    private void requireSign() {
        if (!perm.has("report:sign")) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Missing permission: report:sign");
        }
    }

    @PostMapping("/{id}/submit")
    public Report submit(@PathVariable Long id, @RequestBody(required = false) ReportActionRequest req) {
        requireWrite();
        Report report = find(id);
        if (!DRAFT.equals(report.getStatus()) && !REJECTED.equals(report.getStatus())) {
            throw ApiException.badRequest(
                    "Only draft or rejected reports can be submitted for review / 仅草稿或退回的报告可提交审核");
        }
        return transition(report, "SUBMIT", UNDER_REVIEW, comment(req), null, null, r -> {
            User actor = SecurityUtils.currentUser();
            r.setSubmittedBy(actor.getId());
            r.setSubmittedByName(actor.getUsername());
            r.setSubmittedAt(Instant.now());
            r.setApprovedBy(null);
            r.setApprovedByName(null);
            r.setApprovedAt(null);
        });
    }

    @PostMapping("/{id}/approve")
    public Report approve(@PathVariable Long id, @RequestBody(required = false) ReportActionRequest req) {
        requireApprove();
        Report report = find(id);
        if (!UNDER_REVIEW.equals(report.getStatus())) {
            throw ApiException.badRequest("Only reports under review can be approved / 仅审核中的报告可批准");
        }
        return transition(report, "APPROVE", APPROVED, comment(req), null, null, r -> {
            User actor = SecurityUtils.currentUser();
            r.setApprovedBy(actor.getId());
            r.setApprovedByName(actor.getUsername());
            r.setApprovedAt(Instant.now());
        });
    }

    @PostMapping("/{id}/reject")
    public Report reject(@PathVariable Long id, @RequestBody(required = false) ReportActionRequest req) {
        requireApprove();
        Report report = find(id);
        if (!UNDER_REVIEW.equals(report.getStatus())) {
            throw ApiException.badRequest("Only reports under review can be rejected / 仅审核中的报告可退回");
        }
        return transition(report, "REJECT", REJECTED, comment(req), null, null, null);
    }

    @PostMapping("/{id}/sign")
    public Report sign(@PathVariable Long id, @RequestBody(required = false) ReportActionRequest req) {
        requireSign();
        Report report = find(id);
        if (!APPROVED.equals(report.getStatus())) {
            throw ApiException.badRequest("Only approved reports can be signed / 仅已批准的报告可签名");
        }
        User actor = SecurityUtils.currentUser();
        String password = req != null ? req.password() : null;
        if (password == null || password.isBlank()
                || !passwordEncoder.matches(password, actor.getHashedPassword())) {
            throw ApiException.badRequest("Signature password is incorrect / 签名密码不正确");
        }
        String meaning = req != null && req.meaning() != null && !req.meaning().isBlank()
                ? req.meaning() : "Approved and signed / 批准并签发";
        Instant now = Instant.now();
        String hash = signatureHash(report.getReportNo(), actor.getUsername(), meaning, now);
        return transition(report, "SIGN", SIGNED, comment(req), meaning, hash, r -> {
            r.setSignedBy(actor.getId());
            r.setSignedByName(actor.getUsername());
            r.setSignedAt(now);
            r.setSignatureHash(hash);
        });
    }

    @PostMapping("/{id}/issue")
    public Report issue(@PathVariable Long id, @RequestBody(required = false) ReportActionRequest req) {
        requireApprove();
        Report report = find(id);
        if (!SIGNED.equals(report.getStatus())) {
            throw ApiException.badRequest("Only signed reports can be issued / 仅已签名的报告可签发");
        }
        return transition(report, "ISSUE", ISSUED, comment(req), null, null, r -> {
            User actor = SecurityUtils.currentUser();
            r.setIssuedBy(actor.getId());
            r.setIssuedByName(actor.getUsername());
            r.setIssuedAt(Instant.now());
            LocalDate issueDate = req != null && req.issueDate() != null ? req.issueDate() : LocalDate.now();
            r.setIssueDate(issueDate);
        });
    }

    @GetMapping("/{id}/signatures")
    public List<ReportSignature> signatures(@PathVariable Long id) {
        requireRead();
        find(id);
        return signatureRepository.findByReportIdOrderByIdDesc(id);
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> pdf(@PathVariable Long id) {
        requireRead();
        Report report = find(id);
        List<ReportSignature> sigs = signatureRepository.findByReportIdOrderByIdDesc(id);
        byte[] body = pdfService.render(report, sigs);
        String filename = "report-" + report.getReportNo() + ".pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(body);
    }

    private Report find(Long id) {
        return repository.findById(id).orElseThrow(ApiException::notFound);
    }

    private String comment(ReportActionRequest req) {
        return req != null ? req.comment() : null;
    }

    private Report transition(
            Report report,
            String action,
            String toStatus,
            String comment,
            String meaning,
            String signatureHash,
            java.util.function.Consumer<Report> mutator) {
        Object before = objectMapper.convertValue(report, Map.class);
        String fromStatus = report.getStatus();
        if (mutator != null) {
            mutator.accept(report);
        }
        report.setStatus(toStatus);
        Report saved = repository.save(report);
        recordSignature(report.getId(), action, fromStatus, toStatus, comment, meaning, signatureHash);
        auditService.recordChange(action, "Report", before, saved);
        return saved;
    }

    private void recordSignature(
            Long reportId,
            String action,
            String fromStatus,
            String toStatus,
            String comment,
            String meaning,
            String signatureHash) {
        User actor = SecurityUtils.currentUser();
        ReportSignature sig = new ReportSignature();
        sig.setReportId(reportId);
        sig.setAction(action);
        sig.setFromStatus(fromStatus);
        sig.setToStatus(toStatus);
        sig.setActorId(actor.getId());
        sig.setActorUsername(actor.getUsername());
        sig.setComment(comment);
        sig.setMeaning(meaning);
        sig.setSignatureHash(signatureHash);
        sig.setCreatedAt(Instant.now());
        signatureRepository.save(sig);
    }

    private static String signatureHash(String reportNo, String username, String meaning, Instant when) {
        try {
            String payload = reportNo + "|" + username + "|" + meaning + "|" + when.toEpochMilli();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] out = digest.digest(payload.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(out);
        } catch (Exception e) {
            throw ApiException.badRequest("Failed to compute signature hash / 计算签名摘要失败");
        }
    }
}
