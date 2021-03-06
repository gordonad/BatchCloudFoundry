package com.gordondickens.bcf.repository;

import com.gordondickens.bcf.entity.ProductTrx;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

//@RepositoryDefinition(domainClass = ProductTrx.class, idClass = Long.class)
@Repository
public interface ProductTrxRepository extends JpaRepository<ProductTrx, Long>, JpaSpecificationExecutor<ProductTrx> {

}
