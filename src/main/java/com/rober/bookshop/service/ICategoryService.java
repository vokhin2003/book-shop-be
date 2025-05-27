package com.rober.bookshop.service;

import com.rober.bookshop.model.entity.Category;
import com.rober.bookshop.model.request.CategoryRequestDTO;
import com.rober.bookshop.model.response.CategoryResponseDTO;
import com.rober.bookshop.model.response.ResultPaginationDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface ICategoryService {

    CategoryResponseDTO create(CategoryRequestDTO reqDTO);
    CategoryResponseDTO update(Long id, CategoryRequestDTO reqDTO);
    void delete(Long id);
    ResultPaginationDTO getAll(Specification<Category> spec, Pageable pageable);

}
