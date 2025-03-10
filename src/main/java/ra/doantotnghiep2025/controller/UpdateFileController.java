package ra.doantotnghiep2025.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ra.doantotnghiep2025.service.UploadFileService;

@RestController
@RequestMapping("/api/v1/upload")
public class UpdateFileController {
    @Autowired
    private UploadFileService uploadFileService;
    @PostMapping("")
    public ResponseEntity<String> upload(@RequestParam MultipartFile file) {
        String fileName = uploadFileService.uploadFile(file);
        return new ResponseEntity<>(fileName, HttpStatus.CREATED);
    }
}