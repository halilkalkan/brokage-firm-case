package com.kalkan.brokage.repository;

import com.kalkan.brokage.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
        List<Order> findByCustomerIdAndCreateDateBetween(Long customerId, LocalDateTime from, LocalDateTime to);

        List<Order> findByCustomerId(Long customerId);
}