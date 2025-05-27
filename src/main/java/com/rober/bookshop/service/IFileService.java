package com.rober.bookshop.service;


import com.rober.bookshop.model.response.FileResponseDTO;
import org.springframework.web.multipart.MultipartFile;

public interface IFileService {
    FileResponseDTO uploadFile(MultipartFile file, String folderName);
}
