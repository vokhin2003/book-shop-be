package com.rober.bookshop.controller;

import com.rober.bookshop.annotation.ApiMessage;
import com.rober.bookshop.model.request.CartRequestDTO;
import com.rober.bookshop.model.response.CartResponseDTO;
import com.rober.bookshop.service.ICartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CartController {

    private final ICartService cartService;

    @PostMapping("/carts")
    @ApiMessage("User add to card")
    public ResponseEntity<CartResponseDTO> addToCart(@RequestBody @Valid CartRequestDTO reqDTO) {
        return ResponseEntity.ok(this.cartService.handleAddToCart(reqDTO));
    }

    @PutMapping("/carts")
    @ApiMessage("User update quantity in cart")
    public ResponseEntity<CartResponseDTO> update(@RequestBody @Valid CartRequestDTO reqDTO) {
        return ResponseEntity.ok(this.cartService.handleUpdateCart(reqDTO));
    }

    @GetMapping("/carts")
    @ApiMessage("User get all cart item in cart")
    public ResponseEntity<List<CartResponseDTO>> getAll() {
        return ResponseEntity.ok(this.cartService.getAll());
    }

    @DeleteMapping("/carts/{bookId}")
    @ApiMessage("User remove an item from cart")
    public ResponseEntity<Void> removeOneItem(@PathVariable("bookId") Long bookId) {
        this.cartService.handleRemoveItemFromCart(bookId);
        return ResponseEntity.ok(null);
    }

    @DeleteMapping("/carts/clear")
    @ApiMessage("Remove all item in cart")
    public ResponseEntity<Void> removeAllItem() {
        this.cartService.handleClearCart();
        return ResponseEntity.ok(null);
    }

}
