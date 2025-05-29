package com.rober.bookshop.service.impl;

import com.rober.bookshop.exception.IdInvalidException;
import com.rober.bookshop.exception.InputInvalidException;
import com.rober.bookshop.mapper.CartMapper;
import com.rober.bookshop.model.entity.Book;
import com.rober.bookshop.model.entity.Cart;
import com.rober.bookshop.model.entity.User;
import com.rober.bookshop.model.request.CartRequestDTO;
import com.rober.bookshop.model.response.CartResponseDTO;
import com.rober.bookshop.repository.CartRepository;
import com.rober.bookshop.service.IBookService;
import com.rober.bookshop.service.ICartService;
import com.rober.bookshop.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService implements ICartService {

    private final CartRepository cartRepository;
    private final IUserService userService;
    private final IBookService bookService;
    private final CartMapper cartMapper;

    @Override
    public CartResponseDTO handleAddToCart(CartRequestDTO reqDTO) {
        User user = this.userService.getUserLogin();

        if (user == null) throw new IdInvalidException("User not found in database");
        Book book = this.bookService.getBookEntityById(reqDTO.getBookId());

        Cart cartItem = this.cartRepository.findByUserAndBook(user, book);
        if (cartItem == null) {
            // add new item to cart
            validateQuantity(book, reqDTO.getQuantity());
            Cart newCart = Cart.builder()
                    .user(user)
                    .book(book)
                    .quantity(reqDTO.getQuantity())
                    .build();

            return cartMapper.toResponseDTO(this.cartRepository.save(newCart));
        } else {
            // add quantity to an exist item in cart

            Integer quantity = cartItem.getQuantity() + reqDTO.getQuantity();

            validateQuantity(book, quantity);

            cartItem.setQuantity(quantity);
            return cartMapper.toResponseDTO(this.cartRepository.save(cartItem));
        }
    }

    @Override
    public CartResponseDTO handleUpdateCart(CartRequestDTO reqDTO) {
        User user = this.userService.getUserLogin();
        if (user == null) throw new IdInvalidException("User not found in database");

        Book book = this.bookService.getBookEntityById(reqDTO.getBookId());

        Cart cartItem = this.cartRepository.findByUserAndBook(user, book);
        if (cartItem == null) {
            throw new IdInvalidException("Cart not found in database");
        } else {
            validateQuantity(book, reqDTO.getQuantity());
            cartItem.setQuantity(reqDTO.getQuantity());
            return cartMapper.toResponseDTO(this.cartRepository.save(cartItem));
        }
    }

    @Override
    public List<CartResponseDTO> getAll() {
        User user = this.userService.getUserLogin();
        if (user == null) throw new IdInvalidException("User not found in database");

        List<Cart> carts = this.cartRepository.findAllByUser(user);

        return carts.stream().map(this.cartMapper::toResponseDTO).toList();
    }

    @Override
    @Transactional
    public void handleRemoveItemFromCart(Long bookId) {
        User user = this.userService.getUserLogin();
        if (user == null) throw new IdInvalidException("User not found in database");

        Book book = this.bookService.getBookEntityById(bookId);
        this.cartRepository.deleteByUserAndBook(user, book);
    }

    @Override
    @Transactional
    public void handleClearCart() {
        User user = this.userService.getUserLogin();
        if (user == null) throw new IdInvalidException("User not found in database");

        this.cartRepository.deleteAllByUser(user);
    }

    private void validateQuantity(Book book, Integer reqQuantity) {
        if (book.getQuantity() < reqQuantity) {
            throw new InputInvalidException("Cart quantity exceeds book quantity");
        }
    }


}
