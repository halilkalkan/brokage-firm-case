package com.kalkan.brokage.controller;

import com.kalkan.brokage.model.Order;
import com.kalkan.brokage.service.CustomerService;
import com.kalkan.brokage.service.OrderService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private final OrderService orderService;
    private final CustomerService customerService;

    public OrderController(OrderService orderService, CustomerService customerService) {
        this.orderService = orderService;
        this.customerService = customerService;
    }

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody Order order) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            if (!isAdmin) {
                String authenticatedUsername = authentication.getName();
                Long customerId = customerService.getCustomerByUsername(authenticatedUsername).getId();

                if (!customerId.equals(order.getCustomerId())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body("You can only create orders for your own account");
                }
            }

            return ResponseEntity.ok(orderService.createOrder(order));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> listOrders(
            @RequestParam Long customerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            String authenticatedUsername = authentication.getName();
            Long actualCustomerId = customerService.getCustomerByUsername(authenticatedUsername).getId();

            if (!actualCustomerId.equals(customerId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You can only list orders for your own account");
            }
        }

        return ResponseEntity.ok(orderService.listOrders(customerId, from, to));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOrder(@PathVariable Long id) {
        Order order = orderService.getOrderById(id);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            String authenticatedUsername = authentication.getName();
            Long customerId = customerService.getCustomerByUsername(authenticatedUsername).getId();

            if (!customerId.equals(order.getCustomerId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You can only delete your own orders");
            }
        }

        boolean deleted = orderService.deleteOrder(id);
        return deleted ? ResponseEntity.ok("Order deleted")
                : ResponseEntity.badRequest().body("Order cannot be deleted");
    }
}