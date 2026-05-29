package com.hongqiao.lims.user;

import com.hongqiao.lims.common.ApiException;
import com.hongqiao.lims.security.JwtService;
import com.hongqiao.lims.security.SecurityUtils;
import com.hongqiao.lims.user.dto.RoleDto;
import com.hongqiao.lims.user.dto.TokenResponse;
import com.hongqiao.lims.user.dto.UserCreateRequest;
import com.hongqiao.lims.user.dto.UserDto;
import com.hongqiao.lims.user.dto.UserUpdateRequest;
import jakarta.validation.Valid;
import java.util.LinkedHashSet;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public TokenResponse login(
            @RequestParam("username") String username,
            @RequestParam("password") String password) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null || !passwordEncoder.matches(password, user.getHashedPassword())) {
            throw new ApiException(
                    HttpStatus.UNAUTHORIZED, "用户名或密码错误 / Invalid username or password");
        }
        if (!user.isActive()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "账号已停用 / Inactive account");
        }
        return TokenResponse.bearer(jwtService.generateToken(user.getId()));
    }

    @GetMapping("/me")
    public UserDto me() {
        return UserDto.from(SecurityUtils.currentUser());
    }

    @GetMapping("/roles")
    public List<RoleDto> roles() {
        return roleRepository.findAllByOrderByIdAsc().stream().map(RoleDto::from).toList();
    }

    @GetMapping("/users")
    @PreAuthorize("@perm.has('user:read')")
    public List<UserDto> listUsers() {
        return userRepository.findAllByOrderByIdAsc().stream().map(UserDto::from).toList();
    }

    @PostMapping("/users")
    @PreAuthorize("@perm.has('user:write')")
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserCreateRequest req) {
        if (userRepository.existsByUsername(req.username())) {
            throw ApiException.badRequest("用户名已存在 / Username already exists");
        }
        User user = new User();
        user.setUsername(req.username());
        user.setFullName(req.fullName());
        user.setEmail(req.email());
        user.setHashedPassword(passwordEncoder.encode(req.password()));
        user.setSuperuser(req.superuser());
        user.setRoles(resolveRoles(req.roleIds()));
        userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserDto.from(user));
    }

    @PutMapping("/users/{id}")
    @PreAuthorize("@perm.has('user:write')")
    public UserDto updateUser(@PathVariable Long id, @RequestBody UserUpdateRequest req) {
        User user = userRepository.findById(id).orElseThrow(ApiException::notFound);
        if (req.fullName() != null) {
            user.setFullName(req.fullName());
        }
        if (req.email() != null) {
            user.setEmail(req.email());
        }
        if (req.password() != null && !req.password().isBlank()) {
            user.setHashedPassword(passwordEncoder.encode(req.password()));
        }
        if (req.active() != null) {
            user.setActive(req.active());
        }
        if (req.roleIds() != null) {
            user.setRoles(resolveRoles(req.roleIds()));
        }
        userRepository.save(user);
        return UserDto.from(user);
    }

    private LinkedHashSet<Role> resolveRoles(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return new LinkedHashSet<>();
        }
        return new LinkedHashSet<>(roleRepository.findAllById(roleIds));
    }
}
