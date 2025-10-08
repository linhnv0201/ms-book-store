package product_service.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import product_service.entity.Product;
import product_service.projection.ProductProjection;

import java.util.List;
import java.util.Map;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    Page<Product> findAll(Specification<Product> spec, Pageable pageable);
    boolean existsByName(String name);

    @Query("SELECT p FROM Product p JOIN p.categories c where c.id = :categoryId")
    List<Product> GetProductsByCategoryId(Long categoryId);

    @Query(value = """
        SELECT p.id AS product_id,
               p.name AS product_name,
               p.price,
               p.cost,
               c.name AS category_name
        FROM products p
        LEFT JOIN product_categories pc ON p.id = pc.product_id
        LEFT JOIN categories c ON pc.category_id = c.id
        WHERE p.id = :id
        """, nativeQuery = true)
    List<Map<String, Object>> findProductWithCategoriesById(@Param("id") Long id);

    @Query(value = """
        SELECT 
            p.id AS product_id,
            p.name AS product_name,
            p.author,
            p.description,
            p.price,
            p.stock,
            c.name AS category_name
        FROM products p
        LEFT JOIN product_categories pc ON p.id = pc.product_id
        LEFT JOIN categories c ON pc.category_id = c.id
        WHERE p.author = :author
        ORDER BY p.name ASC
        """, nativeQuery = true)
    List<Object[]> findProductsByAuthor(@Param("author") String author);

    @Query("SELECT p.id AS id, p.name AS name, p.price AS price FROM Product p WHERE p.id = ?1")
    List<ProductProjection> getProductSummaryProjectionById(Long id);


}
