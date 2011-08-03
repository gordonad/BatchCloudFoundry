package com.gordondickens.bcf.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.gordondickens.bcf.entity.Product;

@Transactional(readOnly = true)
public interface ProductRepository extends CrudRepository<Product, Long> {
	@Query("FROM Product")
	List<Product> findAllProducts();

	// @Query("FROM Product p where p.productId = :productId")
	// Product findByProductId(@Param("productId") String productId);
	@Query
	Product findByProductId(String productId);

	@Modifying
	@Transactional(readOnly = false)
	@Query("update Product p set p.description = :description where p.productId = :productId")
	Integer setNewDescriptionForProduct(@Param("productId") String productId,
			@Param("description") String description);

	// @Modifying
	// @Query("update Product p set p.description = ?2 where p.productId = ?1")
	// Integer setNewDescriptionForProduct(String productId, String
	// description);

}
