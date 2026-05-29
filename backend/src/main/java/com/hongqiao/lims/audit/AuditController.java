package com.hongqiao.lims.audit;

import com.hongqiao.lims.common.PageResponse;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/audit")
public class AuditController {

    private final AuditLogRepository repository;

    public AuditController(AuditLogRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/logs")
    @PreAuthorize("@perm.has('audit:read')")
    public PageResponse<AuditLog> list(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "page_size", defaultValue = "20") int pageSize,
            @RequestParam(name = "action", required = false) String action,
            @RequestParam(name = "entity_type", required = false) String entityType,
            @RequestParam(name = "q", required = false) String q) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(pageSize, 1), 200);
        Specification<AuditLog> spec = (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            if (action != null && !action.isBlank()) {
                ps.add(cb.equal(root.get("action"), action));
            }
            if (entityType != null && !entityType.isBlank()) {
                ps.add(cb.equal(root.get("entityType"), entityType));
            }
            if (q != null && !q.isBlank()) {
                String like = "%" + q.toLowerCase() + "%";
                ps.add(cb.or(
                        cb.like(cb.lower(root.get("actorUsername")), like),
                        cb.like(cb.lower(root.get("entityId")), like)));
            }
            return cb.and(ps.toArray(new Predicate[0]));
        };
        PageRequest pageable = PageRequest.of(safePage - 1, safeSize, Sort.by("id").descending());
        Page<AuditLog> result = repository.findAll(spec, pageable);
        return PageResponse.of(result, safePage, safeSize);
    }
}
