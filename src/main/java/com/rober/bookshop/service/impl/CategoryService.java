package com.rober.bookshop.service.impl;

import com.rober.bookshop.exception.BadRequestException;
import com.rober.bookshop.exception.IdInvalidException;
import com.rober.bookshop.mapper.CategoryMapper;
import com.rober.bookshop.model.entity.Book;
import com.rober.bookshop.model.entity.Category;
import com.rober.bookshop.model.request.CategoryRequestDTO;
import com.rober.bookshop.model.response.BookResponseDTO;
import com.rober.bookshop.model.response.CategoryResponseDTO;
import com.rober.bookshop.model.response.ResultPaginationDTO;
import com.rober.bookshop.repository.CategoryRepository;
import com.rober.bookshop.service.ICategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService implements ICategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public CategoryResponseDTO create(CategoryRequestDTO reqDTO) {
        if (this.categoryRepository.existsByName(reqDTO.getName())) {
            throw new IdInvalidException("Category already exists.");
        }

        Category category = this.categoryMapper.toCategory(reqDTO);

        return this.categoryMapper.toResponseDTO(this.categoryRepository.save(category));
    }

    @Override
    public CategoryResponseDTO update(Long id, CategoryRequestDTO reqDTO) {
        Category exisitingCategory = this.categoryRepository.findById(id).orElseThrow(() -> new IdInvalidException("Category with id = " + id + " not found in database"));

        if (!exisitingCategory.getName().equals(reqDTO.getName())) {
            if (this.categoryRepository.existsByName(reqDTO.getName())) {
                throw new IdInvalidException("Category name = '" + reqDTO.getName() + "' already exists.");
            }
        }

        this.categoryMapper.updatedCategoryFromDTO(reqDTO, exisitingCategory);

        return this.categoryMapper.toResponseDTO(this.categoryRepository.save(exisitingCategory));
    }

    @Override
    public void delete(Long id) {
        Category category = this.categoryRepository.findById(id).orElseThrow(() -> new IdInvalidException("Category with id = " + id + " not found"));

        if (category.getBooks() != null && !category.getBooks().isEmpty()) {
            throw new BadRequestException("Cannot delete category because it contains books");
        }

        this.categoryRepository.delete(category);
    }

    @Override
    public ResultPaginationDTO getAll(Specification<Category> spec, Pageable pageable) {
        Page<Category> categoryPage = this.categoryRepository.findAll(spec, pageable);
        ResultPaginationDTO res = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();

        meta.setCurrent(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());

        meta.setPages(categoryPage.getTotalPages());
        meta.setTotal(categoryPage.getTotalElements());

        res.setMeta(meta);

        List<CategoryResponseDTO> listCategory = categoryPage.getContent().stream().map(this.categoryMapper::toResponseDTO).toList();

        res.setResult(listCategory);
        return res;
    }

}
