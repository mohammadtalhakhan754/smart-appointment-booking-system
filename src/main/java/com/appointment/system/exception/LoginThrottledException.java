package com.appointment.system.exception;

/**
 * Exception thrown when login throttling is applied
 * Contains remaining lock time for client-side retry logic
 */
public class LoginThrottledException extends RuntimeException {

    private final long remainingLockTimeSeconds;

    /**
     * Constructor with message and remaining lock time
     *
     * @param message The error message
     * @param remainingLockTimeSeconds Seconds until account can attempt login again
     */
    public LoginThrottledException(String message, long remainingLockTimeSeconds) {
        super(message);
        this.remainingLockTimeSeconds = remainingLockTimeSeconds;
    }

    /**
     * Get remaining lock time
     *
     * @return seconds until account is unlocked
     */
    public long getRemainingLockTimeSeconds() {
        return remainingLockTimeSeconds;
    }

    /**
     * Get remaining lock time in minutes (for display)
     *
     * @return minutes until account is unlocked
     */
    public long getRemainingLockTimeMinutes() {
        return remainingLockTimeSeconds / 60;
    }
}
