package fsa.training.travelee.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@RestController
public class UploadCKEditorContoller {

    @PostMapping("/api/upload-image")
    public Map<String, Object> uploadImage(@RequestParam("upload") MultipartFile file) {
        try {
            String uploadDir = System.getProperty("user.dir") + "/uploads";
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(uploadDir, fileName);

            Files.createDirectories(filePath.getParent());
            file.transferTo(filePath.toFile());

            String imageUrl = "/uploads/" + fileName;
            return Map.of("url", imageUrl);
        } catch (IOException e) {
            return Map.of("error", Map.of("message", "Upload thất bại: " + e.getMessage()));
        }
    }
}
