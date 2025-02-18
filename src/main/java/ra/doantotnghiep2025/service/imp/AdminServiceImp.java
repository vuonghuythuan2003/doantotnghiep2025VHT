package ra.doantotnghiep2025.service.imp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ra.doantotnghiep2025.exception.CustomerException;
import ra.doantotnghiep2025.model.dto.UserRegisterResponseDTO;
import ra.doantotnghiep2025.model.dto.UserResponseDTO;
import ra.doantotnghiep2025.model.entity.Role;
import ra.doantotnghiep2025.model.entity.User;
import ra.doantotnghiep2025.repository.RoleRepository;
import ra.doantotnghiep2025.repository.UserRepository;
import ra.doantotnghiep2025.service.AdminService;

import java.util.Optional;


@Service

public class AdminServiceImp implements AdminService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    
    
    @Override
    public Page<UserResponseDTO> getUsers(int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return userRepository.findAll(pageable).map(this::convertToDto);
    }
    @Override
    public UserRegisterResponseDTO updatePermission(Long userId, Long roleId) throws CustomerException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomerException("User NOT FOUND"));
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new CustomerException("Role NOT FOUND"));
        if (!user.getRoles().contains(role)) {
            user.getRoles().add(role);
            userRepository.save(user);
        }
        return UserRegisterResponseDTO.builder()
                .username(user.getUsername())
                .roles(user.getRoles())
                .build();
    }


    @Override
    public void deleteUserRole(Long userId, Long roleId) throws CustomerException {
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomerException("Người dùng không tồn tại với ID này"));

        Role role = roleRepository.findById(roleId).orElseThrow(() -> new CustomerException("Người dùng không có quyền"));
        if(!user.getRoles().contains(role)) {
            throw new CustomerException("Người dùng không có quyền này chọn cái khác");
        }
        user.getRoles().remove(role);
        userRepository.save(user);
    }

    @Override
    public UserResponseDTO toggleUserStatus(Long userId) throws CustomerException {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            throw new CustomerException("Người dùng không tồn tại!");
        }

        User user = optionalUser.get();
        user.setStatus(!user.getStatus()); // Đảo trạng thái khóa/mở khóa
        userRepository.save(user);

        return UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullname(user.getFullname())
                .status(user.getStatus())
                .build();
    }

    private UserResponseDTO convertToDto(User user) {
        return UserResponseDTO.builder().id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullname(user.getFullname())
                .status(user.getStatus())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .address(user.getAddress())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
    
}
