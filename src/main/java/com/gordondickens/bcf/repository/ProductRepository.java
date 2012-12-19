package com.gordondickens.bcf.repository;

import com.gordondickens.bcf.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(readOnly = true)
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    @Query
    Product findByProductId(String productId);

    @Modifying
    @Transactional(readOnly = false)
    @Query("update Product p set p.description = :description where p.productId = :productId")
    Integer setNewDescriptionForProduct(@Param("productId") String productId,
                                        @Param("description") String description);


}
