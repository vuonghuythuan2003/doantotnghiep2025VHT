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
        return httpSecurity.csrf(AbstractHttpConfigurer::disable)
                .authenticationProvider(authenticationProvider())
                .authorizeHttpRequests(auth->{
                    auth
                            .requestMatchers("/api/v1/admin/accounts/**").hasAuthority("ADMIN")
                            .requestMatchers("/api/v1/admin/blogs").hasAnyAuthority("ADMIN","BLOGGER")
                            .requestMatchers("/api/v1/home","/api/v1/auth/**").permitAll()
                            .anyRequest().authenticated();
                }).sessionManagement(auth->auth.sessionCreationPolicy(SessionCreationPolicy.STATELESS)).
                exceptionHandling(auth->auth.authenticationEntryPoint(jwtEntryPoint).accessDeniedHandler(customAccessDeniedHandler)).
                addFilterBefore(jwtAuthTokenFilter, UsernamePasswordAuthenticationFilter.class).
                build();
    }
    @Bean
    public AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailService);
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

}