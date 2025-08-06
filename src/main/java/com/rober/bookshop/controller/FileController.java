package com.rober.bookshop.controller;

import com.rober.bookshop.annotation.ApiMessage;
import com.rober.bookshop.model.response.FileResponseDTO;
import com.rober.bookshop.service.IFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "File")
public class FileController {

    private final IFileService fileService;

    @PostMapping("/files")
    @ApiMessage("Upload a single image file")
    @Operation(summary = "Upload a file", description = "Upload a single image file to a specific folder and return the file details.")
    public ResponseEntity<FileResponseDTO> upload(@RequestParam("file") MultipartFile file,
                                                  @RequestParam("folder") String folder) {
        FileResponseDTO response = fileService.uploadFile(file, folder);
        return ResponseEntity.ok(response);
    }
}
