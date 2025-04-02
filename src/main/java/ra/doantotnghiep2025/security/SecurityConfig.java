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

import java.util.List;

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
                .csrf(AbstractHttpConfigurer::disable) // Tắt CSRF vì dùng JWT
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:5173"));
                    config.setAllowedMethods(List.of("*"));
                    config.setAllowedHeaders(List.of("*"));
                    config.setExposedHeaders(List.of("*"));
                    config.setAllowCredentials(true);
                    return config;
                }))
                .authenticationProvider(authenticationProvider())
                .authorizeHttpRequests(auth -> {
                    // 1. Khách hàng vãng lai (Guest) - API công khai, không cần đăng nhập
                    auth.requestMatchers(
                            "/api/v1/auth/sign-up",              // Đăng ký
                            "/api/v1/auth/sign-in",              // Đăng nhập
                            "/api/v1/auth/logout",               // Đăng xuất (Thêm vào đây)
                            "/api/v1/categories",                // Danh mục sản phẩm
                            "/api/v1/products/search",           // Tìm kiếm sản phẩm
                            "/api/v1/products",                  // Danh sách sản phẩm
                            "/api/v1/products/featured-products", // Sản phẩm nổi bật
                            "/api/v1/products/new-products",     // Sản phẩm mới
                            "/api/v1/products/best-seller-products", // Sản phẩm bán chạy
                            "/api/v1/products/categories/{categoryId}", // Sản phẩm theo danh mục
                            "/api/v1/products/brands/{brandId}", // Sản phẩm theo thương hiệu
                            "/api/v1/products/{productId}",      // Chi tiết sản phẩm
                            "/api/v1/account/forgot-password",   // Quên mật khẩu
                            "/api/v1/account/reset-password"     // Đặt lại mật khẩu
                    ).permitAll();

                    // 2. Khách hàng (User) - API yêu cầu đăng nhập và vai trò USER
                    auth.requestMatchers("/api/v1/user/**").hasAuthority("USER");

                    // 3. Admin - API yêu cầu đăng nhập và vai trò ADMIN
                    auth.requestMatchers("/api/v1/admin/**").hasAuthority("ADMIN");

                    // Tất cả các request khác cần xác thực
                    auth.anyRequest().authenticated();
                })
                .sessionManagement(auth -> auth.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Không dùng session
                .exceptionHandling(auth -> auth
                        .authenticationEntryPoint(jwtEntryPoint) // Xử lý lỗi xác thực
                        .accessDeniedHandler(customAccessDeniedHandler)) // Xử lý lỗi quyền truy cập
                .addFilterBefore(jwtAuthTokenFilter, UsernamePasswordAuthenticationFilter.class) // Thêm filter JWT
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