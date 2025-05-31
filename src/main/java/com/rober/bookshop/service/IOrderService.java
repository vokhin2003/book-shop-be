package com.rober.bookshop.service;

import com.rober.bookshop.enums.OrderStatus;
import com.rober.bookshop.model.entity.Order;
import com.rober.bookshop.model.entity.User;
import com.rober.bookshop.model.request.CreateOrderRequestDTO;
import com.rober.bookshop.model.response.CancelOrderResponseDTO;
import com.rober.bookshop.model.response.OrderResponseDTO;
import com.rober.bookshop.model.response.ResultPaginationDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface IOrderService {

    OrderResponseDTO handleCreateOrder(CreateOrderRequestDTO reqDTO, User user);
    void updateOrderStatusById(Long orderId, OrderStatus status);
    ResultPaginationDTO fetchHistory(Specification<Order> spec, Pageable pageable);

    OrderResponseDTO fetchOrderById(Long id);

    CancelOrderResponseDTO handleCancelOrder(Long orderId);

}
