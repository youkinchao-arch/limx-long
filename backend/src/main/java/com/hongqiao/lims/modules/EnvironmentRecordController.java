package com.hongqiao.lims.modules;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hongqiao.lims.common.AbstractCrudController;
import com.hongqiao.lims.security.PermissionChecker;
import java.util.List;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/environment/records")
public class EnvironmentRecordController extends AbstractCrudController<EnvironmentRecord> {

    public EnvironmentRecordController(
            EnvironmentRecordRepository repository, ObjectMapper objectMapper, PermissionChecker perm) {
        super(repository, EnvironmentRecord.class, "environment",
                List.of("location", "status"), objectMapper, perm);
    }
}
