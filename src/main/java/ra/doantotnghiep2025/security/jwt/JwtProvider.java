package ra.doantotnghiep2025.security.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.expression.ExpressionException;
import org.springframework.stereotype.Component;
import ra.doantotnghiep2025.security.UserPrinciple;

import java.util.Date;


@Component
public class JwtProvider {

    //Tạo JWT token khi người dùng đăng nhập thành công.
    //Xác thực JWT token khi có request gửi lên.
    //Lấy username từ token để xác thực người dùng.
    // Lớp này liên quan đến Token
    @Value("${SECRET_KEY}")
    private String SECRET_KEY;
    @Value("${EXPIRED}")
    private Long EXPIRED;
    private Logger logger = LoggerFactory.getLogger(JwtProvider.class);

    public String generateToken(UserPrinciple userPrinciple) {
        //  Lấy thời gian hiện tại (tính bằng mili-giây) Cộng thêm khoảng thời gian EXPIRED
        //  Chuyển giá trị thành một đối tượng Date, tức là ngày hết hạn của token
        Date dateExpiration = new Date(new Date().getTime() + EXPIRED);

        return Jwts.builder() // để tạo token mới.
                .setSubject(userPrinciple.getUsername()) //  Lưu trữ danh tính người dùng (thường là username)
                .signWith(SignatureAlgorithm.HS512, SECRET_KEY)  // Dùng thuật toán HS512 để ký JWT,
                // SECRET_KEY → Khóa bí mật để đảm bảo chỉ server có thể xác minh JWT.
                .setExpiration(dateExpiration) // Giới hạn thời gian sử dụng JWT, sau thời gian này token sẽ không hợp lệ
                .compact(); // compact() → Hoàn thành việc tạo token và chuyển thành chuỗi JWT để gửi cho client.
    }

    public Boolean validateToken(String token) {
        try {
            Jwts.parser() //Jwts.parser() → Khởi tạo một trình phân tích JWT.
                    .setSigningKey(SECRET_KEY) //setSigningKey(SECRET_KEY) → Thiết lập khóa bí mật để kiểm tra tính hợp lệ.
                    .parseClaimsJws(token); //Giải mã token
            return true;
        }  //ExpiredJwtException → Token đã hết hạn.
        // MalformedJwtException → Token bị sai định dạng hoặc không hợp lệ.//
        // ExpressionException → Lỗi biểu thức (có thể xảy ra nếu chuỗi JWT bị sai cú pháp)
        catch (ExpressionException | ExpiredJwtException | MalformedJwtException exception) {
            logger.error(exception.getMessage());
        }
        return false;
    }

    public String getUserNameFromToken(String token) {
        return Jwts.parser() //Tạo một bộ phân tích JWT.
                .setSigningKey(SECRET_KEY) //Sử dụng khóa bí mật để giải mã token và kiểm tra tính hợp lệ.
                .parseClaimsJws(token).getBody().getSubject(); //Giải mã token và lấy thông tin bên trong (nếu token hợp lệ).
    }
}
