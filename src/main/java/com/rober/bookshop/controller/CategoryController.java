package com.rober.bookshop.controller;

import com.rober.bookshop.annotation.ApiMessage;
import com.rober.bookshop.model.entity.Category;
import com.rober.bookshop.model.request.CategoryRequestDTO;
import com.rober.bookshop.model.response.CategoryResponseDTO;
import com.rober.bookshop.model.response.ResultPaginationDTO;
import com.rober.bookshop.service.ICategoryService;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CategoryController {

    private final ICategoryService categoryService;

    @PostMapping("/categories")
    @ApiMessage("Create a category")
    public ResponseEntity<CategoryResponseDTO> create(@Valid @RequestBody CategoryRequestDTO reqDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.categoryService.create(reqDTO));
    }

    @PutMapping("/categories/{id}")
    @ApiMessage("Update a category")
    public ResponseEntity<CategoryResponseDTO> update(@PathVariable("id") Long id, @Valid @RequestBody CategoryRequestDTO reqDTO) {
        return ResponseEntity.ok(this.categoryService.update(id, reqDTO));
    }

    @DeleteMapping("/categories/{id}")
    @ApiMessage("Delete a category")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        this.categoryService.delete(id);
        return ResponseEntity.ok(null);
    }

    @GetMapping("/categories")
    @ApiMessage("Get category with pagination")
    public ResponseEntity<ResultPaginationDTO> getAll(@Filter Specification<Category> spec, Pageable pageable) {
        return ResponseEntity.ok(this.categoryService.getAll(spec, pageable));
    }


}
