package product_service.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import product_service.dto.response.ProductResponse;
import product_service.dto.response.ProductSummaryResponse;
import product_service.entity.Product;

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

    @Query(value = """
            SELECT DISTINCT p.id
            FROM products p
            JOIN product_categories pc ON p.id = pc.product_id
            WHERE pc.category_id IN :categoryIds 
              AND p.id <> :productId
            ORDER BY RAND()
            LIMIT 5
            """, nativeQuery = true)
    List<Long> findRandomByCategoryListGetIdOnly(@Param("categoryIds") List<Long> categoryIds,
                                                 @Param("productId") Long productId);


    @Query("""
                SELECT new product_service.dto.response.ProductSummaryResponse(
                    p.id, p.name, p.price
                )
                FROM Product p
                WHERE p.id = ?1
            """)
    ProductSummaryResponse getProductSummaryById(Long id);

    @Query("SELECT c.id FROM Product p JOIN p.categories c WHERE p.id = :productId")
    List<Long> findCategoryIdsByProductId(@Param("productId") Long productId);

    @Query(value = "SELECT * FROM products ORDER BY sold_quantity DESC LIMIT 5", nativeQuery = true)
    List<Product> findTop5ByOrderBySoldQuantityDesc();
}
