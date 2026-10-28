package com.example.configuration;

import com.example.security.gitHubOauth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.*;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final gitHubOauth2UserService gitHubOauth2UserService;


    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
                                            AuthenticationSuccessHandler successHandler,
                                            AuthenticationFailureHandler failureHandler) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))

                .authorizeHttpRequests(auth ->
                        auth.requestMatchers(
                                        "/api/auth/login-url",
                                        "/api/auth/logout",
                                        "/oauth2/**",
                                        "/error",
                                        "/login/oauth2/**"
                                ).permitAll()
                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                .anyRequest().authenticated())
                .exceptionHandling(ex -> ex.authenticationEntryPoint(
                        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .oauth2Login(login -> login
                        .loginPage("/api/auth/login-url")
                        .userInfoEndpoint(user ->
                                user.userService(gitHubOauth2UserService))
                        .successHandler(successHandler)
                        .failureHandler(failureHandler));
        return http.build();
    }

    @Bean
    AuthenticationSuccessHandler successHandler(
            @Value("${app.frontend-url}") String url) {

        SimpleUrlAuthenticationSuccessHandler handler =
                new SimpleUrlAuthenticationSuccessHandler();

        String baseUrl = (url != null && url.endsWith("/")) ? url.substring(0, url.length() - 1) : url;
        handler.setDefaultTargetUrl(baseUrl + "/dashboard");

        return handler;
    }

    @Bean
    AuthenticationFailureHandler failureHandler(
            @Value("${app.frontend-url}") String url) {

        SimpleUrlAuthenticationFailureHandler handler =
                new SimpleUrlAuthenticationFailureHandler();

        String baseUrl = (url != null && url.endsWith("/")) ? url.substring(0, url.length() - 1) : url;
        handler.setDefaultFailureUrl(baseUrl + "/login?error=oauth_failed");

        return handler;
    }
}
