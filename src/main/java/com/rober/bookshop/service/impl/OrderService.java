package com.rober.bookshop.service.impl;

import com.rober.bookshop.enums.OrderStatus;
import com.rober.bookshop.exception.IdInvalidException;
import com.rober.bookshop.exception.InputInvalidException;
import com.rober.bookshop.mapper.OrderMapper;
import com.rober.bookshop.model.entity.Book;
import com.rober.bookshop.model.entity.Order;
import com.rober.bookshop.model.entity.OrderItem;
import com.rober.bookshop.model.entity.User;
import com.rober.bookshop.model.request.CreateOrderRequestDTO;
import com.rober.bookshop.model.request.OrderItemRequestDTO;
import com.rober.bookshop.model.response.OrderResponseDTO;
import com.rober.bookshop.repository.BookRepository;
import com.rober.bookshop.repository.OrderItemRepository;
import com.rober.bookshop.repository.OrderRepository;
import com.rober.bookshop.service.IOrderService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService implements IOrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final BookRepository bookRepository;
    private final OrderItemRepository orderItemRepository;

    @Override
    @Transactional
    public OrderResponseDTO handleCreateOrder(CreateOrderRequestDTO reqDTO, User user) {

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalPrice = BigDecimal.ZERO;

        for (OrderItemRequestDTO item : reqDTO.getItems()) {
            Book book = bookRepository.findById(item.getBookId())
                    .orElseThrow(() -> new IdInvalidException("Book with id = " + item.getBookId() + " not found"));
            if (book.getQuantity() < item.getQuantity()) {
                throw new InputInvalidException("Insufficient stock for book: " + book.getTitle());
            }

            // create OrderItem
            OrderItem orderItem = new OrderItem();
            orderItem.setBook(book);
            orderItem.setQuantity(item.getQuantity());


            // Tính tổng giá (áp dụng discount nếu có)
            BigDecimal itemPrice = book.getPrice()
                    .multiply(BigDecimal.valueOf(100 - book.getDiscount()).divide(BigDecimal.valueOf(100)));

            totalPrice = totalPrice.add(itemPrice.multiply(BigDecimal.valueOf(item.getQuantity())) );

            orderItem.setPrice(itemPrice);

            orderItems.add(orderItem);

            // Cập nhật tồn kho
//            book.setQuantity(book.getQuantity() - item.getQuantity());
//            bookRepository.save(book);

            bookRepository.updateQuantity(book.getId(), book.getQuantity() - item.getQuantity());
        }

        // Tạo Order
        Order order = this.orderMapper.toOrder(reqDTO);
        order.setUser(user);
        order.setOrderItems(orderItems);
        order.setTotalAmount(totalPrice);
        order.setStatus(OrderStatus.PENDING);

        // Liên kết OrderItem với Order
        orderItems.forEach(item -> item.setOrder(order));
        this.orderItemRepository.saveAll(orderItems);

        return orderMapper.toResponseDTO(this.orderRepository.save(order));

    }
}
