package com.bittuthecoder.common.context;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

/**
 * ThreadLocal-based context storing the active tenant (organization) and authenticated user.
 * Essential for multi-tenant isolation across all Assessify services.
 * Always call {@link #clear()} in a filter/interceptor finally block to prevent thread pool leakage.
 */
public final class TenantContext {

    private static final ThreadLocal<UUID> CURRENT_ORG_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> CURRENT_USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> CURRENT_USER_EMAIL = new ThreadLocal<>();
    private static final ThreadLocal<Set<String>> CURRENT_ROLES = new ThreadLocal<>();

    private TenantContext() {
        // static utility class
    }

    public static void setOrgId(UUID orgId) {
        CURRENT_ORG_ID.set(orgId);
    }

    public static UUID getOrgId() {
        return CURRENT_ORG_ID.get();
    }

    public static void clearOrgId() {
        CURRENT_ORG_ID.remove();
    }

    public static void setUserId(String userId) {
        CURRENT_USER_ID.set(userId);
    }

    public static String getUserId() {
        return CURRENT_USER_ID.get();
    }

    public static void clearUserId() {
        CURRENT_USER_ID.remove();
    }

    public static void setUserEmail(String email) {
        CURRENT_USER_EMAIL.set(email);
    }

    public static String getUserEmail() {
        return CURRENT_USER_EMAIL.get();
    }

    public static void clearUserEmail() {
        CURRENT_USER_EMAIL.remove();
    }

    public static void setRoles(Set<String> roles) {
        CURRENT_ROLES.set(roles != null ? Collections.unmodifiableSet(roles) : Collections.emptySet());
    }

    public static Set<String> getRoles() {
        Set<String> roles = CURRENT_ROLES.get();
        return roles != null ? roles : Collections.emptySet();
    }

    public static boolean hasRole(String role) {
        return getRoles().contains(role);
    }

    public static void clear() {
        CURRENT_ORG_ID.remove();
        CURRENT_USER_ID.remove();
        CURRENT_USER_EMAIL.remove();
        CURRENT_ROLES.remove();
    }
}
