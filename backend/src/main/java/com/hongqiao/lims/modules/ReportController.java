package com.hongqiao.lims.modules;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hongqiao.lims.common.AbstractCrudController;
import com.hongqiao.lims.security.PermissionChecker;
import java.util.List;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController extends AbstractCrudController<Report> {

    public ReportController(ReportRepository repository, ObjectMapper objectMapper, PermissionChecker perm) {
        super(repository, Report.class, "report",
                List.of("reportNo", "title", "customer"), objectMapper, perm);
    }
}
