package com.appointment.system.controller;

import com.appointment.system.dto.request.LoginRequest;
import com.appointment.system.dto.request.RegisterRequest;
import com.appointment.system.dto.response.ApiResponse;
import com.appointment.system.dto.response.AuthResponse;
import com.appointment.system.dto.response.UserResponse;
import com.appointment.system.entity.Patient;
import com.appointment.system.entity.Role;
import com.appointment.system.entity.User;
import com.appointment.system.exception.LoginThrottledException;
import com.appointment.system.repository.PatientRepository;
import com.appointment.system.repository.UserRepository;
import com.appointment.system.security.JwtTokenProvider;
import com.appointment.system.security.LoginAttemptService;
import com.appointment.system.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication Controller with Login Throttling
 *
 * Integrates LoginAttemptService to track failed login attempts
 * Implements 5-strike lockout with progressive delay
 *
 * Features:
 * - Account lockout after 5 failed attempts (15 min timeout)
 * - Progressive exponential delay (1s, 2s, 4s, 8s)
 * - Admin unlock and statistics endpoints
 * - Full compatibility with existing ApiResponse DTO
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication and registration endpoints")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;
    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final LoginAttemptService loginAttemptService;  // ✅ NEW: Inject throttling service

    /**
     * Login endpoint with throttling
     *
     * Process Flow:
     * 1. Check if account is locked due to failed attempts
     * 2. Apply progressive delay before authentication
     * 3. Attempt authentication
     * 4. On success: clear attempt counter
     * 5. On failure: increment counter and potentially lock account
     *
     * @param request LoginRequest with usernameOrEmail and password
     * @return AuthResponse with JWT tokens on success
     *
     * HTTP Status Codes:
     * - 200 OK: Login successful
     * - 401 Unauthorized: Invalid credentials (with remaining attempts)
     * - 423 Locked: Account locked due to too many failed attempts
     * - 500 Internal Server Error: Server error
     */
    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and receive JWT token with login throttling")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {

        String usernameOrEmail = request.getUsernameOrEmail();

        try {
            // ============ STEP 1: Check if account is locked ============
            if (loginAttemptService.isAccountLocked(usernameOrEmail)) {
                long remainingLockTime = loginAttemptService.getRemainingSuspensionTime(usernameOrEmail);
                long remainingMinutes = remainingLockTime / 60;

                log.warn("Login attempt on locked account: {}", usernameOrEmail);

                // Return 423 Locked status with message
                String errorMessage = String.format(
                        "Account is locked due to too many failed login attempts. " +
                                "Please try again in %d minutes and %d seconds.",
                        remainingMinutes,
                        remainingLockTime % 60
                );

                return ResponseEntity
                        .status(HttpStatus.LOCKED)  // HTTP 423
                        .body(ApiResponse.error(errorMessage));
            }

            // ============ STEP 2: Apply progressive delay ============
            long progressiveDelay = loginAttemptService.getProgressiveDelay(usernameOrEmail);
            if (progressiveDelay > 0) {
                log.debug("Applying progressive delay of {} seconds for: {}",
                        progressiveDelay, usernameOrEmail);

                try {
                    // Apply delay BEFORE password verification to save resources
                    Thread.sleep(progressiveDelay * 1000);  // Convert to milliseconds
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("Progressive delay interrupted");
                }
            }

            // ============ STEP 3: Attempt authentication ============
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            usernameOrEmail,
                            request.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // ============ STEP 4: Success - Clear attempt counter ============
            loginAttemptService.loginSucceeded(usernameOrEmail);
            log.info("Successful authentication for: {}", usernameOrEmail);

            // Generate JWT tokens
            String token = jwtTokenProvider.generateToken(authentication);
            String refreshToken = jwtTokenProvider.generateRefreshToken(authentication.getName());

            User user = userRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found after successful authentication"));

            AuthResponse authResponse = AuthResponse.builder()
                    .token(token)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtTokenProvider.getJwtExpirationMs())
                    .userId(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .role(user.getRole())
                    .build();

            return ResponseEntity.ok(ApiResponse.success("Login successful", authResponse));

        } catch (AuthenticationException authEx) {
            // ============ STEP 5: Failure - Record attempt ============
            log.warn("Failed login attempt for: {}", usernameOrEmail);

            try {
                // Record failed attempt in Redis
                loginAttemptService.loginFailed(usernameOrEmail);

                // If we reach here, account is not yet locked
                int failedAttempts = loginAttemptService.getFailedAttemptCount(usernameOrEmail);
                int remainingAttempts = 5 - failedAttempts;  // 5 = maxAttempts

                String errorMessage = String.format(
                        "Invalid credentials. Remaining attempts: %d",
                        remainingAttempts
                );

                log.warn("Failed attempt #{} for user: {}", failedAttempts, usernameOrEmail);

                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error(errorMessage));

            } catch (LoginThrottledException throttledException) {
                // Account is now locked after 5 failed attempts
                log.warn("Account locked: {}", throttledException.getMessage());

                return ResponseEntity
                        .status(HttpStatus.LOCKED)  // HTTP 423
                        .body(ApiResponse.error(throttledException.getMessage()));
            }
        } catch (Exception ex) {
            // Generic exception handler
            log.error("Unexpected error during login: {}", ex.getMessage(), ex);

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("An unexpected error occurred. Please try again."));
        }
    }

    /**
     * Register endpoint - unchanged
     * Existing functionality preserved
     */
    @PostMapping("/register")
    @Operation(summary = "User registration", description = "Register a new user account")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse userResponse = userService.createUser(request);

        // If patient, create patient profile
        if (request.getRole() == Role.PATIENT) {
            User user = userRepository.findById(userResponse.getId()).orElseThrow();
            Patient patient = Patient.builder()
                    .user(user)
                    .build();
            patientRepository.save(patient);
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", userResponse));
    }

    /**
     * Refresh token endpoint - unchanged
     * Existing functionality preserved
     */
    @PostMapping("/refresh")
    @Operation(summary = "Refresh token", description = "Get new access token using refresh token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@RequestParam String refreshToken) {
        if (jwtTokenProvider.validateToken(refreshToken)) {
            String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
            String newToken = jwtTokenProvider.generateToken(username);

            User user = userRepository.findByUsername(username).orElseThrow();

            AuthResponse authResponse = AuthResponse.builder()
                    .token(newToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtTokenProvider.getJwtExpirationMs())
                    .userId(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .role(user.getRole())
                    .build();

            return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", authResponse));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Invalid refresh token"));
    }

    // ============ NEW ADMIN ENDPOINTS ============

    /**
     * Admin endpoint to manually unlock a user account
     *
     * Use Cases:
     * - User forgot password and locked their account
     * - Support team needs to help locked user
     * - Security incident resolution
     *
     * @param usernameOrEmail The user to unlock
     * @return Success message
     *
     * Requires: ADMIN role
     */
    @PostMapping("/admin/unlock/{usernameOrEmail}")
    @Operation(summary = "Unlock user account (Admin only)",
            description = "Manually unlock an account locked due to failed login attempts")
    public ResponseEntity<ApiResponse<Void>> unlockAccount(@PathVariable String usernameOrEmail) {
        loginAttemptService.unlockAccount(usernameOrEmail);

        log.info("Account manually unlocked by admin: {}", usernameOrEmail);

        return ResponseEntity.ok(ApiResponse.success(
                String.format("Account '%s' unlocked successfully", usernameOrEmail),
                null
        ));
    }

    /**
     * Admin endpoint to get login attempt statistics for a user
     *
     * Used for:
     * - Monitoring failed login patterns
     * - Detecting brute force attacks
     * - Troubleshooting user account issues
     * - Audit and compliance reporting
     *
     * @param usernameOrEmail The user to check
     * @return Login attempt statistics
     *
     * Response includes:
     * - Failed attempts count
     * - Lock status
     * - Remaining lock time
     * - Progressive delay
     * - Max allowed attempts
     * - Lock duration config
     */
    @GetMapping("/admin/login-attempts/{usernameOrEmail}")
    @Operation(summary = "Get login attempt statistics (Admin only)",
            description = "View login attempt statistics and lock status for a user")
    public ResponseEntity<ApiResponse<Object>> getLoginAttempts(@PathVariable String usernameOrEmail) {
        var stats = loginAttemptService.getLoginAttemptStats(usernameOrEmail);

        log.debug("Admin retrieved login statistics for: {}", usernameOrEmail);

        return ResponseEntity.ok(ApiResponse.success(
                "Login attempt statistics retrieved",
                stats
        ));
    }
}
