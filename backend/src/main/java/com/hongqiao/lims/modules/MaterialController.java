package com.hongqiao.lims.modules;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hongqiao.lims.common.AbstractCrudController;
import com.hongqiao.lims.security.PermissionChecker;
import java.util.List;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/warehouse/materials")
public class MaterialController extends AbstractCrudController<Material> {

    public MaterialController(MaterialRepository repository, ObjectMapper objectMapper, PermissionChecker perm) {
        super(repository, Material.class, "warehouse",
                List.of("code", "name", "category", "warehouse"), objectMapper, perm);
    }
}
