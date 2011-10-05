package com.gordondickens.bcf.web;

import java.util.Properties;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.ui.context.support.ResourceBundleThemeSource;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceConfigurer;
import org.springframework.web.servlet.config.annotation.ViewControllerConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.theme.CookieThemeResolver;
import org.springframework.web.servlet.theme.ThemeChangeInterceptor;
import org.springframework.web.servlet.view.UrlBasedViewResolver;
import org.springframework.web.servlet.view.tiles2.TilesConfigurer;
import org.springframework.web.servlet.view.tiles2.TilesView;

import com.gordondickens.bcf.entity.Product;
import com.gordondickens.bcf.entity.ProductTrx;

@Configuration
@EnableWebMvc
public class ApplicationMvcConfig extends WebMvcConfigurerAdapter {
	@Bean
	public UrlBasedViewResolver tilesViewResolver() {
		UrlBasedViewResolver url = new UrlBasedViewResolver();
		url.setViewClass(TilesView.class);
		return url;
	}

	@Bean
	public TilesConfigurer tilesConfigurer() {
		TilesConfigurer tilesConfig = new TilesConfigurer();
		tilesConfig
				.setDefinitions(new String[] { "/WEB-INF/layouts/layouts.xml",
						"/WEB-INF/views/**/views.xml" });
		return tilesConfig;
	}

	@Bean
	public SimpleMappingExceptionResolver simpleMappingExceptionResolver() {
		SimpleMappingExceptionResolver er = new SimpleMappingExceptionResolver();
		er.setDefaultErrorView("uncaughtException");
		Properties mappings = new Properties();
		mappings.put(".DataAccessException", "dataAccessFailure");
		mappings.put(".NoSuchRequestHandlingMethodException",
				"resourceNotFound");
		mappings.put(".TypeMismatchException", "resourceNotFound");
		mappings.put(".MissingServletRequestParameterException",
				"resourceNotFound");

		er.setExceptionMappings(mappings);
		return er;
	}

	@Bean
	public ResourceBundleThemeSource themeSource() {
		return new ResourceBundleThemeSource();
	}

	@Bean
	public CookieThemeResolver themeResolver() {
		CookieThemeResolver themeResolver = new CookieThemeResolver();
		themeResolver.setCookieName("theme");
		themeResolver.setDefaultThemeName("standard");
		return themeResolver;
	}

	@Bean
	public CookieLocaleResolver localeResolver() {
		CookieLocaleResolver localeResolver = new CookieLocaleResolver();
		localeResolver.setCookieName("locale");
		return localeResolver;
	}

	@Bean
	public ReloadableResourceBundleMessageSource messageSource() {
		ReloadableResourceBundleMessageSource source = new ReloadableResourceBundleMessageSource();
		source.setBasenames(new String[] { "WEB-INF/i18n/messages",
				"WEB-INF/i18n/application" });
		source.setFallbackToSystemLocale(false);
		return source;
	}

	@Bean
	public CommonsMultipartResolver multipartResolver() {
		return new CommonsMultipartResolver();
	}

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
