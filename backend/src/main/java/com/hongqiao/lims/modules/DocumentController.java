package com.hongqiao.lims.modules;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hongqiao.lims.common.AbstractCrudController;
import com.hongqiao.lims.security.PermissionChecker;
import java.util.List;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/documents")
public class DocumentController extends AbstractCrudController<Document> {

    public DocumentController(DocumentRepository repository, ObjectMapper objectMapper, PermissionChecker perm) {
        super(repository, Document.class, "document",
                List.of("docNo", "title", "category", "status"), objectMapper, perm);
    }
}
