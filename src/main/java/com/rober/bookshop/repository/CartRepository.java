package com.rober.bookshop.repository;

import com.rober.bookshop.model.entity.Book;
import com.rober.bookshop.model.entity.Cart;
import com.rober.bookshop.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    Cart findByUserAndBook(User user, Book book);
    List<Cart> findAllByUser(User user);
    void deleteByUserAndBook(User user, Book book);
    void deleteAllByUser(User user);

}
