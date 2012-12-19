package com.gordondickens.bcf.batch;

import com.gordondickens.bcf.entity.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;

import java.util.List;

public class ProductItemLoggerWriter implements ItemWriter<Product> {
    private static final Logger logger = LoggerFactory
            .getLogger(ProductItemLoggerWriter.class);

    @Override
    public void write(List<? extends Product> items) throws Exception {
        for (Product product : items) {
            logger.debug("***** About to Persist {}", product);
        }
    }
}
