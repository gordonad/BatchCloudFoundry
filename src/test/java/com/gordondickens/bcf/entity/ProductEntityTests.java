package com.gordondickens.bcf.entity;

import com.gordondickens.bcf.config.ApplicationConfigLocal;
import com.gordondickens.bcf.service.ProductService;
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
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.assertNotNull;

/**
 * User: gordondickens
 * Date: 6/6/12
 * Time: 7:51 PM
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ApplicationConfigLocal.class, loader = AnnotationConfigContextLoader.class)
@ActiveProfiles(profiles = Env.LOCAL)
public class ProductEntityTests {
    private static Logger logger = LoggerFactory.getLogger(ProductEntityTests.class);

    @Autowired
    ProductService productService;

    @Test
    @Transactional
    public void profileSave() {
        logger.debug("Packages to Scan: '{}'", Product.class.getPackage().getName());
        assertNotNull("Product Repository MUST exist", productService);
        Product product = productService.saveProduct(createProduct());
        assertNotNull("Product MUST Exist", product);
        assertNotNull("Product MUST have a version", product.getVersion());
        assertNotNull("Product MUST hava an ID", product.getId());
    }

    private Product createProduct() {
        Product product = new Product();
        product.setDescription("This is a fine description");
        product.setProductId("123");
        product.setQuantity(100);
        product.setStore("King of Prussia");

        return product;
    }


}
