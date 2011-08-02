package com.gordondickens.bcf.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.gordondickens.bcf.entity.ProductTrx;

public interface ProductTrxRepository extends CrudRepository<ProductTrx, Long> {
	@Query("FROM ProductTrx")
	List<ProductTrx> findProductTrxEntries();
}
