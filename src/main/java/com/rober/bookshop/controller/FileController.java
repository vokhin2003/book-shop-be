package com.rober.bookshop.controller;

import com.rober.bookshop.annotation.ApiMessage;
import com.rober.bookshop.model.response.FileResponseDTO;
import com.rober.bookshop.service.IFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class FileController {

    private final IFileService fileService;

    @PostMapping("/files")
    @ApiMessage("Upload a single image file")
    public ResponseEntity<FileResponseDTO> upload(@RequestParam("file") MultipartFile file,
                                                  @RequestParam("folder") String folder) {
        FileResponseDTO response = fileService.uploadFile(file, folder);
        return ResponseEntity.ok(response);
    }
}
