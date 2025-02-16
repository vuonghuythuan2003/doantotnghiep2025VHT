package ra.doantotnghiep2025.service.imp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ra.doantotnghiep2025.model.entity.TokenBlackList;
import ra.doantotnghiep2025.repository.TokenRepository;
import ra.doantotnghiep2025.service.TokenService;

@Service
public class TokenServiceImp implements TokenService {
    @Autowired
    private TokenRepository tokenRepository;
    // vo hieu hoa 1 token bang cach them vao bang black_lits
    @Override
    public void invalidateToken(String token) {
        TokenBlackList tokenBlackList = new TokenBlackList();
        tokenBlackList.setToken(token);
        tokenBlackList.setInvalid(true);
        tokenRepository.save(tokenBlackList);
    }
    // kiem tra 1 token co trong black_list
    @Override
    public boolean isTokenInvalidated(String token) {
        return tokenRepository.existsByToken(token);
    }
}