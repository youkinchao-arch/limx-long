package com.hongqiao.lims.modules;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hongqiao.lims.common.AbstractCrudController;
import com.hongqiao.lims.security.PermissionChecker;
import java.util.List;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/resources/bookings")
public class ResourceBookingController extends AbstractCrudController<ResourceBooking> {

    public ResourceBookingController(
            ResourceBookingRepository repository, ObjectMapper objectMapper, PermissionChecker perm) {
        super(repository, ResourceBooking.class, "resource",
                List.of("title", "resourceName", "resourceType", "status"), objectMapper, perm);
    }
}
