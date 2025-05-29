package com.rober.bookshop.service;

import com.rober.bookshop.model.request.CartRequestDTO;
import com.rober.bookshop.model.response.CartResponseDTO;

import java.util.List;

public interface ICartService {

    CartResponseDTO handleAddToCart(CartRequestDTO reqDTO);
    CartResponseDTO handleUpdateCart(CartRequestDTO reqDTO);
    List<CartResponseDTO> getAll();
    void handleRemoveItemFromCart(Long bookId);
    void handleClearCart();

}
