package com.hongqiao.lims.personnel;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainingRecordRepository extends JpaRepository<TrainingRecord, Long> {

    List<TrainingRecord> findByPersonnelIdOrderByIdDesc(Long personnelId);
}
