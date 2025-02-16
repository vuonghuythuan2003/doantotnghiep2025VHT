package ra.doantotnghiep2025.service;


import ra.doantotnghiep2025.exception.CustomerException;
import ra.doantotnghiep2025.model.dto.*;

public interface AuthService {
    UserLoginResponse login(UserLoginRequestDto userLoginRequestDTO) throws CustomerException;
    UserRegisterResponseDTO register(UserRegisterRequestDTO userRegisterDTO) throws CustomerException;
    UserRegisterResponseDTO updatePermission(UserPermissionDTO userPermissionDTO, Long userId) throws CustomerException;
}