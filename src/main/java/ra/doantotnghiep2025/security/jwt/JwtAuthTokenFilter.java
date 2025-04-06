// File: src/main/java/ra/doantotnghiep2025/security/jwt/JwtAuthTokenFilter.java
package ra.doantotnghiep2025.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import ra.doantotnghiep2025.security.UserDetailService;
import ra.doantotnghiep2025.service.TokenService;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class JwtAuthTokenFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthTokenFilter.class);

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private UserDetailService userDetailService;

    @Autowired
    private TokenService tokenService;

    private static final List<String> EXCLUDED_PATHS = Arrays.asList(
            "/api/v1/auth/sign-up",
            "/api/v1/auth/sign-in",
            "/api/v1/categories",
            "/api/v1/products/search",
            "/api/v1/products",
            "/api/v1/products/featured-products",
            "/api/v1/products/new-products",
            "/api/v1/products/best-seller-products",
            "/api/v1/products/categories/**",
            "/api/v1/products/**",
            "/api/v1/account/forgot-password",
            "/api/v1/account/reset-password",
            "/api/v1/user/cart/checkout/success",
            "/api/v1/user/cart/checkout/cancel",
            "/api/v1/paypal/**"
    );

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        boolean shouldNotFilter = EXCLUDED_PATHS.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
        logger.info("Request path: {}, shouldNotFilter: {}", path, shouldNotFilter);
        return shouldNotFilter;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = getTokenFromRequest(request);

        try {
            if (token != null && jwtProvider.validateToken(token)) {
                // Kiểm tra token có trong danh sách đen không
                if (tokenService.isTokenInvalidated(token)) {
                    sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Token đã bị vô hiệu hóa");
                    return;
                }

                String username = jwtProvider.getUserNameFromToken(token);
                UserDetails userDetails = userDetailService.loadUserByUsername(username);
                if (userDetails != null) {
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            }
        } catch (Exception e) {
            logger.error("Lỗi xác thực token: {}", e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Token không hợp lệ: " + e.getMessage());
            return;
        }

        filterChain.doFilter(request, response);
    }

    public String getTokenFromRequest(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write("{\"message\": \"" + message + "\"}");
    }
}