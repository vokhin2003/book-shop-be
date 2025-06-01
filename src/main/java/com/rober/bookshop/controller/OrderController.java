package com.rober.bookshop.controller;

import com.rober.bookshop.annotation.ApiMessage;
import com.rober.bookshop.exception.IdInvalidException;
import com.rober.bookshop.model.entity.Book;
import com.rober.bookshop.model.entity.Order;
import com.rober.bookshop.model.entity.User;
import com.rober.bookshop.model.request.CreateOrderRequestDTO;
import com.rober.bookshop.model.request.UpdateOrderRequestDTO;
import com.rober.bookshop.model.response.CancelOrderResponseDTO;
import com.rober.bookshop.model.response.OrderResponseDTO;
import com.rober.bookshop.model.response.ResultPaginationDTO;
import com.rober.bookshop.service.IOrderService;
import com.rober.bookshop.service.IUserService;
import com.rober.bookshop.validation.AdminCreateOrderValidationGroup;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/orders")
    @ApiMessage("Get order with pagination")
    public ResponseEntity<ResultPaginationDTO> getAll(@Filter Specification<Order> spec, Pageable pageable) {
        return ResponseEntity.ok(this.orderService.getAll(spec, pageable));
    }

    @PostMapping("/admin/orders")
    @ApiMessage("Admin create order for user")
    public ResponseEntity<OrderResponseDTO> adminCreateOrder(@RequestBody @Validated(AdminCreateOrderValidationGroup.class) CreateOrderRequestDTO reqDTO) {
        User user = this.userService.getUserById(reqDTO.getUserId());
        if (user == null) throw new IdInvalidException("User not found in database");
        return ResponseEntity.status(HttpStatus.CREATED).body(this.orderService.handleCreateOrder(reqDTO, user));
    }

    @PutMapping("/admin/orders/{id}")
    @ApiMessage("Admin update order for user")
    public ResponseEntity<OrderResponseDTO> adminUpdateOrder(@PathVariable Long id, @RequestBody @Valid UpdateOrderRequestDTO reqDTO) {
        return ResponseEntity.ok(this.orderService.handleUpdateOrder(id, reqDTO));
    }


}
