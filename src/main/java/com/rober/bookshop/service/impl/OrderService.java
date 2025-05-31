package com.rober.bookshop.service.impl;

import com.rober.bookshop.enums.OrderStatus;
import com.rober.bookshop.enums.PaymentMethod;
import com.rober.bookshop.enums.TransactionStatus;
import com.rober.bookshop.exception.BadRequestException;
import com.rober.bookshop.exception.IdInvalidException;
import com.rober.bookshop.exception.InputInvalidException;
import com.rober.bookshop.mapper.OrderMapper;
import com.rober.bookshop.model.entity.*;
import com.rober.bookshop.model.request.CreateOrderRequestDTO;
import com.rober.bookshop.model.request.OrderItemRequestDTO;
import com.rober.bookshop.model.response.BookResponseDTO;
import com.rober.bookshop.model.response.CancelOrderResponseDTO;
import com.rober.bookshop.model.response.OrderResponseDTO;
import com.rober.bookshop.model.response.ResultPaginationDTO;
import com.rober.bookshop.repository.BookRepository;
import com.rober.bookshop.repository.OrderItemRepository;
import com.rober.bookshop.repository.OrderRepository;
import com.rober.bookshop.repository.TransactionRepository;
import com.rober.bookshop.service.IOrderService;
import com.rober.bookshop.service.IUserService;
import com.turkraft.springfilter.builder.FilterBuilder;
import com.turkraft.springfilter.converter.FilterSpecificationConverter;
import com.turkraft.springfilter.parser.node.FilterNode;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderService implements IOrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final BookRepository bookRepository;
    private final OrderItemRepository orderItemRepository;
    private final TransactionRepository transactionRepository;
    private final IUserService userService;
    private final FilterBuilder filterBuilder;
    private final FilterSpecificationConverter filterSpecificationConverter;

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

        Order savedOrder = this.orderRepository.save(order);

        // Tạo Transaction
        Transaction transaction = new Transaction();
        String transactionId = reqDTO.getPaymentMethod() == PaymentMethod.COD
                ? "COD_" + savedOrder.getId()
                : savedOrder.getId() + "_" + Instant.now().toEpochMilli();
        transaction.setTransactionId(transactionId);
        transaction.setAmount(totalPrice);
        transaction.setPaymentMethod(reqDTO.getPaymentMethod());
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setTransactionDate(Instant.now());
        transaction.setOrder(savedOrder);
//        transaction.setOrderInfo("Payment for order with id " + savedOrder.getId() + ". Total: " + totalPrice + " VND");
        transaction.setOrderInfo(String.format("Payment for order with id %d. Total: %.2f VND",
                savedOrder.getId(), totalPrice));

        transactionRepository.save(transaction);

        return orderMapper.toResponseDTO(savedOrder);

    }

    @Override
    @Transactional
    public void updateOrderStatusById(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IdInvalidException("Order with id = " + orderId + " not found"));
        if (order.getStatus() == OrderStatus.PENDING) {
            order.setStatus(status);
            orderRepository.save(order);
        } else {
            throw new IdInvalidException("Order is not in PENDING status");
        }
    }

    @Override
    public ResultPaginationDTO fetchHistory(Specification<Order> spec, Pageable pageable) {

        // Lấy user từ email
        User currentUser = this.userService.getUserLogin();
        if (currentUser == null) {
            throw new IdInvalidException("User not found");
        }

        // Tạo FilterNode để lọc theo user_id
        FilterNode userFilterNode = filterBuilder.field("user.id").equal(filterBuilder.input(currentUser.getId())).get();
        Specification<Order> userSpec = filterSpecificationConverter.convert(userFilterNode);

        // Kết hợp userSpec với spec từ @Filter
        Specification<Order> finalSpec = Specification.where(userSpec).and(spec);

        Page<Order> orderPage = orderRepository.findAll(finalSpec, pageable);

        ResultPaginationDTO res = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();

        meta.setCurrent(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());

        meta.setPages(orderPage.getTotalPages());
        meta.setTotal(orderPage.getTotalElements());

        res.setMeta(meta);

        List<OrderResponseDTO> listOrder = orderPage.getContent().stream().map(this.orderMapper::toResponseDTO).toList();

        res.setResult(listOrder);
        return res;
    }

    @Override
    public OrderResponseDTO fetchOrderById(Long id) {
        Order order = this.orderRepository.findById(id).orElseThrow(() -> new IdInvalidException("Order with id = " + id + " not found"));
        return orderMapper.toResponseDTO(order);
    }

    @Override
    public CancelOrderResponseDTO handleCancelOrder(Long orderId) {
        Order canceledOrder = this.orderRepository.findById(orderId).orElseThrow(() -> new IdInvalidException("Order with id = " + orderId + " not found"));
        if (canceledOrder.getStatus() != OrderStatus.PENDING) {
            throw new BadRequestException("The order cannot be canceled because it is being processed");
        }

        canceledOrder.setStatus(OrderStatus.CANCELLED);
        this.orderRepository.save(canceledOrder);
        CancelOrderResponseDTO res = new CancelOrderResponseDTO();
        res.setOrderId(canceledOrder.getId());
        res.setStatus(canceledOrder.getStatus());

        return res;
    }
}
