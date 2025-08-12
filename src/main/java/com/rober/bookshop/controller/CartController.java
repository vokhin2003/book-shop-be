package com.rober.bookshop.controller;

import com.rober.bookshop.annotation.ApiMessage;
import com.rober.bookshop.model.request.CartRequestDTO;
import com.rober.bookshop.model.response.CartResponseDTO;
import com.rober.bookshop.model.request.BulkRemoveCartRequestDTO;
import com.rober.bookshop.service.ICartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Cart")
public class CartController {

    private final ICartService cartService;

    @PostMapping("/carts")
    @ApiMessage("User add to card")
    @Operation(summary = "Add item to cart", description = "Add an item to the user's cart.")
    public ResponseEntity<CartResponseDTO> addToCart(@RequestBody @Valid CartRequestDTO reqDTO) {
        return ResponseEntity.ok(this.cartService.handleAddToCart(reqDTO));
    }

    @PutMapping("/carts")
    @ApiMessage("User update quantity in cart")
    @Operation(summary = "Update cart item quantity", description = "Update the quantity of an item in the user's cart.")
    public ResponseEntity<CartResponseDTO> update(@RequestBody @Valid CartRequestDTO reqDTO) {
        return ResponseEntity.ok(this.cartService.handleUpdateCart(reqDTO));
    }

    @GetMapping("/carts")
    @ApiMessage("User get all cart item in cart")
    @Operation(summary = "Get all cart items", description = "Get all items in the user's cart.")
    public ResponseEntity<List<CartResponseDTO>> getAll() {
        return ResponseEntity.ok(this.cartService.getAll());
    }

    @DeleteMapping("/carts/{bookId}")
    @ApiMessage("User remove an item from cart")
    @Operation(summary = "Remove item from cart", description = "Remove a specific item from the user's cart by book id.")
    public ResponseEntity<Void> removeOneItem(@PathVariable("bookId") Long bookId) {
        this.cartService.handleRemoveItemFromCart(bookId);
        return ResponseEntity.ok(null);
    }

    @DeleteMapping("/carts/clear")
    @ApiMessage("Remove all item in cart")
    @Operation(summary = "Clear cart", description = "Remove all items from the user's cart.")
    public ResponseEntity<Void> removeAllItem() {
        this.cartService.handleClearCart();
        return ResponseEntity.ok(null);
    }

    @DeleteMapping("/carts/bulk")
    @ApiMessage("Remove selected items in cart")
    @Operation(summary = "Remove selected items in cart", description = "Remove selected cart items by list of bookIds.")
    public ResponseEntity<Void> removeSelectedItems(@RequestBody @Valid BulkRemoveCartRequestDTO request) {
        this.cartService.handleRemoveSelectedItems(request.getBookIds());
        return ResponseEntity.ok(null);
    }

}
