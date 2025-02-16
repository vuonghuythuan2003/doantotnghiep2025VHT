package ra.doantotnghiep2025.service.imp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import ra.doantotnghiep2025.exception.CustomerException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import ra.doantotnghiep2025.model.dto.*;
import ra.doantotnghiep2025.model.entity.Role;
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

    @Override
    public UserLoginResponse login(UserLoginRequestDto userLoginRequestDTO) {
        Authentication authentication;
        authentication = authenticationProvider.authenticate(new UsernamePasswordAuthenticationToken(
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
    public UserRegisterResponseDTO register(UserRegisterRequestDTO userRegisterDTO) {
        Set<Role> roles = new HashSet<>();
        Role role = roleRepository.findRoleByRoleName("USER");
        roles.add(role);
        User user = User.builder()
                .username(userRegisterDTO.getUsername())
                .password(new BCryptPasswordEncoder().encode(userRegisterDTO.getPassword()))
                .status(true)
                .roles(roles)
                .build();
        User userNew = userRepository.save(user);

        return UserRegisterResponseDTO.builder().username(userNew.getUsername()).build();
    }

    @Override
    public UserRegisterResponseDTO updatePermission(UserPermissionDTO userPermissionDTO, Long userId) throws CustomerException {
        User user = userRepository.findById(userId).orElseThrow(()->new CustomerException("User NOT FOUND"));
        Set<Role> roles = new HashSet<>();
        for (String roleName: userPermissionDTO.getRoleName()) {
            Role role = roleRepository.findRoleByRoleName(roleName);
            roles.add(role);
        }
        // cap nhat vai tro moi
        user.setRoles(roles);
        User userUpdate = userRepository.save(user);
        return UserRegisterResponseDTO.builder()
                .username(userUpdate.getUsername())
                .roles(userUpdate.getRoles())
                .build();
    }
}
