package com.gordondickens.bcf.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;

import com.gordondickens.bcf.entity.Product;

public class ProductFieldSetMapper implements FieldSetMapper<Product> {
	private static final Logger logger = LoggerFactory.getLogger(ProductFieldSetMapper.class);
	
	@Override
	public Product mapFieldSet(FieldSet fieldSet) {
		Product product = new Product();
		product.setProductId(fieldSet.readString("productId"));
		product.setStore(fieldSet.readString("store"));
		product.setQuantity(fieldSet.readInt("quantity"));
		product.setDescription(fieldSet.readString("description"));
		logger.debug("Mapped Product {}", product);
		return product;
	}
}
