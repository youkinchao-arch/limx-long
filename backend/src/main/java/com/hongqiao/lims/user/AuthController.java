package com.hongqiao.lims.user;

import com.hongqiao.lims.audit.AuditService;
import com.hongqiao.lims.common.ApiException;
import com.hongqiao.lims.config.LimsProperties;
import com.hongqiao.lims.security.JwtService;
import com.hongqiao.lims.security.LoginRateLimiter;
import com.hongqiao.lims.security.PasswordPolicy;
import com.hongqiao.lims.security.SecurityUtils;
import com.hongqiao.lims.user.dto.ChangePasswordRequest;
import com.hongqiao.lims.user.dto.RoleDto;
import com.hongqiao.lims.user.dto.TokenResponse;
import com.hongqiao.lims.user.dto.UserCreateRequest;
import com.hongqiao.lims.user.dto.UserDto;
import com.hongqiao.lims.user.dto.UserUpdateRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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
    private final PasswordPolicy passwordPolicy;
    private final LoginRateLimiter loginRateLimiter;
    private final AuditService auditService;
    private final LimsProperties props;

    public AuthController(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            PasswordPolicy passwordPolicy,
            LoginRateLimiter loginRateLimiter,
            AuditService auditService,
            LimsProperties props) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.passwordPolicy = passwordPolicy;
        this.loginRateLimiter = loginRateLimiter;
        this.auditService = auditService;
        this.props = props;
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public TokenResponse login(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            HttpServletRequest request) {
        loginRateLimiter.check(clientIp(request));
        User user = userRepository.findByUsername(username).orElse(null);
        if (user != null && user.getLockedUntil() != null
                && user.getLockedUntil().isAfter(Instant.now())) {
            throw new ApiException(HttpStatus.FORBIDDEN,
                    "账号已锁定，请稍后再试 / Account is locked, please try again later");
        }
        if (user == null || !passwordEncoder.matches(password, user.getHashedPassword())) {
            if (user != null) {
                registerFailedAttempt(user);
            }
            auditService.recordAuth("LOGIN_FAILED", user != null ? user.getId() : null, username);
            throw new ApiException(
                    HttpStatus.UNAUTHORIZED, "用户名或密码错误 / Invalid username or password");
        }
        if (!user.isActive()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "账号已停用 / Inactive account");
        }
        if (user.getFailedLoginAttempts() != 0 || user.getLockedUntil() != null) {
            user.setFailedLoginAttempts(0);
            user.setLockedUntil(null);
            userRepository.save(user);
        }
        auditService.recordAuth("LOGIN", user.getId(), user.getUsername());
        return TokenResponse.bearer(jwtService.generateToken(user));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        User user = SecurityUtils.currentUser();
        user.setTokenVersion(user.getTokenVersion() + 1);
        userRepository.save(user);
        auditService.recordAuth("LOGOUT", user.getId(), user.getUsername());
        return ResponseEntity.ok(Map.of("detail", "已登出 / Logged out"));
    }

    @PostMapping("/change-password")
    public TokenResponse changePassword(@Valid @RequestBody ChangePasswordRequest req) {
        User user = SecurityUtils.currentUser();
        if (!passwordEncoder.matches(req.oldPassword(), user.getHashedPassword())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "原密码错误 / Current password is incorrect");
        }
        passwordPolicy.validate(req.newPassword());
        user.setHashedPassword(passwordEncoder.encode(req.newPassword()));
        user.setTokenVersion(user.getTokenVersion() + 1);
        userRepository.save(user);
        auditService.recordAuth("CHANGE_PASSWORD", user.getId(), user.getUsername());
        return TokenResponse.bearer(jwtService.generateToken(user));
    }

    @GetMapping("/me")
    public UserDto me() {
        return UserDto.from(SecurityUtils.currentUser());
    }

    private void registerFailedAttempt(User user) {
        int attempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);
        if (attempts >= props.getSecurity().getMaxFailedAttempts()) {
            user.setLockedUntil(
                    Instant.now().plus(props.getSecurity().getLockoutMinutes(), ChronoUnit.MINUTES));
            user.setFailedLoginAttempts(0);
        }
        userRepository.save(user);
    }

    private static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
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
        passwordPolicy.validate(req.password());
        User user = new User();
        user.setUsername(req.username());
        user.setFullName(req.fullName());
        user.setEmail(req.email());
        user.setHashedPassword(passwordEncoder.encode(req.password()));
        user.setSuperuser(req.superuser());
        user.setRoles(resolveRoles(req.roleIds()));
        userRepository.save(user);
        auditService.recordChange("CREATE", "User", null, UserDto.from(user));
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
            passwordPolicy.validate(req.password());
            user.setHashedPassword(passwordEncoder.encode(req.password()));
            user.setTokenVersion(user.getTokenVersion() + 1);
        }
        if (req.active() != null) {
            user.setActive(req.active());
        }
        if (req.roleIds() != null) {
            user.setRoles(resolveRoles(req.roleIds()));
        }
        userRepository.save(user);
        auditService.recordChange("UPDATE", "User", null, UserDto.from(user));
        return UserDto.from(user);
    }

    private LinkedHashSet<Role> resolveRoles(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return new LinkedHashSet<>();
        }
        return new LinkedHashSet<>(roleRepository.findAllById(roleIds));
    }
}
