package ra.doantotnghiep2025.service;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ra.doantotnghiep2025.exception.CustomerException;
import ra.doantotnghiep2025.model.dto.*;

public interface AuthService {
    UserLoginResponse login(UserLoginRequestDto userLoginRequestDTO) throws CustomerException;
    UserRegisterResponseDTO register(UserRegisterRequestDTO userRegisterDTO) throws CustomerException;

}