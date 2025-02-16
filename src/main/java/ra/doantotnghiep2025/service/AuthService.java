package ra.doantotnghiep2025.service;


import ra.doantotnghiep2025.exception.CustomerException;
import ra.doantotnghiep2025.model.dto.*;

public interface AuthService {
    UserLoginResponse login(UserLoginRequestDto userLoginRequestDTO);
    UserRegisterResponseDTO register(UserRegisterRequestDTO userRegisterDTO);
    UserRegisterResponseDTO updatePermission(UserPermissionDTO userPermissionDTO, Long userId) throws CustomerException;
}