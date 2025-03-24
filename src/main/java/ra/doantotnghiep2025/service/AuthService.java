package ra.doantotnghiep2025.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ra.doantotnghiep2025.exception.CustomerException;
import ra.doantotnghiep2025.model.dto.*;

import java.util.List;

public interface AuthService {
    UserLoginResponse login(UserLoginRequestDto userLoginRequestDTO) throws CustomerException;
    UserRegisterResponseDTO register(UserRegisterRequestDTO userRegisterDTO) throws CustomerException;
    UserResponseDTO getUserAccount(Long userId);
    UserResponseDTO updateUserAccount(Long userId, UserUpdateRequestDTO requestDTO);
    void changePassword(Long userId, ChangePasswordRequestDTO requestDTO);
    UserResponseDTO addAddress(Long userId, AddressRequestDTO addressRequestDTO) throws CustomerException;
    void deleteAddress(Long addressId, Long userId) throws CustomerException;
    List<AddressResponseDTO> getUserAddresses(Long userId) throws CustomerException;
    AddressResponseDTO getAddressById(Long addressId) throws CustomerException;
    Long getUserIdByUsername(String username);
    void forgotPassword(ForgotPasswordRequestDTO request) throws CustomerException;
    void resetPassword(ResetPasswordRequestDTO request) throws CustomerException;


}