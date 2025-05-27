package com.rober.bookshop.service.impl;

import com.rober.bookshop.exception.IdInvalidException;
import com.rober.bookshop.exception.InputInvalidException;
import com.rober.bookshop.mapper.BookMapper;
import com.rober.bookshop.model.entity.Book;
import com.rober.bookshop.model.entity.Category;
import com.rober.bookshop.model.entity.Role;
import com.rober.bookshop.model.request.BookRequestDTO;
import com.rober.bookshop.model.response.BookResponseDTO;
import com.rober.bookshop.model.response.ResultPaginationDTO;
import com.rober.bookshop.model.response.RoleResponseDTO;
import com.rober.bookshop.repository.BookRepository;
import com.rober.bookshop.repository.CategoryRepository;
import com.rober.bookshop.service.IBookService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookService implements IBookService {


    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final BookMapper bookMapper;

    @Override
    public BookResponseDTO createBook(BookRequestDTO reqDTO) {

        if (reqDTO.getDiscount() != null && (reqDTO.getDiscount() < 0 || reqDTO.getDiscount() > 100)) {
            throw new InputInvalidException("Discount must be between 0 and 100");
        }
        if (reqDTO.getSold() != null && reqDTO.getSold() < 0) {
            throw new InputInvalidException("Sold must be at least 0");
        }

//        Category category = categoryRepository.findByName(reqDTO.getCategory())
//                .orElseGet(() -> {
//                    Category newCategory = new Category();
//                    newCategory.setName(reqDTO.getCategory());
//                    return categoryRepository.save(newCategory);
//                });

        Category category = categoryRepository.findById(reqDTO.getCategory()).orElseThrow(() -> new IdInvalidException("Category with id = " + reqDTO.getCategory() + " not found in database"));


        Book book = bookMapper.toBook(reqDTO);
        book.setCategory(category);

        Book savedBook = this.bookRepository.save(book);

        return bookMapper.toResponseDTO(savedBook);
    }

    @Transactional
    @Override
    public BookResponseDTO updateBook(Long id, BookRequestDTO reqDTO) {

//        if (reqDTO.getDiscount() < 0 || reqDTO.getDiscount() > 100) {
//            throw new InputInvalidException("Discount must be between 0 and 100");
//        }
//        if (reqDTO.getSold() < 0) {
//            throw new InputInvalidException("Sold must be at least 0");
//        }

        Book existingBook = this.bookRepository.findById(id).orElseThrow(() -> new IdInvalidException("Book is not found in database"));

        bookMapper.updateBookFromDTO(reqDTO, existingBook);
        Category category = this.categoryRepository.findById(reqDTO.getCategory()).orElseThrow(
                () -> new IdInvalidException("Category with id = " + reqDTO.getCategory() + " not found in database")
        );

        existingBook.setCategory(category);

        this.bookRepository.save(existingBook);
        return bookMapper.toResponseDTO(existingBook);
    }

    @Transactional
    @Override
    public void deleteBook(Long id) {
        Book book = this.bookRepository.findById(id).orElseThrow(() -> new IdInvalidException("Book with id " + id + " not found in database"));
        this.bookRepository.delete(book);
    }

    @Override
    public ResultPaginationDTO getAll(Specification<Book> spec, Pageable pageable) {
        Page<Book> bookPage = this.bookRepository.findAll(spec, pageable);
        ResultPaginationDTO res = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();

        meta.setCurrent(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());

        meta.setPages(bookPage.getTotalPages());
        meta.setTotal(bookPage.getTotalElements());

        res.setMeta(meta);

        List<BookResponseDTO> listBook = bookPage.getContent().stream().map(this.bookMapper::toResponseDTO).toList();

        res.setResult(listBook);
        return res;
    }

    @Override
    public BookResponseDTO getBookById(Long id) {
        Book book = this.bookRepository.findById(id).orElseThrow(() -> new IdInvalidException("Book with id = " + id + " not found"));
        return this.bookMapper.toResponseDTO(book);
    }

}
