package ra.doantotnghiep2025.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UploadFileService {
    private final Cloudinary cloudinary;
    public String uploadFile(MultipartFile file) {
        // lay file goc
        String originalFilename = file.getOriginalFilename();
        if (originalFilename !=null && originalFilename.contains(".")) {
            originalFilename = originalFilename.substring(0,originalFilename.lastIndexOf("."));
        }
        //Them ten file vao tham so upload
        Map uploadParams = ObjectUtils.asMap(
                "public_id",originalFilename
        );
        // up len cloudinary
        try {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(),uploadParams);
            return uploadResult.get("url").toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}