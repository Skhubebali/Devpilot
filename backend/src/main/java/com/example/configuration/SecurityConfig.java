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
                        auth.requestMatchers("/api/auth/login-url", "/oauth2/**",
                                "/error", "/login/oauth2/**").permitAll()
                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                .anyRequest().authenticated())
                            .exceptionHandling(ex -> ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .oauth2Login(login -> login
                        // Added custom loginPage to disable Spring Security's default auto-generated login form
                        // while keeping OAuth2 functional. Unauthenticated access will return 401 (configured above).
                        .loginPage("/api/auth/login-url")
                        .userInfoEndpoint(user ->
                                 user.userService(gitHubOauth2UserService))
                        .successHandler(successHandler).failureHandler(failureHandler));
//                .authorizeHttpRequests(auth ->
//                        auth
//                                .requestMatchers(
//                                        "/api/auth/login-url",
//                                        "/oauth2/**",
//                                        "/error",
//                                        "/login/oauth2/**"
//                                ).permitAll()
//
//                                .requestMatchers("/api/repositories/**").permitAll()
//
//                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
//
//                                .anyRequest().authenticated()
//                );
        return http.build();


    }

    @Bean
    AuthenticationSuccessHandler successHandler(
            @Value("${app.frontend-url}") String url) {

        SimpleUrlAuthenticationSuccessHandler handler =
                new SimpleUrlAuthenticationSuccessHandler();

        handler.setDefaultTargetUrl(url + "/dashboard");

        return handler;
    }

    @Bean
    AuthenticationFailureHandler failureHandler(
            @Value("${app.frontend-url}") String url) {

        SimpleUrlAuthenticationFailureHandler handler =
                new SimpleUrlAuthenticationFailureHandler();

        handler.setDefaultFailureUrl(url + "/login?error=oauth_failed");

        return handler;
    }
}
