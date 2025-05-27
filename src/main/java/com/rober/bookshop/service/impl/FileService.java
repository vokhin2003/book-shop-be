package com.rober.bookshop.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.rober.bookshop.exception.FileUploadException;
import com.rober.bookshop.model.response.FileResponseDTO;
import com.rober.bookshop.service.IFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileService implements IFileService {

    private final Cloudinary cloudinary;

    @Override
    public FileResponseDTO uploadFile(MultipartFile file, String folderName) {
        String originalFilename = file.getOriginalFilename();

        if (originalFilename == null || originalFilename.isBlank()) {
            throw new FileUploadException("File name is invalid.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new FileUploadException("Only image files are allowed.");
        }

        int dotIndex = originalFilename.lastIndexOf('.');
        String baseFileName = (dotIndex != -1) ? originalFilename.substring(0, dotIndex) : originalFilename;
        String uniqueFileName = baseFileName + "_" + System.currentTimeMillis();

        try {
            Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", folderName,
                    "public_id", uniqueFileName,
                    "overwrite", true
            ));

            String url = (String) uploadResult.get("secure_url");

            return FileResponseDTO.builder()
                    .url(url)
                    .fileName(uniqueFileName)
                    .uploadedAt(Instant.now())
                    .build();

        } catch (IOException e) {
            log.error("Upload failed: {}", e.getMessage(), e);
            throw new FileUploadException("Failed to upload file to Cloudinary.", e);
        }
    }
}
