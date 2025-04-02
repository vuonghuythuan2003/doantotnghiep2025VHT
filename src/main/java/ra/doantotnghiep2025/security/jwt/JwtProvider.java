package ra.doantotnghiep2025.security.jwt;

import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ra.doantotnghiep2025.security.UserPrinciple;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtProvider {

    @Value("${SECRET_KEY}")
    private String SECRET_KEY;

    @Value("${EXPIRED}")
    private Long EXPIRED;

    private Logger logger = LoggerFactory.getLogger(JwtProvider.class);

    public String generateToken(UserPrinciple userPrinciple) {
        Date dateExpiration = new Date(new Date().getTime() + EXPIRED);

        return Jwts.builder()
                .setSubject(userPrinciple.getUsername())
                .claim("roles", userPrinciple.getAuthorities().stream()
                        .map(grantedAuthority -> grantedAuthority.getAuthority())
                        .collect(Collectors.toList())) // Thêm thông tin vai trò vào token
                .signWith(SignatureAlgorithm.HS512, SECRET_KEY)
                .setExpiration(dateExpiration)
                .compact();
    }

    public Boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            logger.error("Token đã hết hạn: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("Token không hợp lệ: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Lỗi khi xác thực token: {}", e.getMessage());
        }
        return false;
    }

    public String getUserNameFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // Thêm phương thức để lấy vai trò từ token
    public List<String> getRolesFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
        return claims.get("roles", List.class);
    }
}