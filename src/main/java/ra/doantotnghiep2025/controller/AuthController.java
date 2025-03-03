package ra.doantotnghiep2025.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ra.doantotnghiep2025.exception.CustomerException;
import ra.doantotnghiep2025.model.dto.*;
import ra.doantotnghiep2025.service.AuthService;
import ra.doantotnghiep2025.service.TokenService;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    @Autowired
    private AuthService authService;
    @Autowired
    private TokenService tokenService;

    @PostMapping("/sign-up")
    public ResponseEntity<?> register(@ModelAttribute UserRegisterRequestDTO requestDTO) throws CustomerException {
        UserRegisterResponseDTO responseDTO = authService.register(requestDTO);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    @PostMapping("/sign-in")
    public ResponseEntity<?> login(@ModelAttribute UserLoginRequestDto requestDTO) throws CustomerException{
        UserLoginResponse responseDTO = authService.login(requestDTO);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }
    @GetMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request){
        String token = request.getHeader("Authorization");
        if(token != null & token.startsWith("Bearer ")){
            token = token.substring(7);
            tokenService.invalidateToken(token);
        }
        return ResponseEntity.ok("Đăng xuất");
    }

}
