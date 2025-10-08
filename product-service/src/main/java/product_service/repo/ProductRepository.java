package product_service.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import product_service.entity.Product;

import java.util.List;
import java.util.Map;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    Page<Product> findAll(Specification<Product> spec, Pageable pageable);
    boolean existsByName(String name);

//Các cách truy vấn lấy list product by categoryId
//    Cách 1: JPQL
    @Query("SELECT p FROM Product p JOIN p.categories c where c.id = :categoryId")
    List<Product> GetProductsByCategoryId(Long categoryId);

//    Cách 2: native query, dùng Param trong trường họp có nhiều parameter
//    @Query(value = "SELECT p.* " +
//            "FROM products p " +
//            "JOIN product_categories pc ON p.id = pc.product_id " +
//            "WHERE pc.category_id = :categoryId",
//            nativeQuery = true)
//    List<Product> GetProductsByCategoryId(@Param("categoryId") Long categoryId);

    //    Cách 3: native query, dùng ?1 khi chỉ có 1 parameter duy nhất
//    @Query(value = "SELECT p.* " +
//            "FROM products p " +
//            "JOIN product_categories pc ON p.id = pc.product_id " +
//            "WHERE pc.category_id = ?1",
//            nativeQuery = true)
//    List<Product> GetProductsByCategoryId(Long categoryId);

//    Cách 4: Dùng store procedure
//    @Procedure(name = "GetProductsByCategoryId")
//    List<Product> getProductsByCategoryId(@Param("categoryId") Long categoryId);

    //JPQL
        //Query đơn giản, theo entity
        //Muốn portable giữa DB
        //Muốn tận dụng cache của JPA
    //Native Query:
        //Query phức tạp, join nhiều bảng, subquery, function DB-specific
        //Khi JPQL không hỗ trợ
        //Muốn tối ưu SQL để performance cao

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
//    @Query(value = "CALL get_products_search_by_category_id_and_order_by_price_desc(:categoryId)", nativeQuery = true)
//    List<Map<String, Object>> findProductsByCategoryOrderByPriceDesc(@Param("categoryId") Long categoryId);

}
