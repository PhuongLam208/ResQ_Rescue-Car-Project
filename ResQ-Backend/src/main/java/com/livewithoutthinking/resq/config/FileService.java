package com.livewithoutthinking.resq.config;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class FileService {

    private static final String UPLOAD_DIRECTORY = "/path/to/uploads/pdfs/";

    public String savePdfFile(MultipartFile file) throws IOException {

        File dir = new File(UPLOAD_DIRECTORY);
        if (!dir.exists()) {
            dir.mkdirs();
        }


        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(UPLOAD_DIRECTORY, fileName);


        FileUtils.writeByteArrayToFile(filePath.toFile(), file.getBytes());
        return fileName;
    }
}

