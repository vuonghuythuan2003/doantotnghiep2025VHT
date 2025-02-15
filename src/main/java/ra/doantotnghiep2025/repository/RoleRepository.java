package ra.doantotnghiep2025.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ra.doantotnghiep2025.model.entity.Role;

public interface RoleRepository extends JpaRepository<Role,Long> {

    //Tìm vai trò của người dùng trong cơ sở dữ liệu
    Role findRoleByRoleName(String roleName);
}