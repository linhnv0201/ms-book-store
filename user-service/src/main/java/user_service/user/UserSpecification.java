package user_service.user;

import org.springframework.data.jpa.domain.Specification;
import user_service.user.entity.User;


public class UserSpecification {
    public static Specification<User> hasEmail(String email) {
        return (root, query, criteriaBuilder) -> {
            if (email == null || email.isBlank()) return null;
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), "%" + email.toLowerCase() + "%");
        };
    }

    public static Specification<User> hasFullName(String fullName) {
        return (root, query, criteriaBuilder) -> {
            if (fullName == null || fullName.isBlank()) return null;
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("fullname")), "%" + fullName.toLowerCase() + "%");
        };
    }

    public static Specification<User> hasRole(String role) {
        return (root, query, criteriaBuilder) -> {
            if (role == null || role.isBlank()) {
                return null;
            }
            // Dùng isMember để kiểm tra role có trong Set<String>
            return criteriaBuilder.isMember(role, root.get("role"));
        };
    }



}
