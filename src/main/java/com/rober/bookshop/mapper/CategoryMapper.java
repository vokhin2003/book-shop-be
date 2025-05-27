package com.rober.bookshop.mapper;

import com.rober.bookshop.model.entity.Category;
import com.rober.bookshop.model.request.CategoryRequestDTO;
import com.rober.bookshop.model.response.CategoryResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    Category toCategory(CategoryRequestDTO dto);
    CategoryResponseDTO toResponseDTO(Category category);
    void updatedCategoryFromDTO(CategoryRequestDTO reqDTO, @MappingTarget Category category);

}
