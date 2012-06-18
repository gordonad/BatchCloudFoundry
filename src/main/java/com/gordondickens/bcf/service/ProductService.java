package com.gordondickens.bcf.service;

import com.gordondickens.bcf.entity.Product;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public interface ProductService {
    public Product saveProduct(Product product);
}
