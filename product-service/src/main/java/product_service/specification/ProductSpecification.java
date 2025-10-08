package product_service.specification;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import product_service.entity.Category;
import product_service.entity.Product;

import java.math.BigDecimal;
import java.util.List;

public class ProductSpecification {

    public static Specification<Product> hasAuthor(String author) {
        return (root, query, builder) ->
                (author == null || author.isEmpty()) ? null :
                        builder.like(builder.lower(root.get("author")), "%" + author.toLowerCase() + "%");
    }

    public static Specification<Product> hasName(String name) {
        return (root, query, builder) ->
                (name == null || name.isEmpty()) ? null :
                        builder.like(builder.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    public static Specification<Product> hasLanguage(String language) {
        return (root, query, builder) ->
                (language == null || language.isEmpty()) ? null :
                        builder.equal(root.get("language"), language);
    }

    public static Specification<Product> hasPriceBetween(BigDecimal minPrice, BigDecimal maxPrice) {
        return (root, query, builder) -> {
            if (minPrice == null && maxPrice == null) return null;
            if (minPrice != null && maxPrice != null) return builder.between(root.get("price"), minPrice, maxPrice);
            if (minPrice != null) return builder.greaterThanOrEqualTo(root.get("price"), minPrice);
            return builder.lessThanOrEqualTo(root.get("price"), maxPrice);
        };
    }

    public static Specification<Product> hasCategories(List<Long> categoryIds) {
        return (root, query, builder) -> {
            if (categoryIds == null || categoryIds.isEmpty()) return null;

            Join<Product, Category> join = root.join("categories", JoinType.INNER);
            return join.get("id").in(categoryIds);
        };
    }
}
