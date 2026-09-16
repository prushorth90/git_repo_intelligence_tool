package com.repoinsight.api.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfiguration implements WebMvcConfigurer {

	private final AppProperties appProperties;

	public WebConfiguration(AppProperties appProperties) {
		this.appProperties = appProperties;
	}

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/api/**")
				.allowedOrigins(appProperties.cors().allowedOrigin())
				.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
	}
}