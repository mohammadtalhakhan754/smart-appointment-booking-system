package com.appointment.system.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Statistics about login attempts for a user
 * Used for diagnostics and admin monitoring
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginAttemptStats {

    /**
     * Number of failed login attempts in current window
     */
    private int failedAttempts;

    /**
     * Whether account is currently locked
     */
    private boolean isLocked;

    /**
     * Remaining lock time in seconds (0 if not locked)
     */
    private long remainingLockTime;

    /**
     * Current progressive delay in seconds (0 if no delay)
     */
    private long progressiveDelay;

    /**
     * Maximum failed attempts allowed
     */
    private int maxAttempts;

    /**
     * Lock duration in minutes
     */
    private int lockDurationMinutes;

    /**
     * Whether user is eligible to attempt login
     */
    public boolean canAttemptLogin() {
        return !isLocked;
    }

    /**
     * Whether user is approaching lock threshold
     */
    public boolean isApproachingThreshold() {
        return failedAttempts >= (maxAttempts - 1);
    }

    /**
     * Remaining attempts before lock
     */
    public int getRemainingAttempts() {
        return Math.max(0, maxAttempts - failedAttempts);
    }
}
