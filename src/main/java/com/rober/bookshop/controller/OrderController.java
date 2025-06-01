package com.rober.bookshop.controller;

import com.rober.bookshop.annotation.ApiMessage;
import com.rober.bookshop.enums.OrderStatus;
import com.rober.bookshop.enums.PaymentMethod;
import com.rober.bookshop.exception.IdInvalidException;
import com.rober.bookshop.model.entity.Order;
import com.rober.bookshop.model.entity.User;
import com.rober.bookshop.model.request.CreateOrderRequestDTO;
import com.rober.bookshop.model.response.CancelOrderResponseDTO;
import com.rober.bookshop.model.response.OrderResponseDTO;
import com.rober.bookshop.model.response.ResultPaginationDTO;
import com.rober.bookshop.service.IEmailService;
import com.rober.bookshop.service.IOrderService;
import com.rober.bookshop.service.IUserService;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class OrderController {

    private final IOrderService orderService;
    private final IUserService userService;
    private final IEmailService emailService;

    @Value("${spring.bookshop.logo}")
    private String logo;

    @PostMapping("/orders")
    @ApiMessage("User place order")
    public ResponseEntity<OrderResponseDTO> userPlaceOrder(@RequestBody @Valid CreateOrderRequestDTO reqDTO) {
        User  user = this.userService.getUserLogin();
        if (user == null) {
            throw new IdInvalidException("User not found in database");
        }

        OrderResponseDTO respDTO = this.orderService.handleCreateOrder(reqDTO, user);

        if (respDTO.getPaymentMethod() == PaymentMethod.COD && respDTO.getStatus() == OrderStatus.PENDING){
            this.emailService.sendOrderSuccessEmail(user, respDTO);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(respDTO);
    }


    @GetMapping("/orders/history")
    @ApiMessage("User get order history")
    public ResponseEntity<ResultPaginationDTO> history(@Filter Specification<Order> spec, Pageable pageable) {
        return ResponseEntity.ok(this.orderService.fetchHistory(spec, pageable));
    }

    @GetMapping("/orders/{id}")
    @ApiMessage("Get detail or by id")
    public ResponseEntity<OrderResponseDTO> getOrderDetail(@PathVariable Long id) {
        return ResponseEntity.ok(this.orderService.fetchOrderById(id));
    }

    @PutMapping("/orders/{orderId}/cancel")
    @ApiMessage("User cancel order")
    public ResponseEntity<CancelOrderResponseDTO> cancelOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(this.orderService.handleCancelOrder(orderId));
    }



}
