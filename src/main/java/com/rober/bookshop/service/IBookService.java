package com.rober.bookshop.service;

import com.rober.bookshop.model.entity.Book;
import com.rober.bookshop.model.request.BookRequestDTO;
import com.rober.bookshop.model.response.BookResponseDTO;
import com.rober.bookshop.model.response.ResultPaginationDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface IBookService {

    BookResponseDTO createBook(BookRequestDTO reqDTO);

    BookResponseDTO updateBook(Long id, BookRequestDTO reqDTO);
    void deleteBook(Long id);
    ResultPaginationDTO getAll(Specification<Book> spec, Pageable pageable);
    BookResponseDTO getBookById(Long id);

}
