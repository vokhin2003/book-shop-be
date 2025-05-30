package com.rober.bookshop.controller;

import com.rober.bookshop.annotation.ApiMessage;
import com.rober.bookshop.exception.IdInvalidException;
import com.rober.bookshop.model.entity.User;
import com.rober.bookshop.model.request.CreateOrderRequestDTO;
import com.rober.bookshop.model.response.OrderResponseDTO;
import com.rober.bookshop.service.IOrderService;
import com.rober.bookshop.service.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class OrderController {

    private final IOrderService orderService;
    private final IUserService userService;

    @PostMapping("/orders")
    @ApiMessage("User place order")
    public ResponseEntity<OrderResponseDTO> userPlaceOrder(@RequestBody @Valid CreateOrderRequestDTO reqDTO) {
        User  user = this.userService.getUserLogin();
        if (user == null) {
            throw new IdInvalidException("User not found in database");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(this.orderService.handleCreateOrder(reqDTO, user));
    }

}
