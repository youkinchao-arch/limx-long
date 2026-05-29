package com.hongqiao.lims.dashboard;

import com.hongqiao.lims.equipment.Equipment;
import com.hongqiao.lims.equipment.EquipmentRepository;
import com.hongqiao.lims.modules.Document;
import com.hongqiao.lims.modules.DocumentRepository;
import com.hongqiao.lims.modules.EnvironmentRecord;
import com.hongqiao.lims.modules.EnvironmentRecordRepository;
import com.hongqiao.lims.modules.Material;
import com.hongqiao.lims.modules.MaterialRepository;
import com.hongqiao.lims.modules.Report;
import com.hongqiao.lims.modules.ReportRepository;
import com.hongqiao.lims.personnel.Personnel;
import com.hongqiao.lims.personnel.PersonnelRepository;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final PersonnelRepository personnelRepository;
    private final EquipmentRepository equipmentRepository;
    private final MaterialRepository materialRepository;
    private final DocumentRepository documentRepository;
    private final ReportRepository reportRepository;
    private final EnvironmentRecordRepository environmentRepository;

    public DashboardController(
            PersonnelRepository personnelRepository,
            EquipmentRepository equipmentRepository,
            MaterialRepository materialRepository,
            DocumentRepository documentRepository,
            ReportRepository reportRepository,
            EnvironmentRecordRepository environmentRepository) {
        this.personnelRepository = personnelRepository;
        this.equipmentRepository = equipmentRepository;
        this.materialRepository = materialRepository;
        this.documentRepository = documentRepository;
        this.reportRepository = reportRepository;
        this.environmentRepository = environmentRepository;
    }

    @GetMapping("/summary")
    public Map<String, Object> summary(
            @RequestParam(name = "reminder_days", defaultValue = "30") int reminderDays) {
        LocalDate limit = LocalDate.now().plusDays(reminderDays);

        Map<String, Object> totals = new LinkedHashMap<>();
        totals.put("personnel", personnelRepository.count());
        totals.put("equipment", equipmentRepository.count());
        totals.put("material", materialRepository.count());
        totals.put("document", documentRepository.count());
        totals.put("report", reportRepository.count());

        Specification<Personnel> qualExpiring = (root, q, cb) -> cb.and(
                cb.isNotNull(root.get("qualificationExpiry")),
                cb.lessThanOrEqualTo(root.get("qualificationExpiry"), limit));
        Specification<Equipment> calDue = (root, q, cb) -> cb.and(
                cb.isNotNull(root.get("calibrationDue")),
                cb.lessThanOrEqualTo(root.get("calibrationDue"), limit));
        Specification<Material> matExpiring = (root, q, cb) -> cb.and(
                cb.isNotNull(root.get("expiryDate")),
                cb.lessThanOrEqualTo(root.get("expiryDate"), limit));
        Specification<EnvironmentRecord> envAlarm =
                (root, q, cb) -> cb.equal(root.get("status"), "alarm");

        Map<String, Object> reminders = new LinkedHashMap<>();
        reminders.put("window_days", reminderDays);
        reminders.put("qualification_expiring", personnelRepository.count(qualExpiring));
        reminders.put("calibration_due", equipmentRepository.count(calDue));
        reminders.put("material_expiring", materialRepository.count(matExpiring));
        reminders.put("environment_alarms", environmentRepository.count(envAlarm));

        Map<String, Object> equipmentStatus = new LinkedHashMap<>();
        for (Object[] row : equipmentRepository.countByStatus()) {
            equipmentStatus.put((String) row[0], row[1]);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totals", totals);
        result.put("reminders", reminders);
        result.put("equipment_status", equipmentStatus);
        return result;
    }
}
