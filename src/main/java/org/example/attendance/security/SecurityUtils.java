package org.example.attendance.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static CustomUserDetails getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails;
        }
        return null;
    }

    public static boolean isStudent() {
        CustomUserDetails user = getCurrentUser();
        return user != null && "STUDENT".equals(user.getRole());
    }

    public static boolean isTeacher() {
        CustomUserDetails user = getCurrentUser();
        return user != null && "TEACHER".equals(user.getRole());
    }

    public static boolean isAdmin() {
        CustomUserDetails user = getCurrentUser();
        return user != null && "ADMIN".equals(user.getRole());
    }

    public static Long getCurrentGroupId() {
        CustomUserDetails user = getCurrentUser();
        return user != null ? user.getGroupId() : null;
    }

    public static Long getCurrentTeacherId() {
        CustomUserDetails user = getCurrentUser();
        return user != null ? user.getTeacherId() : null;
    }
}
