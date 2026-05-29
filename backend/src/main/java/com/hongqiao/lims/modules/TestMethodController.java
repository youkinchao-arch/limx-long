package com.hongqiao.lims.modules;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hongqiao.lims.common.AbstractCrudController;
import com.hongqiao.lims.security.PermissionChecker;
import java.util.List;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/methods")
public class TestMethodController extends AbstractCrudController<TestMethod> {

    public TestMethodController(TestMethodRepository repository, ObjectMapper objectMapper, PermissionChecker perm) {
        super(repository, TestMethod.class, "method",
                List.of("code", "name", "standard", "category"), objectMapper, perm);
    }
}
