package com.hongqiao.lims.config;

import com.hongqiao.lims.user.Role;
import com.hongqiao.lims.user.RoleRepository;
import com.hongqiao.lims.user.User;
import com.hongqiao.lims.user.UserRepository;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Seeds the permission roles and the bootstrap super admin on startup (idempotent). */
@Component
public class DataInitializer implements ApplicationRunner {

    private static final List<String> MODULES = List.of(
            "user", "personnel", "equipment", "warehouse",
            "document", "environment", "method", "report", "resource");

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final LimsProperties props;

    public DataInitializer(
            RoleRepository roleRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            LimsProperties props) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.props = props;
    }

    private static String allPermissions() {
        return MODULES.stream()
                .flatMap(m -> List.of(m + ":read", m + ":write").stream())
                .collect(Collectors.joining(","));
    }

    private static String readonlyPermissions() {
        return MODULES.stream().map(m -> m + ":read").collect(Collectors.joining(","));
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Role admin = upsertRole("admin", "系统管理员 / Administrator", "*");
        upsertRole("manager", "实验室管理员 / Lab Manager", allPermissions());
        upsertRole("operator", "实验员 / Operator", readonlyPermissions());

        if (userRepository.findByUsername(props.getAdmin().getUsername()).isEmpty()) {
            User user = new User();
            user.setUsername(props.getAdmin().getUsername());
            user.setFullName(props.getAdmin().getName());
            user.setHashedPassword(passwordEncoder.encode(props.getAdmin().getPassword()));
            user.setSuperuser(true);
            user.setActive(true);
            user.setRoles(new LinkedHashSet<>(List.of(admin)));
            userRepository.save(user);
        }
    }

    private Role upsertRole(String code, String name, String permissions) {
        Role role = roleRepository.findByCode(code).orElseGet(Role::new);
        role.setCode(code);
        role.setName(name);
        role.setPermissions(permissions);
        return roleRepository.save(role);
    }
}
