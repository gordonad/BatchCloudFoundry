package com.gordondickens.bcf.repository;

import com.gordondickens.bcf.config.ApplicationConfigLocal;
import com.gordondickens.bcf.entity.Product;
import com.gordondickens.bcf.services.Env;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import static org.junit.Assert.*;


@ActiveProfiles(profiles = Env.LOCAL)
@ContextConfiguration(classes = ApplicationConfigLocal.class, loader = AnnotationConfigContextLoader.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class ProductRepositoryTest {
    private static final String PRODUCT_DESC = "Cap'n Curdles Chunky Chowder";

    private static final String PRODUCT_ID = "Yummy-Chowder";

    private static final Logger logger = LoggerFactory
            .getLogger(ProductRepositoryTest.class);

    @Autowired
    ProductRepository repository;

    @Test
    public void testInsert() {
        Product product = new Product();
        product.setDescription("productname");
        product.setProductId(PRODUCT_ID);
        product.setStore("Dayton");
        product.setQuantity(100);

        product = repository.save(product);
        logger.debug("Saved Product {}", product);
        Product retrievedProduct = repository.findOne(product.getId());
        assertNotNull("Retrieved product MUST exist", retrievedProduct);
        assertEquals("Product description MUST match",
                retrievedProduct.getDescription(), product.getDescription());
        assertEquals("Product id MUST match", retrievedProduct.getProductId(),
                product.getProductId());
        assertEquals("Product store MUST match", retrievedProduct.getStore(),
                product.getStore());
        assertEquals("Product quantity MUST match",
                retrievedProduct.getQuantity(), product.getQuantity());

        Integer rowsAffected = repository.setNewDescriptionForProduct(
                PRODUCT_ID, PRODUCT_DESC);
        assertTrue("id MUST be valid", rowsAffected > 0);
        Product p = repository.findByProductId(PRODUCT_ID);
        logger.debug("Product after update {}", p);
        assertNotNull("Product MUST exist", p);
        assertEquals("Product Description MUST match", PRODUCT_DESC,
                p.getDescription());
    }
}
