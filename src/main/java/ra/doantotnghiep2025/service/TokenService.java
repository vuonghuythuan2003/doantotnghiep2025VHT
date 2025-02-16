package ra.doantotnghiep2025.service;

import org.springframework.stereotype.Service;


public interface TokenService {
    void invalidateToken(String token);
    boolean isTokenInvalidated(String token);
}