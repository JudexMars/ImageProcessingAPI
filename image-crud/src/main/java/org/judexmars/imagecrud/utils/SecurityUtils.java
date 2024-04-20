package org.judexmars.imagecrud.utils;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Utility class for security operations.
 */
@Component
public class SecurityUtils {

    /**
     * Get email of the logged-in user.
     *
     * @return email
     */
    public String getLoggedInUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
