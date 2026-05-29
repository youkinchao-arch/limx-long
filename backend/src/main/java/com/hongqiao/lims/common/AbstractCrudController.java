package com.hongqiao.lims.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hongqiao.lims.security.PermissionChecker;
import jakarta.persistence.criteria.Predicate;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Generic list/get/create/update/delete controller mirroring the original Python CRUD factory.
 * Permission checks are programmatic so the permission prefix can stay an instance field.
 */
public abstract class AbstractCrudController<T> {

    protected final CrudRepository<T> repository;
    protected final Class<T> entityClass;
    protected final String permissionPrefix;
    protected final List<String> searchFields;
    protected final ObjectMapper objectMapper;
    protected final PermissionChecker perm;

    protected AbstractCrudController(
            CrudRepository<T> repository,
            Class<T> entityClass,
            String permissionPrefix,
            List<String> searchFields,
            ObjectMapper objectMapper,
            PermissionChecker perm) {
        this.repository = repository;
        this.entityClass = entityClass;
        this.permissionPrefix = permissionPrefix;
        this.searchFields = searchFields;
        this.objectMapper = objectMapper;
        this.perm = perm;
    }

    protected void requireRead() {
        if (!perm.has(permissionPrefix + ":read")) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Missing permission: " + permissionPrefix + ":read");
        }
    }

    protected void requireWrite() {
        if (!perm.has(permissionPrefix + ":write")) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Missing permission: " + permissionPrefix + ":write");
        }
    }

    @GetMapping
    public PageResponse<T> list(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "page_size", defaultValue = "20") int pageSize,
            @RequestParam(name = "q", required = false) String q) {
        requireRead();
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(pageSize, 1), 200);
        PageRequest pageable =
                PageRequest.of(safePage - 1, safeSize, Sort.by("id").descending());
        Page<T> result = repository.findAll(searchSpec(q), pageable);
        return PageResponse.of(result, safePage, safeSize);
    }

    @GetMapping("/{id}")
    public T get(@PathVariable Long id) {
        requireRead();
        return repository.findById(id).orElseThrow(ApiException::notFound);
    }

    @PostMapping
    public ResponseEntity<T> create(@RequestBody Map<String, Object> body) {
        requireWrite();
        T entity = objectMapper.convertValue(body, entityClass);
        return ResponseEntity.status(HttpStatus.CREATED).body(repository.save(entity));
    }

    @PutMapping("/{id}")
    public T update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        requireWrite();
        T existing = repository.findById(id).orElseThrow(ApiException::notFound);
        T incoming = objectMapper.convertValue(body, entityClass);
        BeanCopyUtils.copyNonNull(incoming, existing);
        return repository.save(existing);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        requireWrite();
        T existing = repository.findById(id).orElseThrow(ApiException::notFound);
        repository.delete(existing);
        return ResponseEntity.noContent().build();
    }

    protected Specification<T> searchSpec(String q) {
        if (q == null || q.isBlank() || searchFields.isEmpty()) {
            return null;
        }
        String like = "%" + q.toLowerCase() + "%";
        return (root, query, cb) -> {
            List<Predicate> predicates = searchFields.stream()
                    .map(field -> cb.like(cb.lower(root.get(field).as(String.class)), like))
                    .toList();
            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }
}
