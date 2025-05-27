package com.rober.bookshop.controller;

import com.rober.bookshop.annotation.ApiMessage;
import com.rober.bookshop.model.entity.Book;
import com.rober.bookshop.model.request.BookRequestDTO;
import com.rober.bookshop.model.response.BookResponseDTO;
import com.rober.bookshop.model.response.ResultPaginationDTO;
import com.rober.bookshop.service.IBookService;
import com.rober.bookshop.validation.BookUpdateValidationGroup;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class BookController {

    private final IBookService bookService;

    @PostMapping("/books")
    @ApiMessage("Create a book")
    @PreAuthorize("hasAuthority('POST:/books')")
    public ResponseEntity<BookResponseDTO> create(@Valid @RequestBody BookRequestDTO reqDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.bookService.createBook(reqDTO));
    }

//    @PutMapping("/books/{id}")
//    @ApiMessage("Update a book")
//    public ResponseEntity<BookResponseDTO> update(@PathVariable("id") Long id, @Valid @RequestBody BookRequestDTO reqDTO) {
//        return ResponseEntity.ok(this.bookService.updateBook(id, reqDTO));
//    }

    @PutMapping("/books/{id}")
    @ApiMessage("Update a book")
    public ResponseEntity<BookResponseDTO> update(
            @PathVariable("id") Long id,
            @Validated(BookUpdateValidationGroup.class) @RequestBody BookRequestDTO reqDTO
    ) {
        return ResponseEntity.ok(this.bookService.updateBook(id, reqDTO));
    }

    @DeleteMapping("/books/{id}")
    @ApiMessage("Delete a book by id")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        this.bookService.deleteBook(id);
        return ResponseEntity.ok(null);
    }

    @GetMapping("/books")
    @ApiMessage("Get book with pagination")
    public ResponseEntity<ResultPaginationDTO> getAll(@Filter Specification<Book> spec, Pageable pageable) {
        return ResponseEntity.ok(this.bookService.getAll(spec, pageable));
    }

    @GetMapping("/books/{id}")
    @ApiMessage("Get book by id")
    public ResponseEntity<BookResponseDTO> getBook(@PathVariable("id") Long id) {
        return ResponseEntity.ok(this.bookService.getBookById(id));
    }

}
