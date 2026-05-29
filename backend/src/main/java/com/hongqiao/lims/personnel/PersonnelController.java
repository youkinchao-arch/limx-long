package com.hongqiao.lims.personnel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hongqiao.lims.common.ApiException;
import com.hongqiao.lims.common.BeanCopyUtils;
import com.hongqiao.lims.common.PageResponse;
import com.hongqiao.lims.security.PermissionChecker;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/personnel")
public class PersonnelController {

    private final PersonnelRepository repository;
    private final DepartmentRepository departmentRepository;
    private final TrainingRecordRepository trainingRepository;
    private final PermissionChecker perm;
    private final ObjectMapper objectMapper;

    public PersonnelController(
            PersonnelRepository repository,
            DepartmentRepository departmentRepository,
            TrainingRecordRepository trainingRepository,
            PermissionChecker perm,
            ObjectMapper objectMapper) {
        this.repository = repository;
        this.departmentRepository = departmentRepository;
        this.trainingRepository = trainingRepository;
        this.perm = perm;
        this.objectMapper = objectMapper;
    }

    private void requireRead() {
        if (!perm.has("personnel:read")) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Missing permission: personnel:read");
        }
    }

    private void requireWrite() {
        if (!perm.has("personnel:write")) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Missing permission: personnel:write");
        }
    }

    // ---- Departments (literal path resolves before /{id}) ----

    @GetMapping("/departments")
    public List<Department> listDepartments() {
        requireRead();
        return departmentRepository.findAllByOrderByIdAsc();
    }

    @PostMapping("/departments")
    public ResponseEntity<Department> createDepartment(@RequestBody Department body) {
        requireWrite();
        body.setId(null);
        return ResponseEntity.status(HttpStatus.CREATED).body(departmentRepository.save(body));
    }

    @DeleteMapping("/departments/{id}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable Long id) {
        requireWrite();
        Department dep = departmentRepository.findById(id).orElseThrow(ApiException::notFound);
        departmentRepository.delete(dep);
        return ResponseEntity.noContent().build();
    }

    // ---- Training records ----

    @GetMapping("/{pid}/trainings")
    public List<TrainingRecord> listTrainings(@PathVariable Long pid) {
        requireRead();
        return trainingRepository.findByPersonnelIdOrderByIdDesc(pid);
    }

    @PostMapping("/{pid}/trainings")
    public ResponseEntity<TrainingRecord> createTraining(
            @PathVariable Long pid, @RequestBody Map<String, Object> body) {
        requireWrite();
        repository.findById(pid).orElseThrow(ApiException::notFound);
        TrainingRecord record = objectMapper.convertValue(body, TrainingRecord.class);
        record.setId(null);
        record.setPersonnelId(pid);
        return ResponseEntity.status(HttpStatus.CREATED).body(trainingRepository.save(record));
    }

    // ---- Personnel CRUD ----

    @GetMapping
    public PageResponse<Personnel> list(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "page_size", defaultValue = "20") int pageSize,
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "department_id", required = false) Long departmentId,
            @RequestParam(name = "expiring_days", required = false) Integer expiringDays) {
        requireRead();
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(pageSize, 1), 200);
        Specification<Personnel> spec = (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            if (q != null && !q.isBlank()) {
                String like = "%" + q.toLowerCase() + "%";
                ps.add(cb.or(
                        cb.like(cb.lower(root.get("name")), like),
                        cb.like(cb.lower(root.get("employeeNo")), like),
                        cb.like(cb.lower(root.get("position").as(String.class)), like)));
            }
            if (departmentId != null) {
                ps.add(cb.equal(root.get("departmentId"), departmentId));
            }
            if (expiringDays != null) {
                LocalDate limit = LocalDate.now().plusDays(expiringDays);
                ps.add(cb.isNotNull(root.get("qualificationExpiry")));
                ps.add(cb.lessThanOrEqualTo(root.get("qualificationExpiry"), limit));
            }
            return cb.and(ps.toArray(new Predicate[0]));
        };
        PageRequest pageable = PageRequest.of(safePage - 1, safeSize, Sort.by("id").descending());
        Page<Personnel> result = repository.findAll(spec, pageable);
        return PageResponse.of(result, safePage, safeSize);
    }

    @GetMapping("/{id}")
    public Personnel get(@PathVariable Long id) {
        requireRead();
        return repository.findById(id).orElseThrow(ApiException::notFound);
    }

    @PostMapping
    public ResponseEntity<Personnel> create(@RequestBody Personnel body) {
        requireWrite();
        body.setId(null);
        if (repository.existsByEmployeeNo(body.getEmployeeNo())) {
            throw ApiException.badRequest("工号已存在 / Employee no exists");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(repository.save(body));
    }

    @PutMapping("/{id}")
    public Personnel update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        requireWrite();
        Personnel existing = repository.findById(id).orElseThrow(ApiException::notFound);
        Personnel incoming = objectMapper.convertValue(body, Personnel.class);
        BeanCopyUtils.copyNonNull(incoming, existing);
        return repository.save(existing);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        requireWrite();
        Personnel existing = repository.findById(id).orElseThrow(ApiException::notFound);
        repository.delete(existing);
        return ResponseEntity.noContent().build();
    }
}
