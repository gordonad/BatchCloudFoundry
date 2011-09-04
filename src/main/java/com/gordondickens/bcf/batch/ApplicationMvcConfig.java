package com.gordondickens.bcf.batch;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceConfigurer;
import org.springframework.web.servlet.config.annotation.ViewControllerConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.theme.ThemeChangeInterceptor;

import com.gordondickens.bcf.entity.Product;
import com.gordondickens.bcf.entity.ProductTrx;

@Configuration
public class ApplicationMvcConfig extends WebMvcConfigurerAdapter {

	@Override
	public void configureViewControllers(ViewControllerConfigurer configurer) {
		// <mvc:view-controller path="/" view-name="welcome"/>
		configurer.mapViewName("/", "index");
		configurer.mapViewNameByConvention("/uncaughtException");
		configurer.mapViewNameByConvention("/resourceNotFound");
		configurer.mapViewNameByConvention("/dataAccessFailure");
	}

	@Override
	public void configureInterceptors(InterceptorConfigurer configurer) {
		configurer.addInterceptor(new ThemeChangeInterceptor());
		LocaleChangeInterceptor locale = new LocaleChangeInterceptor();
		locale.setParamName("lang");
		configurer.addInterceptor(locale);
	}

	@Override
	public void configureDefaultServletHandling(
			DefaultServletHandlerConfigurer configurer) {
		configurer.enable();
	}

	@Override
	public void configureResourceHandling(ResourceConfigurer configurer) {
		configurer.addPathMapping("/resources/**");
		configurer.addResourceLocations("/", "/META-INF/web-resources/");
	}

	@Override
	public void addFormatters(FormatterRegistry registry) {
		registry.addConverter(new ProductConverter());
		registry.addConverter(new ProductTrxConverter());
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
