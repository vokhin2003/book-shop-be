package com.rober.bookshop.service;

import com.rober.bookshop.model.entity.User;
import com.rober.bookshop.model.request.CreateOrderRequestDTO;
import com.rober.bookshop.model.response.OrderResponseDTO;

public interface IOrderService {

    OrderResponseDTO handleCreateOrder(CreateOrderRequestDTO reqDTO, User user);

}
