package com.hongqiao.lims.equipment;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EquipmentRecordRepository extends JpaRepository<EquipmentRecord, Long> {

    List<EquipmentRecord> findByEquipmentIdOrderByIdDesc(Long equipmentId);
}
