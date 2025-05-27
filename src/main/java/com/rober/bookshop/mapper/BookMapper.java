package com.rober.bookshop.mapper;

import com.rober.bookshop.model.entity.Book;
import com.rober.bookshop.model.request.BookRequestDTO;
import com.rober.bookshop.model.response.BookResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface BookMapper {

//    @Mapping(target = "category", ignore = true)
//    Book toBook(BookRequestDTO reqDTO);

    @Mapping(target = "category", ignore = true)
    @Mapping(target = "discount", expression = "java(reqDTO.getDiscount() != null ? reqDTO.getDiscount() : 0)")
    @Mapping(target = "sold", expression = "java(reqDTO.getSold() != null ? reqDTO.getSold() : 0)")
    Book toBook(BookRequestDTO reqDTO);

//    @Mapping(source = "category.name", target = "category")
    BookResponseDTO toResponseDTO(Book book);

    @Mapping(target = "category", ignore = true)
    void updateBookFromDTO(BookRequestDTO reqDTO, @MappingTarget Book book);

}
