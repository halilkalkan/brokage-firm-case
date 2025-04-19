package com.kalkan.brokage.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kalkan.brokage.model.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Customer findByUsername(String username);

    boolean existsByUsername(String username);
}
