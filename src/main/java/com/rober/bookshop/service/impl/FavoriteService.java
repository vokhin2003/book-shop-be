package com.rober.bookshop.service.impl;

import com.rober.bookshop.exception.InputInvalidException;
import com.rober.bookshop.mapper.FavoriteMapper;
import com.rober.bookshop.model.entity.Book;
import com.rober.bookshop.model.entity.Favorite;
import com.rober.bookshop.model.entity.User;
import com.rober.bookshop.model.request.FavoriteRequestDTO;
import com.rober.bookshop.model.response.FavoriteResponseDTO;
import com.rober.bookshop.model.response.ResultPaginationDTO;
import com.rober.bookshop.repository.FavoriteRepository;
import com.rober.bookshop.service.IBookService;
import com.rober.bookshop.service.IFavoriteService;
import com.rober.bookshop.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteService implements IFavoriteService {

    private final IUserService userService;
    private final IBookService bookService;
    private final FavoriteRepository favoriteRepository;
    private final FavoriteMapper mapper;

    @Override
    public FavoriteResponseDTO createFavorite(FavoriteRequestDTO reqDTO) {

        User favUser = userService.getUserLogin();

        Book favBook = bookService.getBookEntityById(reqDTO.getBookId());

        if (favUser != null && favBook != null) {
            Favorite favorite = new Favorite();
            favorite.setBook(favBook);
            favorite.setUser(favUser);

            return mapper.toResponse(favoriteRepository.save(favorite));
        }

        throw new InputInvalidException("User or Book not found");

    }

    @Override
    @Transactional
    public Void deleteFavorite(Long id) {
        favoriteRepository.deleteFavoriteById(id);
        return null;
    }

    @Override
    public FavoriteResponseDTO getByUserAndBook(FavoriteRequestDTO reqDTO) {
        User favUser = userService.getUserLogin();
        Favorite favorite = favoriteRepository.findFavoriteByUserIdAndBookId(favUser.getId(), reqDTO.getBookId());

        if (favorite != null) {
            return mapper.toResponse(favorite);
        }
        return null;
    }

    @Override
    public ResultPaginationDTO getAll(Specification<Favorite> spec, Pageable pageable) {
        Page<Favorite> favoritePage = this.favoriteRepository.findAll(spec, pageable);
        ResultPaginationDTO res = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();

        meta.setCurrent(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());

        meta.setPages(favoritePage.getTotalPages());
        meta.setTotal(favoritePage.getTotalElements());

        res.setMeta(meta);

        List<FavoriteResponseDTO> listFavorite = favoritePage.getContent().stream().map(this.mapper::toResponse).toList();

        res.setResult(listFavorite);
        return res;
    }
}
