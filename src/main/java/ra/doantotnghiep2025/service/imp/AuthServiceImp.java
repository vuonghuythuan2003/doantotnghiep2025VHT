package ra.doantotnghiep2025.service.imp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ra.doantotnghiep2025.exception.CustomerException;
import ra.doantotnghiep2025.model.dto.*;
import ra.doantotnghiep2025.model.entity.*;
import ra.doantotnghiep2025.repository.AddressRepository;
import ra.doantotnghiep2025.repository.PasswordResetTokenRepository;
import ra.doantotnghiep2025.repository.RoleRepository;
import ra.doantotnghiep2025.repository.UserRepository;
import ra.doantotnghiep2025.security.UserPrinciple;
import ra.doantotnghiep2025.security.jwt.JwtProvider;
import ra.doantotnghiep2025.service.AuthService;
import ra.doantotnghiep2025.service.EmailService;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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
    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private EmailService emailService;
    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Override
    public UserLoginResponse login(UserLoginRequestDto userLoginRequestDTO) {
        System.out.println("Đăng nhập với username: " + userLoginRequestDTO.getUsername());
        System.out.println("Mật khẩu gửi lên: " + userLoginRequestDTO.getPassword());

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

    @Override
    public UserResponseDTO getUserAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        return UserResponseDTO.builder()
                .id(user.getId())
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

    @Override
    public UserResponseDTO updateUserAccount(Long userId, UserUpdateRequestDTO requestDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        if (requestDTO.getFullname() != null) user.setFullname(requestDTO.getFullname());
        if (requestDTO.getPhone() != null) user.setPhone(requestDTO.getPhone());
        if (requestDTO.getAvatar() != null) user.setAvatar(requestDTO.getAvatar());
        if (requestDTO.getAddress() != null) user.setAddress(requestDTO.getAddress());

        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        return UserResponseDTO.builder()
                .id(user.getId())
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

    @Override
    public void changePassword(Long userId, ChangePasswordRequestDTO requestDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        // Kiểm tra mật khẩu cũ
        if (!passwordEncoder.matches(requestDTO.getOldPass(), user.getPassword())) {
            throw new RuntimeException("Mật khẩu cũ không chính xác");
        }

        // Kiểm tra mật khẩu mới và xác nhận
        if (!requestDTO.getNewPass().equals(requestDTO.getConfirmNewPass())) {
            throw new RuntimeException("Mật khẩu xác nhận không khớp");
        }

        // Cập nhật mật khẩu mới
        user.setPassword(passwordEncoder.encode(requestDTO.getNewPass()));
        userRepository.save(user);
    }

    @Override
    public UserResponseDTO addAddress(Long userId, AddressRequestDTO addressRequestDTO) throws CustomerException {
        // Kiểm tra xem user có tồn tại không
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomerException("Người dùng không tồn tại"));

        // Tạo mới địa chỉ
        Address newAddress = Address.builder()
                .street(addressRequestDTO.getStreet())
                .city(addressRequestDTO.getCity())
                .state(addressRequestDTO.getState())
                .zipCode(addressRequestDTO.getZipCode())
                .country(addressRequestDTO.getCountry())
                .user(user)
                .build();

        // Lưu địa chỉ mới vào database
        addressRepository.save(newAddress);

        // Lấy lại danh sách địa chỉ của user sau khi thêm mới
        List<String> userAddresses = user.getAddresses().stream()
                .map(address -> String.format("%s, %s, %s, %s, %s",
                        address.getStreet(), address.getCity(), address.getState(),
                        address.getZipCode(), address.getCountry()))
                .toList();

        // Trả về DTO với danh sách địa chỉ mới
        return UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullname(user.getFullname())
                .address(userAddresses.toString()) // Chuyển danh sách địa chỉ thành chuỗi
                .build();
    }


    @Override

    public void deleteAddress(Long addressId, Long userId) throws CustomerException{
        // Kiểm tra user có tồn tại không
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomerException("Người dùng không tồn tại"));

        // Kiểm tra địa chỉ có tồn tại và thuộc về user không
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new CustomerException("Địa chỉ không tồn tại"));

        if (!address.getUser().getId().equals(userId)) {
            throw new CustomerException("Bạn không có quyền xoá địa chỉ này");
        }

        // Xoá địa chỉ
        addressRepository.delete(address);
    }

    @Override
    public List<AddressResponseDTO> getUserAddresses(Long userId) throws CustomerException{
        // Kiểm tra user có tồn tại không
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomerException("Người dùng không tồn tại"));

        // Lấy danh sách địa chỉ của user
        List<Address> addresses = addressRepository.findByUser(user);

        // Chuyển đổi danh sách sang DTO
        return addresses.stream().map(address -> AddressResponseDTO.builder()
                .id(address.getId())
                .street(address.getStreet())
                .city(address.getCity())
                .state(address.getState())
                .zipCode(address.getZipCode())
                .country(address.getCountry())
                .build()).collect(Collectors.toList());
    }

    @Override
    public AddressResponseDTO getAddressById(Long addressId) throws CustomerException {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new CustomerException("Địa chỉ không tồn tại"));

        return AddressResponseDTO.builder()
                .id(address.getId())
                .street(address.getStreet())
                .city(address.getCity())
                .state(address.getState())
                .zipCode(address.getZipCode())
                .country(address.getCountry())
                .build();
    }
    @Override
    public Long getUserIdByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        System.out.println("Đã tìm thấy người dùng: " + user.getUsername() + ", ID: " + user.getId()); // Debug

        return user.getId();
    }

    @Override
    public void forgotPassword(ForgotPasswordRequestDTO request) throws CustomerException {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomerException("Không tìm thấy người dùng với email: " + request.getEmail()));

        String token = UUID.randomUUID().toString();
        // Sử dụng Builder để tạo đối tượng
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusHours(1))
                .build();
        passwordResetTokenRepository.save(resetToken);

        String subject = "Khôi phục mật khẩu";
        String text = "Chào " + user.getFullname() + ",\n\n" +
                "Bạn đã yêu cầu khôi phục mật khẩu. Vui lòng sử dụng mã xác nhận dưới đây để đặt lại mật khẩu:\n" +
                "Mã xác nhận: " + token + "\n" +
                "Mã này sẽ hết hạn sau 1 giờ.\n\n" +
                "Trân trọng,\nCửa hàng đồng hồ";
        emailService.sendSimpleEmail(user.getEmail(), subject, text);
    }
    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequestDTO request) throws CustomerException {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new CustomerException("Mã xác nhận không hợp lệ"));

        if (resetToken.isExpired()) {
            throw new CustomerException("Mật khẩu xác nhận đã hết hạn");
        }

        User user = resetToken.getUser();
        String newPassword = request.getNewPassword();
        String encodedPassword = passwordEncoder.encode(newPassword);
        System.out.println("Đặt lại mật khẩu cho user: " + user.getUsername());
        System.out.println("Mật khẩu mới trước khi mã hóa: " + newPassword);
        System.out.println("Mật khẩu mới sau khi mã hóa: " + encodedPassword);
        user.setPassword(encodedPassword);
        userRepository.save(user);
        System.out.println("Mật khẩu đã được lưu vào cơ sở dữ liệu cho người dùng: " + user.getUsername());

        passwordResetTokenRepository.delete(resetToken);
    }


}
