package ra.doantotnghiep2025.service;

import org.springframework.data.domain.Page;
import ra.doantotnghiep2025.exception.CustomerException;
import ra.doantotnghiep2025.model.dto.UserPermissionDTO;
import ra.doantotnghiep2025.model.dto.UserRegisterResponseDTO;
import ra.doantotnghiep2025.model.dto.UserResponseDTO;

public interface AdminService {
    Page<UserResponseDTO> getUsers(int page, int size, String sortBy, String direction);
    UserRegisterResponseDTO updatePermission(Long userId, Long roleId) throws CustomerException;
    void deleteUserRole(Long userId, Long roleId) throws CustomerException;
    UserResponseDTO toggleUserStatus(Long userId) throws CustomerException;
}

