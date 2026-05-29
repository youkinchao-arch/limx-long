package com.hongqiao.lims.equipment;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hongqiao.lims.audit.AuditService;
import com.hongqiao.lims.common.ApiException;
import com.hongqiao.lims.common.BeanCopyUtils;
import com.hongqiao.lims.common.PageResponse;
import com.hongqiao.lims.security.PermissionChecker;
import com.hongqiao.lims.security.SecurityUtils;
import com.hongqiao.lims.user.User;
import jakarta.persistence.criteria.Predicate;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
@RequestMapping("/api/v1/equipment")
public class EquipmentController {

    static final String CALIBRATION = "calibration";
    static final String MAINTENANCE = "maintenance";

    private final EquipmentRepository repository;
    private final EquipmentRecordRepository recordRepository;
    private final PermissionChecker perm;
    private final ObjectMapper objectMapper;
    private final AuditService auditService;
    private final PasswordEncoder passwordEncoder;

    public EquipmentController(
            EquipmentRepository repository,
            EquipmentRecordRepository recordRepository,
            PermissionChecker perm,
            ObjectMapper objectMapper,
            AuditService auditService,
            PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.recordRepository = recordRepository;
        this.perm = perm;
        this.objectMapper = objectMapper;
        this.auditService = auditService;
        this.passwordEncoder = passwordEncoder;
    }

