package com.rober.bookshop.controller;

import com.rober.bookshop.annotation.ApiMessage;
import com.rober.bookshop.enums.OrderStatus;
import com.rober.bookshop.enums.PaymentMethod;
import com.rober.bookshop.exception.IdInvalidException;
import com.rober.bookshop.model.entity.Order;
import com.rober.bookshop.model.entity.User;
import com.rober.bookshop.model.request.CreateOrderRequestDTO;
import com.rober.bookshop.model.request.UpdateOrderRequestDTO;
import com.rober.bookshop.model.response.CancelOrderResponseDTO;
import com.rober.bookshop.model.response.OrderResponseDTO;
import com.rober.bookshop.model.response.ResultPaginationDTO;
import com.rober.bookshop.service.IEmailService;
import com.rober.bookshop.service.IOrderService;
import com.rober.bookshop.service.IUserService;
import com.rober.bookshop.validation.AdminCreateOrderValidationGroup;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Tag(name = "Order")
public class OrderController {

    private final IOrderService orderService;
    private final IUserService userService;
    private final IEmailService emailService;

    @Value("${spring.bookshop.logo}")
    private String logo;

    @PostMapping("/orders")
    @ApiMessage("User place order")
    @Operation(summary = "User place order", description = "User places a new order and returns the order details.")
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
    @Operation(summary = "Get user order history", description = "Get the order history of the currently logged-in user with pagination and filtering.")
    public ResponseEntity<ResultPaginationDTO> history(@Filter Specification<Order> spec, Pageable pageable) {
        return ResponseEntity.ok(this.orderService.fetchHistory(spec, pageable));
    }

    @GetMapping("/orders/{id}")
    @ApiMessage("Get detail or by id")
    @Operation(summary = "Get order by id", description = "Get details of an order by its id.")
    public ResponseEntity<OrderResponseDTO> getOrderDetail(@PathVariable Long id) {
        return ResponseEntity.ok(this.orderService.fetchOrderById(id));
    }

    @PutMapping("/orders/{orderId}/cancel")
    @ApiMessage("User cancel order")
    @Operation(summary = "Cancel order", description = "Cancel an order by its id.")
    public ResponseEntity<CancelOrderResponseDTO> cancelOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(this.orderService.handleCancelOrder(orderId));
    }


    @GetMapping("/orders")
    @ApiMessage("Get order with pagination")
    @Operation(summary = "Get orders with pagination", description = "Get a paginated list of orders with optional filtering.")
    public ResponseEntity<ResultPaginationDTO> getAll(@Filter Specification<Order> spec, Pageable pageable) {
        return ResponseEntity.ok(this.orderService.getAll(spec, pageable));
    }

    @PostMapping("/admin/orders")
    @ApiMessage("Admin create order for user")
    @Operation(summary = "Admin create order for user", description = "Admin creates an order for a user and returns the order details.")
    public ResponseEntity<OrderResponseDTO> adminCreateOrder(@RequestBody @Validated(AdminCreateOrderValidationGroup.class) CreateOrderRequestDTO reqDTO) {
        User user = this.userService.getUserById(reqDTO.getUserId());

        return ResponseEntity.status(HttpStatus.CREATED).body(this.orderService.handleCreateOrder(reqDTO, user));
    }

    @PutMapping("/admin/orders/{id}")
    @ApiMessage("Admin update order for user")
    @Operation(summary = "Admin update order for user", description = "Admin updates an order for a user by id and returns the updated order details.")
    public ResponseEntity<OrderResponseDTO> adminUpdateOrder(@PathVariable Long id, @RequestBody @Valid UpdateOrderRequestDTO reqDTO) {
        return ResponseEntity.ok(this.orderService.handleUpdateOrder(id, reqDTO));
    }



}
