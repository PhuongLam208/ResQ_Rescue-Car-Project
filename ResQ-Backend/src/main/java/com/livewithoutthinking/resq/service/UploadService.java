
package com.livewithoutthinking.resq.service;

import com.livewithoutthinking.resq.util.AESEncryptionUtil;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class UploadService {

    private final String uploadDir = System.getProperty("user.dir") + "/secure_uploads";

    public String saveEncryptedFile(MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) return null;

        String extension = FilenameUtils.getExtension(file.getOriginalFilename());
        String rawFilename = UUID.randomUUID() + "." + extension;

        //  Tạo full path tuyệt đối
        Path fullPath = Paths.get(uploadDir, rawFilename);

        //  Đảm bảo thư mục tồn tại
        Files.createDirectories(fullPath.getParent());

        //  Ghi file
        file.transferTo(fullPath.toFile());

        //  Encrypt phần tên file thôi (không cả path)
        String encryptedFilename = AESEncryptionUtil.encrypt(rawFilename);

        return "secure_uploads/" + encryptedFilename;
    }
    public byte[] readAndDecryptFile(String path) throws Exception {
        final String prefix = "secure_uploads/";
        if (!path.startsWith(prefix)) {
            throw new IllegalArgumentException("Invalid secure path");
        }

        String filename = path.substring(prefix.length());
        Path filePath = Paths.get(uploadDir, filename);

        if (!Files.exists(filePath)) {
            throw new IllegalArgumentException("File not found: " + filePath);
        }

        return Files.readAllBytes(filePath);
    }

}
