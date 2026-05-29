package com.hongqiao.lims.modules;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentReviewRepository extends JpaRepository<DocumentReview, Long> {

    List<DocumentReview> findByDocumentIdOrderByIdDesc(Long documentId);
}
