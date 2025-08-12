package com.rober.bookshop.model.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class BulkRemoveCartRequestDTO {

    @NotEmpty(message = "bookIds must not be empty")
    private List<Long> bookIds;
}


