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
import com.rober.bookshop.model.request.NotificationRequestDTO;
import com.rober.bookshop.model.request.OrderItemRequestDTO;
import com.rober.bookshop.model.request.UpdateOrderRequestDTO;
import com.rober.bookshop.model.response.BookResponseDTO;
import com.rober.bookshop.model.response.CancelOrderResponseDTO;
import com.rober.bookshop.model.response.OrderResponseDTO;
import com.rober.bookshop.model.response.ResultPaginationDTO;
import com.rober.bookshop.repository.*;
import com.rober.bookshop.service.IFirebaseMessagingService;
import com.rober.bookshop.service.IOrderService;
import com.rober.bookshop.service.IUserService;
import com.turkraft.springfilter.builder.FilterBuilder;
import com.turkraft.springfilter.converter.FilterSpecificationConverter;
import com.turkraft.springfilter.parser.node.FilterNode;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService implements IOrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final BookRepository bookRepository;
    private final OrderItemRepository orderItemRepository;
    private final TransactionRepository transactionRepository;
    private final IUserService userService;
    private final FilterBuilder filterBuilder;
    private final FilterSpecificationConverter filterSpecificationConverter;
    private final UserDeviceTokenRepository userDeviceTokenRepository;
    private final IFirebaseMessagingService firebaseMessagingService;


    @Override
    @Transactional
    public OrderResponseDTO handleCreateOrder(CreateOrderRequestDTO reqDTO, User user, String deviceType) {

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

//            bookRepository.updateQuantity(book.getId(), book.getQuantity() - item.getQuantity());

            bookRepository.updateQuantityAndSold(book.getId(),
                    book.getQuantity() - item.getQuantity(),
                    book.getSold() + item.getQuantity());
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

//        String transactionId = reqDTO.getPaymentMethod() == PaymentMethod.COD
//                ? "COD_" + savedOrder.getId()
//                : savedOrder.getId() + "_" + Instant.now().toEpochMilli();

        String transactionId = reqDTO.getPaymentMethod() == PaymentMethod.COD
                ? "COD_" + savedOrder.getId()
                : savedOrder.getId() + "_" + deviceType + "_" + Instant.now().toEpochMilli();
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
    @Transactional
    public CancelOrderResponseDTO handleCancelOrder(Long orderId) {
        Order canceledOrder = this.orderRepository.findById(orderId).orElseThrow(() -> new IdInvalidException("Order with id = " + orderId + " not found"));

        if (canceledOrder.getStatus() != OrderStatus.PENDING) {
            throw new BadRequestException("The order cannot be canceled because it is being processed");
        }

        // Duyệt qua các OrderItem để cập nhật quantity và sold của Book
        List<OrderItem> orderItems = canceledOrder.getOrderItems();
        for (OrderItem item : orderItems) {
            Book book = item.getBook();
            // Kiểm tra sold để tránh giá trị âm
            if (book.getSold() < item.getQuantity()) {
                throw new BadRequestException("Cannot reduce sold quantity for book: " + book.getId());
            }
            // Cập nhật quantity (tăng lại) và sold (giảm đi)
            bookRepository.updateQuantityAndSold(
                    book.getId(),
                    book.getQuantity() + item.getQuantity(),
                    book.getSold() - item.getQuantity()
            );
        }

        // Cập nhật trạng thái Transaction (nếu có)
//        for (Transaction transaction : canceledOrder.getTransactions()) {
//            if (transaction.getStatus() == TransactionStatus.PENDING) {
//                transaction.setStatus(TransactionStatus.CANCELLED);
//                transactionRepository.save(transaction);
//            }
//        }

        // Cập nhật trạng thái Transaction chỉ cho các Transaction có status = PENDING
        List<Transaction> transactionsToUpdate = canceledOrder.getTransactions().stream()
                .filter(transaction -> transaction.getStatus() == TransactionStatus.PENDING)
                .peek(transaction -> transaction.setStatus(TransactionStatus.CANCELLED))
                .collect(Collectors.toList());
        transactionRepository.saveAll(transactionsToUpdate);

        canceledOrder.setStatus(OrderStatus.CANCELLED);
        this.orderRepository.save(canceledOrder);
        CancelOrderResponseDTO res = new CancelOrderResponseDTO();
        res.setOrderId(canceledOrder.getId());
        res.setStatus(canceledOrder.getStatus());

        return res;
    }

    @Override
    public ResultPaginationDTO getAll(Specification<Order> spec, Pageable pageable) {
        Page<Order> orderPage = this.orderRepository.findAll(spec, pageable);
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
    @Transactional
    public OrderResponseDTO handleUpdateOrder(Long id, UpdateOrderRequestDTO reqDTO) {
        Order updatedOrder = this.orderRepository.findById(id).orElseThrow(() -> new IdInvalidException("Order with id = " + id + " not found"));

        if (updatedOrder.getStatus() == OrderStatus.DELIVERED) {
            throw new BadRequestException("This order cannot be updated because it is delivered");
        }

        if (reqDTO.getStatus() == OrderStatus.PENDING) {
            if (updatedOrder.getPaymentMethod() != PaymentMethod.COD) {
                throw new BadRequestException("This order cannot be updated because it has been paid.");
            }
        }

        if (updatedOrder.getStatus() == OrderStatus.CANCELLED && reqDTO.getStatus() != OrderStatus.CANCELLED) {
            // Duyệt qua các OrderItem để cập nhật quantity và sold của Book
            List<OrderItem> orderItems = updatedOrder.getOrderItems();
            for (OrderItem item : orderItems) {
                Book book = item.getBook();
                // Kiểm tra sold để tránh giá trị âm
                if (book.getSold() < item.getQuantity()) {
                    throw new BadRequestException("Cannot reduce sold quantity for book: " + book.getId());
                }
                // Cập nhật quantity (tăng lại) và sold (giảm đi)
                bookRepository.updateQuantityAndSold(
                        book.getId(),
                        book.getQuantity() - item.getQuantity(),
                        book.getSold() + item.getQuantity()
                );
            }
        }

        if (updatedOrder.getStatus() != OrderStatus.CANCELLED && reqDTO.getStatus() == OrderStatus.CANCELLED) {
// Duyệt qua các OrderItem để cập nhật quantity và sold của Book
            List<OrderItem> orderItems = updatedOrder.getOrderItems();
            for (OrderItem item : orderItems) {
                Book book = item.getBook();
                // Kiểm tra sold để tránh giá trị âm
                if (book.getSold() < item.getQuantity()) {
                    throw new BadRequestException("Cannot reduce sold quantity for book: " + book.getId());
                }
                // Cập nhật quantity (tăng lại) và sold (giảm đi)
                bookRepository.updateQuantityAndSold(
                        book.getId(),
                        book.getQuantity() + item.getQuantity(),
                        book.getSold() - item.getQuantity()
                );
            }
        }

        if (reqDTO.getStatus() != updatedOrder.getStatus()) {
            List<UserDeviceToken> tokens = userDeviceTokenRepository.findByUserIdAndDeviceTypeAndIsActiveTrue(updatedOrder.getUser().getId(), "WEB");
            for (UserDeviceToken token : tokens) {
                NotificationRequestDTO notification = new NotificationRequestDTO();
                notification.setTitle("Cập nhật trạng thái đơn hàng");
                // Sử dụng mapping tiếng Việt cho trạng thái
                notification.setBody("Đơn hàng #" + id + " đã được cập nhật thành: " + getOrderStatusVi(reqDTO.getStatus()));
                notification.setDeviceToken(token.getDeviceToken());
                // Lấy ảnh bìa sách đầu tiên nếu có, nếu không thì dùng logo shop
                String imageUrl = null;
                if (updatedOrder.getOrderItems() != null && !updatedOrder.getOrderItems().isEmpty()) {
                    imageUrl = updatedOrder.getOrderItems().get(0).getBook().getThumbnail();
                }
                if (imageUrl == null || imageUrl.isEmpty()) {
                    imageUrl = "https://camo.githubusercontent.com/a91aaa1d350e2c2450fb563fed4a71039eb8841c3225afc6931a990a4f472bb0/68747470733a2f2f66697265626173652e676f6f676c652e636f6d2f696d616765732f6272616e642d67756964656c696e65732f6c6f676f2d6275696c745f77686974652e706e67";
                }
                notification.setImage(imageUrl);
                // Thêm url vào data để FE redirect khi click
                notification.setData(Map.of(
                    "orderId", id.toString(),
                    "url", "http://localhost:3000/order/detail/" + id
                ));
                try {
                    firebaseMessagingService.sendNotificationByToken(notification);
                    log.info("Notification sent to token: {}", token.getDeviceToken());
                } catch (Exception e) {
                    log.error("Failed to send notification to token: {}. Error: {}", token.getDeviceToken(), e.getMessage());
                }
            }
        }

        this.orderMapper.updateOrderFromDTO(reqDTO, updatedOrder);

        return this.orderMapper.toResponseDTO(this.orderRepository.save(updatedOrder));
    }

    // Thêm hàm mapping OrderStatus sang tiếng Việt
    private String getOrderStatusVi(OrderStatus status) {
        return switch (status) {
            case PENDING -> "Chờ xác nhận";
            case CONFIRMED -> "Đã xác nhận";
            case SHIPPING -> "Đang giao hàng";
            case DELIVERED -> "Đã giao hàng";
            case CANCELLED -> "Đã huỷ";
            default -> status.name();
        };
    }

//    @Override
//    public OrderResponseDTO handleCreateOrder(CreateOrderRequestDTO reqDTO) {
//
//        if (reqDTO.getUserId() == null) throw new IdInvalidException("User id is invalid");
//        User user = this.userService.getUserById(reqDTO.getUserId());
//        if (user == null) throw new IdInvalidException("User not found in database");
//
//        List<OrderItem> orderItems = new ArrayList<>();
//        BigDecimal totalPrice = BigDecimal.ZERO;
//
//        for (OrderItemRequestDTO item : reqDTO.getItems()) {
//            Book book = bookRepository.findById(item.getBookId())
//                    .orElseThrow(() -> new IdInvalidException("Book with id = " + item.getBookId() + " not found"));
//            if (book.getQuantity() < item.getQuantity()) {
//                throw new InputInvalidException("Insufficient stock for book: " + book.getTitle());
//            }
//
//            // create OrderItem
//            OrderItem orderItem = new OrderItem();
//            orderItem.setBook(book);
//            orderItem.setQuantity(item.getQuantity());
//
//
//            // Tính tổng giá (áp dụng discount nếu có)
//            BigDecimal itemPrice = book.getPrice()
//                    .multiply(BigDecimal.valueOf(100 - book.getDiscount()).divide(BigDecimal.valueOf(100)));
//
//            totalPrice = totalPrice.add(itemPrice.multiply(BigDecimal.valueOf(item.getQuantity())) );
//
//            orderItem.setPrice(itemPrice);
//
//            orderItems.add(orderItem);
//
//            // Cập nhật tồn kho
////            book.setQuantity(book.getQuantity() - item.getQuantity());
////            bookRepository.save(book);
//
////            bookRepository.updateQuantity(book.getId(), book.getQuantity() - item.getQuantity());
//
//            bookRepository.updateQuantityAndSold(book.getId(),
//                    book.getQuantity() - item.getQuantity(),
//                    book.getSold() + item.getQuantity());
//        }
//
//
//        // Tạo Order
//        Order order = this.orderMapper.toOrder(reqDTO);
//        order.setUser(user);
//        order.setOrderItems(orderItems);
//        order.setTotalAmount(totalPrice);
//        order.setStatus(OrderStatus.PENDING);
//
//        // Liên kết OrderItem với Order
//        orderItems.forEach(item -> item.setOrder(order));
//        this.orderItemRepository.saveAll(orderItems);
//
//        Order savedOrder = this.orderRepository.save(order);
//
//        // Tạo Transaction
//        Transaction transaction = new Transaction();
//        String transactionId = reqDTO.getPaymentMethod() == PaymentMethod.COD
//                ? "COD_" + savedOrder.getId()
//                : savedOrder.getId() + "_" + Instant.now().toEpochMilli();
//        transaction.setTransactionId(transactionId);
//        transaction.setAmount(totalPrice);
//        transaction.setPaymentMethod(reqDTO.getPaymentMethod());
//        transaction.setStatus(TransactionStatus.PENDING);
//        transaction.setTransactionDate(Instant.now());
//        transaction.setOrder(savedOrder);
////        transaction.setOrderInfo("Payment for order with id " + savedOrder.getId() + ". Total: " + totalPrice + " VND");
//        transaction.setOrderInfo(String.format("Payment for order with id %d. Total: %.2f VND",
//                savedOrder.getId(), totalPrice));
//
//        transactionRepository.save(transaction);
//
//        return orderMapper.toResponseDTO(savedOrder);
//    }
}
