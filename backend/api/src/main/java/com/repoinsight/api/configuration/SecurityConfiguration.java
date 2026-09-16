package com.repoinsight.api.configuration;

import com.repoinsight.api.infrastructure.security.GitHubOAuthFailureHandler;
import com.repoinsight.api.infrastructure.security.GitHubOAuthSuccessHandler;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
public class SecurityConfiguration {

	@Bean
	SecurityFilterChain securityFilterChain(
			HttpSecurity http,
			GitHubOAuthSuccessHandler successHandler,
			GitHubOAuthFailureHandler failureHandler,
			AppProperties appProperties) throws Exception {
		CookieCsrfTokenRepository csrfRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
		RequestMatcher apiRequest = request -> request.getRequestURI().startsWith("/api/");
		http
				.authorizeHttpRequests(authorize -> {
					if (appProperties.github().configured()) {
						authorize.requestMatchers(
								"/api/auth/github/disconnect", "/api/github/**").authenticated();
					} else {
						authorize.requestMatchers(
								"/oauth2/**", "/login/oauth2/**", "/api/auth/github/disconnect", "/api/github/**").denyAll();
					}
					authorize.anyRequest().permitAll();
				})
				.cors(Customizer.withDefaults())
				.exceptionHandling(exceptions -> exceptions.defaultAuthenticationEntryPointFor(
						new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED), apiRequest))
				.csrf(csrf -> csrf
						.csrfTokenRepository(csrfRepository)
						.ignoringRequestMatchers("/api/repositories/**"));
		if (appProperties.github().configured()) {
			http.oauth2Login(oauth -> oauth
					.successHandler(successHandler)
					.failureHandler(failureHandler));
		}
		return http.build();
	}
}