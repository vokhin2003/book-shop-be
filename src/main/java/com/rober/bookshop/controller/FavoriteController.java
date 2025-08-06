package com.rober.bookshop.controller;

import com.rober.bookshop.annotation.ApiMessage;
import com.rober.bookshop.model.entity.Favorite;
import com.rober.bookshop.model.request.FavoriteRequestDTO;
import com.rober.bookshop.model.response.FavoriteResponseDTO;
import com.rober.bookshop.model.response.ResultPaginationDTO;
import com.rober.bookshop.service.IFavoriteService;
import com.turkraft.springfilter.boot.Filter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Favorite")
public class FavoriteController {

    private final IFavoriteService favoriteService;


    @PostMapping("/favorites")
    @ApiMessage("Created a favorite")
    @Operation(summary = "Create a favorite", description = "Mark a book is favorite for an user.")
    public ResponseEntity<FavoriteResponseDTO> create(@Valid @RequestBody FavoriteRequestDTO reqDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.favoriteService.createFavorite(reqDTO));
    }

    @DeleteMapping("/favorites/{id}")
    @ApiMessage("Deleted a favorite")
    @Operation(summary = "Delete a favorite", description = "Unmark a book is favorite for an user.")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(this.favoriteService.deleteFavorite(id));
    }

    @GetMapping("/favorites-user-book/{bookId}")
    @ApiMessage("Got favorite by user and book")
    @Operation(summary = "Get a favorite", description = "Get a favorite by user id and book id.")
    public ResponseEntity<FavoriteResponseDTO> getByUserAndBook(@PathVariable("bookId") Long bookId) {
        return ResponseEntity.status(HttpStatus.OK).body(this.favoriteService.getByUserAndBook(bookId));
    }

    @GetMapping("/favorites")
    @ApiMessage("Get all favorite")
    @Operation(summary = "Get all favorite with pagination", description = "Get a paginated list of favorites with optional filtering.")
    public ResponseEntity<ResultPaginationDTO> getAll(@Filter Specification<Favorite> spec, Pageable pageable) {
        return ResponseEntity.ok(this.favoriteService.getAll(spec, pageable));
    }

}