    private void requireRead() {
        if (!perm.has("equipment:read")) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Missing permission: equipment:read");
        }
    }

    private void requireWrite() {
        if (!perm.has("equipment:write")) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Missing permission: equipment:write");
        }
    }

    @GetMapping
    public PageResponse<Equipment> list(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "page_size", defaultValue = "20") int pageSize,
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "calibration_due_days", required = false) Integer calibrationDueDays,
            @RequestParam(name = "maintenance_due_days", required = false) Integer maintenanceDueDays) {
        requireRead();
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(pageSize, 1), 200);
        Specification<Equipment> spec = (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            if (q != null && !q.isBlank()) {
                String like = "%" + q.toLowerCase() + "%";
                ps.add(cb.or(
                        cb.like(cb.lower(root.get("name")), like),
                        cb.like(cb.lower(root.get("assetNo")), like),
                        cb.like(cb.lower(root.get("model").as(String.class)), like)));
            }
            if (status != null && !status.isBlank()) {
                ps.add(cb.equal(root.get("status"), status));
            }
            if (calibrationDueDays != null) {
                LocalDate limit = LocalDate.now().plusDays(calibrationDueDays);
                ps.add(cb.isNotNull(root.get("calibrationDue")));
                ps.add(cb.lessThanOrEqualTo(root.get("calibrationDue"), limit));
            }
            if (maintenanceDueDays != null) {
                LocalDate limit = LocalDate.now().plusDays(maintenanceDueDays);
                ps.add(cb.isNotNull(root.get("maintenanceDue")));
                ps.add(cb.lessThanOrEqualTo(root.get("maintenanceDue"), limit));
            }
            return cb.and(ps.toArray(new Predicate[0]));
        };
        PageRequest pageable = PageRequest.of(safePage - 1, safeSize, Sort.by("id").descending());
        Page<Equipment> result = repository.findAll(spec, pageable);
        return PageResponse.of(result, safePage, safeSize);
    }

    @GetMapping("/{id}")
    public Equipment get(@PathVariable Long id) {
        requireRead();
        return repository.findById(id).orElseThrow(ApiException::notFound);
    }

    @PostMapping
    public ResponseEntity<Equipment> create(@RequestBody Equipment body) {
        requireWrite();
        body.setId(null);
        if (repository.existsByAssetNo(body.getAssetNo())) {
            throw ApiException.badRequest("资产编号已存在 / Asset no exists");
        }
        Equipment saved = repository.save(body);
        auditService.recordChange("CREATE", "Equipment", null, saved);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public Equipment update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        requireWrite();
        Equipment existing = repository.findById(id).orElseThrow(ApiException::notFound);
        Object before = objectMapper.convertValue(existing, Map.class);
        Equipment incoming = objectMapper.convertValue(body, Equipment.class);
        BeanCopyUtils.copyNonNull(incoming, existing);
        Equipment saved = repository.save(existing);
        auditService.recordChange("UPDATE", "Equipment", before, saved);
        return saved;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        requireWrite();
        Equipment existing = repository.findById(id).orElseThrow(ApiException::notFound);
        Object before = objectMapper.convertValue(existing, Map.class);
        repository.delete(existing);
        auditService.recordChange("DELETE", "Equipment", before, null);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/qrcode")
    public ResponseEntity<byte[]> qrcode(@PathVariable Long id) {
        requireRead();
        Equipment equipment = repository.findById(id).orElseThrow(ApiException::notFound);
        byte[] png = generateQr("LIMS-EQUIP:" + equipment.getAssetNo());
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(png);
    }

    @GetMapping("/{id}/records")
    public List<EquipmentRecord> records(@PathVariable Long id) {
        requireRead();
        repository.findById(id).orElseThrow(ApiException::notFound);
        return recordRepository.findByEquipmentIdOrderByIdDesc(id);
    }

    @PostMapping("/{id}/records")
    public ResponseEntity<EquipmentRecord> addRecord(
            @PathVariable Long id, @RequestBody EquipmentRecordRequest req) {
        Equipment equipment = repository.findById(id).orElseThrow(ApiException::notFound);
        String type = req != null ? req.recordType() : null;
        boolean calibration = CALIBRATION.equals(type);
        boolean maintenance = MAINTENANCE.equals(type);
        if (!calibration && !maintenance) {
            throw ApiException.badRequest(
                    "record_type must be calibration or maintenance / 记录类型必须为校准或维护");
        }
        if (calibration && !perm.has("equipment:calibrate")) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Missing permission: equipment:calibrate");
        }
        if (maintenance && !perm.has("equipment:maintain")) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Missing permission: equipment:maintain");
        }

        User actor = SecurityUtils.currentUser();
        String password = req.password();
        if (password == null || password.isBlank()
                || !passwordEncoder.matches(password, actor.getHashedPassword())) {
            throw ApiException.badRequest("Signature password is incorrect / 签名密码不正确");
        }

        LocalDate performed = req.performedDate() != null ? req.performedDate() : LocalDate.now();
        LocalDate nextDue = req.nextDueDate();
        if (nextDue == null && req.cycleDays() != null) {
            nextDue = performed.plusDays(req.cycleDays());
        }
        Instant now = Instant.now();
        String hash = signatureHash(equipment.getAssetNo(), actor.getUsername(), type, now);

        Object before = objectMapper.convertValue(equipment, Map.class);
        EquipmentRecord record = new EquipmentRecord();
        record.setEquipmentId(id);
        record.setRecordType(type);
        record.setResult(req.result());
        record.setPerformedDate(performed);
        record.setPerformedBy(actor.getId());
        record.setPerformedByName(actor.getUsername());
        record.setProvider(req.provider());
        record.setCertificateNo(req.certificateNo());
        record.setNextDueDate(nextDue);
        record.setCycleDays(req.cycleDays());
        record.setNotes(req.notes());
        record.setSignatureHash(hash);
        record.setCreatedAt(now);
        EquipmentRecord saved = recordRepository.save(record);

        if (calibration) {
            equipment.setCalibrationDate(performed);
            equipment.setCalibrationDue(nextDue);
            if (req.cycleDays() != null) {
                equipment.setCalibrationCycleDays(req.cycleDays());
            }
        } else {
            equipment.setMaintenanceDate(performed);
            equipment.setMaintenanceDue(nextDue);
            if (req.cycleDays() != null) {
                equipment.setMaintenanceCycleDays(req.cycleDays());
            }
        }
        Equipment updated = repository.save(equipment);
        auditService.recordChange(calibration ? "CALIBRATE" : "MAINTAIN", "Equipment", before, updated);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    private static String signatureHash(String assetNo, String username, String type, Instant when) {
        try {
            String payload = assetNo + "|" + username + "|" + type + "|" + when.toEpochMilli();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] out = digest.digest(payload.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(out);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Hash generation failed");
        }
    }

    private byte[] generateQr(String payload) {
        try {
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.MARGIN, 2);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            BitMatrix matrix =
                    new QRCodeWriter().encode(payload, BarcodeFormat.QR_CODE, 240, 240, hints);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", out);
            return out.toByteArray();
        } catch (com.google.zxing.WriterException | IOException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "QR generation failed");
        }
    }
}
