package com.rober.bookshop.model.response;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileResponseDTO {
    private String url;
    private String fileName;
    private Instant uploadedAt;
}

