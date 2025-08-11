package com.rober.bookshop.repository;

import com.rober.bookshop.model.entity.Address;
import com.rober.bookshop.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByUser(User user);
    long countByUserAndIsDefaultTrue(User user);
}


