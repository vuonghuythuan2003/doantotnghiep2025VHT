package ra.doantotnghiep2025.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import ra.doantotnghiep2025.model.dto.UserPermissionDTO;
import ra.doantotnghiep2025.model.dto.UserRegisterResponseDTO;
import ra.doantotnghiep2025.model.dto.UserResponseDTO;
import ra.doantotnghiep2025.service.AdminService;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {
    @Autowired
    private AdminService adminService;
    @GetMapping("/users")
    public ResponseEntity<Page<UserResponseDTO>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "username") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        return ResponseEntity.ok(adminService.getUsers(page, size, sortBy, direction));
    }

    @PatchMapping("/users/{userId}/role/{roleId}")
    public ResponseEntity<?> updatePermissionUser(@PathVariable Long userId, @PathVariable Long roleId) throws Exception {
        UserRegisterResponseDTO userRegisterResponseDTO = adminService.updatePermission(userId, roleId);
        return ResponseEntity.ok(userRegisterResponseDTO);
    }
    @DeleteMapping("/users/{userId}/role/{roleId}")
    public ResponseEntity<?> deletePermissionUser(@PathVariable Long userId, @PathVariable Long roleId) throws Exception {
        adminService.deleteUserRole(userId, roleId);
        return ResponseEntity.ok("Xóa quyền người dùng thành công");
    }


}
