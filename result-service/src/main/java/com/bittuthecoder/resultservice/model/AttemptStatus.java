package com.bittuthecoder.resultservice.model;

/**
 * Lifecycle states of an exam sitting.
 */
public enum AttemptStatus {
    NOT_STARTED,
    IN_PROGRESS,
    SUBMITTED,
    AUTO_SUBMITTED,
    EXPIRED,
    TERMINATED
}
