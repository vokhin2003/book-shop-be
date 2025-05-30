package com.rober.bookshop.repository;

import com.rober.bookshop.enums.TransactionStatus;
import com.rober.bookshop.model.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByTransactionId(String transactionId);
    Optional<Transaction> findByOrder_IdAndStatus(Long orderId, TransactionStatus transactionStatus);

}
