package com.MobilePrepaidRecharge.app.config;

import com.MobilePrepaidRecharge.app.security.JWTAuthenticationFilter;
import com.MobilePrepaidRecharge.app.service.AdminService;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity    // Use @EnableMethodSecurity in Spring Boot 3 instead of @EnableGlobalMethodSecurity
public class SecurityConfig {

    @Autowired
    private AdminService adminService;
    
    @Autowired
    private JWTAuthenticationFilter jwtAuthenticationFilter;
    
    @Autowired
    private PasswordEncoder passwordEncoder; 

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                   .userDetailsService(adminService)
                   .passwordEncoder(passwordEncoder)
                   .and()
                   .build();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .cors(cors -> {}) // Enable CORS as needed
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                // Public endpoints
                .requestMatchers(HttpMethod.GET, "/api/plans").permitAll()
                .requestMatchers(HttpMethod.PUT, "/api/plans").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/subscribe").permitAll()
                .requestMatchers("/api/users/register").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/users/login","/api/users/verify-otp").permitAll()
                .requestMatchers("/api/users").permitAll()
                .requestMatchers("/api/payment").permitAll()
                .requestMatchers("/api/payment/createOrder").permitAll()
                .requestMatchers("/api/plans", "/api/users/check-phone/**", "/api/payment/**").permitAll() // Permit all payment endpoints
                
                .requestMatchers(HttpMethod.GET, "/api/users/check-phone/**").permitAll()

                // Admin endpoints
                .requestMatchers("/api/admin/register", "/api/admin/login", "/api/admin/logout").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                
                // Plan modifications require admin
                .requestMatchers(HttpMethod.POST, "/api/plans").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/plans/update").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/plans/delete").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://127.0.0.1:5500", "http://localhost:5500")); // Allow frontend origin
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true); // Allow cookies, if needed
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
