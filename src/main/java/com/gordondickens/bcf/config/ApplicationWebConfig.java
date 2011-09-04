package com.gordondickens.bcf.config;

import java.util.Properties;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.ui.context.support.ResourceBundleThemeSource;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.theme.CookieThemeResolver;
import org.springframework.web.servlet.view.UrlBasedViewResolver;
import org.springframework.web.servlet.view.tiles2.TilesConfigurer;
import org.springframework.web.servlet.view.tiles2.TilesView;

import com.gordondickens.bcf.batch.ApplicationMvcConfig;

@Configuration
@EnableWebMvc
@Import(ApplicationMvcConfig.class)
public class ApplicationWebConfig {

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

}
