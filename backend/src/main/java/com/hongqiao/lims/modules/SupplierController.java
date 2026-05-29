package com.hongqiao.lims.modules;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hongqiao.lims.common.AbstractCrudController;
import com.hongqiao.lims.security.PermissionChecker;
import java.util.List;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/warehouse/suppliers")
public class SupplierController extends AbstractCrudController<Supplier> {

    public SupplierController(SupplierRepository repository, ObjectMapper objectMapper, PermissionChecker perm) {
        super(repository, Supplier.class, "warehouse", List.of("name", "contact", "phone"), objectMapper, perm);
    }
}
