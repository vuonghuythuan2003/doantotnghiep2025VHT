package ra.doantotnghiep2025.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import ra.doantotnghiep2025.security.jwt.CustomAccessDeniedHandler;
import ra.doantotnghiep2025.security.jwt.JwtAuthTokenFilter;
import ra.doantotnghiep2025.security.jwt.JwtEntryPoint;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UserDetailService userDetailService;

    @Autowired
    private JwtAuthTokenFilter jwtAuthTokenFilter;

    @Autowired
    private CustomAccessDeniedHandler customAccessDeniedHandler;

    @Autowired
    private JwtEntryPoint jwtEntryPoint;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        log.info("Configuring Spring Security...");
        return httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:5173"));
                    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                    config.setAllowedHeaders(List.of("*"));
                    config.setExposedHeaders(List.of("*"));
                    config.setAllowCredentials(true);
                    return config;
                }))
                .authenticationProvider(authenticationProvider())
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers(
                            "/api/v1/auth/sign-up",
                            "/api/v1/auth/sign-in",
                            "/api/v1/categories",
                            "/api/v1/products/search",
                            "/api/v1/products",
                            "/api/v1/products/featured-products",
                            "/api/v1/products/new-products",
                            "/api/v1/products/best-seller-products",
                            "/api/v1/products/categories/{categoryId}",
                            "/api/v1/products/{productId}",
                            "/api/v1/account/forgot-password",
                            "/api/v1/account/reset-password",
                            "/api/v1/user/cart/checkout/success",
                            "/api/v1/user/cart/checkout/cancel",
                            "/api/paypal/**" // Cho phép tất cả endpoint PayPal
                    ).permitAll();

                    auth.requestMatchers("/api/v1/auth/logout").authenticated();

                    auth.requestMatchers("/api/v1/user/**").hasAuthority("USER");

                    auth.requestMatchers("/api/v1/admin/**").hasAuthority("ADMIN");

                    auth.anyRequest().authenticated();
                })
                .sessionManagement(auth -> auth.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(auth -> auth
                        .authenticationEntryPoint(jwtEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler))
                .addFilterBefore(jwtAuthTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        log.info("Configuring AuthenticationProvider...");
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailService);
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        log.info("Configuring PasswordEncoder...");
        return new BCryptPasswordEncoder();
    }
}