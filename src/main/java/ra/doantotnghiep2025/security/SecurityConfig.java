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
import ra.doantotnghiep2025.security.jwt.CustomAccessDeniedHandler;
import ra.doantotnghiep2025.security.jwt.JwtAuthTokenFilter;
import ra.doantotnghiep2025.security.jwt.JwtEntryPoint;

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
        return httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .authenticationProvider(authenticationProvider())
                .authorizeHttpRequests(auth -> {
                    // API dành cho ADMIN
                    auth.requestMatchers(
                            "/api/v1/admin/users",
                            "/api/v1/admin/users/{userId}/role/{roleId}",
                            "/api/v1/admin/users/{userId}",
                            "/api/v1/admin/roles",
                            "/api/v1/admin/search",
                            "/api/v1/admin/products",
                            "/api/v1/admin/products/{productId}",
                            "/api/v1/admin/categories",
                            "/api/v1/admin/categories/{categoryId}",
                            "/api/v1/admin/orders",
                            "/api/v1/admin/orders/{orderId}",
                            "/api/v1/admin/orders/{orderId}/status",
                            "/api/v1/admin/sales-revenue-over-time",
                            "/api/v1/admin/reports/best-seller-products",
                            "/api/v1/admin/reports/revenue-by-category",
                            "/api/v1/admin/reports/top-spending-customers",
                            "/api/v1/admin/reports/new-accounts-this-month",
                            "/api/v1/admin/reports/invoices-over-time"
                    ).hasAuthority("ADMIN");

                    // API dành cho USER
                    auth.requestMatchers(
                            "/api/v1/user/cart/list",
                            "/api/v1/user/cart/add",
                            "/api/v1/user/cart/items/{cartItemId}",
                            "/api/v1/user/cart/clear",
                            "/api/v1/user/cart/checkout",
                            "/api/v1/user/account",
                            "/api/v1/user/account/change-password",
                            "/api/v1/user/account/addresses",
                            "/api/v1/user/account/addresses/{addressId}",
                            "/api/v1/user/history",
                            "/api/v1/user/history/{serialNumber}",
                            "/api/v1/user/history/{orderStatus}",
                            "/api/v1/user/history/{orderId}/cancel",
                            "/api/v1/user/wish-list",
                            "/api/v1/user/wish-list/{wishListId}"
                    ).hasAuthority("USER");

                    // API công khai (không cần đăng nhập)
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
                            "/api/v1/products/{productId}"
                    ).permitAll();

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
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailService);
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
