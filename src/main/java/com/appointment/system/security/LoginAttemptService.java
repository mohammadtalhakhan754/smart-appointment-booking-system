package com.appointment.system.security;

import com.appointment.system.exception.LoginThrottledException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Service to track and throttle login attempts
 * Uses Redis to store failed login attempts with TTL
 * Implements exponential backoff penalty
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    private final RedisTemplate<String, String> redisTemplate;  // ← Changed to String

    @Value("${app.security.login.max-attempts:5}")
    private int maxAttempts;

    @Value("${app.security.login.lock-duration-minutes:15}")
    private int lockDurationMinutes;

    @Value("${app.security.login.progressive-delay-enabled:true}")
    private boolean progressiveDelayEnabled;

    private static final String LOGIN_ATTEMPT_PREFIX = "login:attempts:";
    private static final String LOGIN_LOCK_PREFIX = "login:locked:";
    private static final String LOGIN_DELAY_PREFIX = "login:delay:";

    public void loginSucceeded(String usernameOrEmail) {
        String attemptKey = LOGIN_ATTEMPT_PREFIX + usernameOrEmail;
        String lockKey = LOGIN_LOCK_PREFIX + usernameOrEmail;
        String delayKey = LOGIN_DELAY_PREFIX + usernameOrEmail;

        redisTemplate.delete(attemptKey);
        redisTemplate.delete(lockKey);
        redisTemplate.delete(delayKey);

        log.info("Successful login for: {}", usernameOrEmail);
    }

    public void loginFailed(String usernameOrEmail) throws LoginThrottledException {
        String attemptKey = LOGIN_ATTEMPT_PREFIX + usernameOrEmail;
        String lockKey = LOGIN_LOCK_PREFIX + usernameOrEmail;

        if (isAccountLocked(usernameOrEmail)) {
            long remainingLockTime = getRemainingSuspensionTime(usernameOrEmail);
            log.warn("Login attempt on locked account: {}", usernameOrEmail);
            throw new LoginThrottledException(
                    "Account is locked due to too many failed login attempts.",
                    remainingLockTime
            );
        }

        // Get current attempts as String, convert to Integer
        String attemptStr = redisTemplate.opsForValue().get(attemptKey);
        Integer attempts = (attemptStr != null) ? Integer.parseInt(attemptStr) : 0;
        attempts++;

        log.warn("Failed login attempt #{} for: {}", attempts, usernameOrEmail);

        // Store as String
        redisTemplate.opsForValue().set(
                attemptKey,
                String.valueOf(attempts),
                lockDurationMinutes,
                TimeUnit.MINUTES
        );

        if (attempts >= maxAttempts) {
            lockAccount(usernameOrEmail);
            throw new LoginThrottledException(
                    "Account locked due to too many failed login attempts.",
                    (long) lockDurationMinutes * 60
            );
        }

        if (progressiveDelayEnabled && attempts > 1) {
            setProgressiveDelay(usernameOrEmail, attempts);
        }
    }

    public boolean isAccountLocked(String usernameOrEmail) {
        String lockKey = LOGIN_LOCK_PREFIX + usernameOrEmail;
        return redisTemplate.hasKey(lockKey);
    }

    private void lockAccount(String usernameOrEmail) {
        String lockKey = LOGIN_LOCK_PREFIX + usernameOrEmail;
        redisTemplate.opsForValue().set(
                lockKey,
                "locked",  // Store as String
                lockDurationMinutes,
                TimeUnit.MINUTES
        );
        log.warn("Account locked for {} minutes: {}", lockDurationMinutes, usernameOrEmail);
    }

    private void setProgressiveDelay(String usernameOrEmail, int attemptNumber) {
        String delayKey = LOGIN_DELAY_PREFIX + usernameOrEmail;
        long delaySeconds = (long) Math.pow(2, attemptNumber - 2);
        delaySeconds = Math.min(delaySeconds, 8);

        redisTemplate.opsForValue().set(
                delayKey,
                String.valueOf(delaySeconds),  // Store as String
                lockDurationMinutes,
                TimeUnit.MINUTES
        );
        log.debug("Set progressive delay of {} seconds for: {}", delaySeconds, usernameOrEmail);
    }

    public long getProgressiveDelay(String usernameOrEmail) {
        if (!progressiveDelayEnabled) {
            return 0;
        }

        String delayKey = LOGIN_DELAY_PREFIX + usernameOrEmail;
        String delayStr = redisTemplate.opsForValue().get(delayKey);

        if (delayStr != null) {
            try {
                return Long.parseLong(delayStr);
            } catch (NumberFormatException e) {
                log.error("Invalid delay value: {}", delayStr);
                return 0;
            }
        }
        return 0;
    }

    public long getRemainingSuspensionTime(String usernameOrEmail) {
        String lockKey = LOGIN_LOCK_PREFIX + usernameOrEmail;
        Long remainingTTL = redisTemplate.getExpire(lockKey, TimeUnit.SECONDS);
        return (remainingTTL != null && remainingTTL > 0) ? remainingTTL : 0;
    }

    public int getFailedAttemptCount(String usernameOrEmail) {
        String attemptKey = LOGIN_ATTEMPT_PREFIX + usernameOrEmail;
        String attemptStr = redisTemplate.opsForValue().get(attemptKey);

        if (attemptStr != null) {
            try {
                return Integer.parseInt(attemptStr);
            } catch (NumberFormatException e) {
                log.error("Invalid attempt value: {}", attemptStr);
                return 0;
            }
        }
        return 0;
    }

    public void unlockAccount(String usernameOrEmail) {
        String lockKey = LOGIN_LOCK_PREFIX + usernameOrEmail;
        String attemptKey = LOGIN_ATTEMPT_PREFIX + usernameOrEmail;
        String delayKey = LOGIN_DELAY_PREFIX + usernameOrEmail;

        redisTemplate.delete(lockKey);
        redisTemplate.delete(attemptKey);
        redisTemplate.delete(delayKey);

        log.info("Account manually unlocked: {}", usernameOrEmail);
    }

    public LoginAttemptStats getLoginAttemptStats(String usernameOrEmail) {
        return LoginAttemptStats.builder()
                .failedAttempts(getFailedAttemptCount(usernameOrEmail))
                .isLocked(isAccountLocked(usernameOrEmail))
                .remainingLockTime(getRemainingSuspensionTime(usernameOrEmail))
                .progressiveDelay(getProgressiveDelay(usernameOrEmail))
                .maxAttempts(maxAttempts)
                .lockDurationMinutes(lockDurationMinutes)
                .build();
    }
}
