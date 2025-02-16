    package ra.doantotnghiep2025.configCloudinary;

    import com.cloudinary.Cloudinary;
    import com.cloudinary.utils.ObjectUtils;
    import org.springframework.beans.factory.annotation.Value;
    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.Configuration;

    @Configuration
    public class ConfigCloudinary {
        @Value("${cloudinary.cloud-name}")
        private String cloudName;

        @Value("${cloudinary.api-key}") // Sửa lại tên đúng
        private String apiKey;

        @Value("${cloudinary.api-secret}")
        private String apiSecret;

        @Bean
        public Cloudinary cloudinary() {
            return new Cloudinary(ObjectUtils.asMap(
                    "cloud_name",cloudName,
                    "api_key",apiKey,
                    "api_secret",apiSecret
            ));
        }
    }