package com.rober.bookshop.service;

import com.rober.bookshop.model.entity.Favorite;
import com.rober.bookshop.model.request.FavoriteRequestDTO;
import com.rober.bookshop.model.response.FavoriteResponseDTO;
import com.rober.bookshop.model.response.ResultPaginationDTO;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface IFavoriteService {
    FavoriteResponseDTO createFavorite(@Valid FavoriteRequestDTO reqDTO);

    Void deleteFavorite(Long id);

    FavoriteResponseDTO getByUserAndBook(@Valid FavoriteRequestDTO reqDTO);

    ResultPaginationDTO getAll(Specification<Favorite> spec, Pageable pageable);
}
