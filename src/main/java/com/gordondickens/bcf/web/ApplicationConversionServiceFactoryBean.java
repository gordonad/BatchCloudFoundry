package com.gordondickens.bcf.web;

import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.support.FormattingConversionServiceFactoryBean;

import com.gordondickens.bcf.entity.Product;
import com.gordondickens.bcf.entity.ProductTrx;

/**
 * A central place to register application converters and formatters.
 */
public class ApplicationConversionServiceFactoryBean extends
		FormattingConversionServiceFactoryBean {

	@Override
	protected void installFormatters(FormatterRegistry registry) {
		super.installFormatters(registry);
		// Register application converters and formatters
	}

	public void installLabelConverters(FormatterRegistry registry) {
		registry.addConverter(new ProductConverter());
		registry.addConverter(new ProductTrxConverter());
	}

	@Override
	public void afterPropertiesSet() {
		super.afterPropertiesSet();
		installLabelConverters(getObject());
	}

	static class ProductConverter implements Converter<Product, String> {
		@Override
		public String convert(Product product) {
			return new StringBuilder().append(product.getProductId())
					.append(" ").append(product.getStore()).append(" ")
					.append(product.getQuantity()).append(" ")
					.append(product.getDescription()).toString();
		}

	}

	static class ProductTrxConverter implements Converter<ProductTrx, String> {
		@Override
		public String convert(ProductTrx productTrx) {
			return new StringBuilder().append(productTrx.getStore())
					.append(" ").append(productTrx.getQuantity()).append(" ")
					.append(productTrx.getPrice()).append(" ")
					.append(productTrx.getTrxDate()).toString();
		}

	}
}
