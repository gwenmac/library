package library.security;

import library.entities.User;
import org.springframework.security.core.context.SecurityContextHolder;

public class CurrentUser {

    public static User get() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public static Long id() {
        return get().getId();
    }

    public static Long householdId() {
        return get().getHousehold().getId();
    }
}
