package ra.doantotnghiep2025.service.imp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ra.doantotnghiep2025.exception.CustomerException;
import ra.doantotnghiep2025.model.dto.*;
import ra.doantotnghiep2025.model.entity.Role;
import ra.doantotnghiep2025.model.entity.RoleType;
import ra.doantotnghiep2025.model.entity.User;
import ra.doantotnghiep2025.repository.RoleRepository;
import ra.doantotnghiep2025.repository.UserRepository;
import ra.doantotnghiep2025.security.UserPrinciple;
import ra.doantotnghiep2025.security.jwt.JwtProvider;
import ra.doantotnghiep2025.service.AuthService;

import java.util.HashSet;
import java.util.Set;

@Service
public class AuthServiceImp implements AuthService {

    @Autowired
    private AuthenticationProvider authenticationProvider;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtProvider jwtProvider;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserLoginResponse login(UserLoginRequestDto userLoginRequestDTO) {
        Authentication authentication = authenticationProvider.authenticate(
                new UsernamePasswordAuthenticationToken(
                        userLoginRequestDTO.getUsername(),
                        userLoginRequestDTO.getPassword())
        );

        UserPrinciple userPrinciple = (UserPrinciple) authentication.getPrincipal();
        return UserLoginResponse.builder()
                .username(userPrinciple.getUsername())
                .typeToken("Bearer Token")
                .accessToken(jwtProvider.generateToken(userPrinciple))
                .roles(userPrinciple.getUser().getRoles())
                .build();
    }

    @Override
    public UserRegisterResponseDTO register(UserRegisterRequestDTO userRegisterDTO) throws CustomerException {
        if (userRegisterDTO.getUsername() == null || userRegisterDTO.getPassword() == null) {
            throw new CustomerException("Tên đăng nhập và mật khẩu không được để trống");
        }

        if (userRepository.existsByUsername(userRegisterDTO.getUsername())) {
            throw new CustomerException("Tên người dùng đã tồn tại");
        }

        Role role = roleRepository.findRoleByRoleName(RoleType.USER);
        if (role == null) {
            throw new CustomerException("Không tìm thấy vai trò NGƯỜI DÙNG trong cơ sở dữ liệu");
        }

        Set<Role> roles = new HashSet<>();
        roles.add(role);

        String encodedPassword = passwordEncoder.encode(userRegisterDTO.getPassword());

        User user = User.builder()
                .username(userRegisterDTO.getUsername())
                .password(encodedPassword)
                .status(true)
                .roles(roles)
                .isDeleted(false)
                .email(userRegisterDTO.getEmail())
                .fullname(userRegisterDTO.getFullName())
                .phone(userRegisterDTO.getPhoneNumber())
                .address(userRegisterDTO.getAddress() != null ? userRegisterDTO.getAddress() : "Chưa cập nhật")
                .build();

        User userNew = userRepository.save(user);

        return UserRegisterResponseDTO.builder()
                .username(userNew.getUsername())
                .roles(userNew.getRoles())
                .build();
    }

}
