package com.hongqiao.lims.modules;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportSignatureRepository extends JpaRepository<ReportSignature, Long> {

    List<ReportSignature> findByReportIdOrderByIdDesc(Long reportId);
}
