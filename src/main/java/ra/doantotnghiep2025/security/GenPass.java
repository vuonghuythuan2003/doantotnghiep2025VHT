package ra.doantotnghiep2025.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GenPass {
    public static void main(String[] args) {
        System.out.println(new BCryptPasswordEncoder().encode("123456"));
    }
}