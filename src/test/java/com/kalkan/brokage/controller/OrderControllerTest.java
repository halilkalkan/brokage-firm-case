package com.kalkan.brokage.controller;

import com.kalkan.brokage.model.Customer;
import com.kalkan.brokage.model.Order;
import com.kalkan.brokage.service.OrderService;
import com.kalkan.brokage.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private OrderController orderController;

    @Test
    @WithMockUser(roles = "ADMIN")
    void createOrder_AdminUser_ReturnsOrder() {
        Long customerId = 1L;
        Order order = Order.builder()
                .customerId(customerId)
                .assetName("AAPL")
                .orderSide(Order.Side.BUY)
                .size(100.0)
                .price(150.0)
                .status(Order.Status.PENDING)
                .createDate(LocalDateTime.now())
                .build();

        when(orderService.createOrder(any(Order.class))).thenReturn(order);

        ResponseEntity<?> response = orderController.createOrder(order);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(order, response.getBody());
        verify(orderService).createOrder(order);
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void createOrder_NonAdminUser_OwnAccount_ReturnsOrder() {
        Long customerId = 1L;
        String username = "testuser";
        Customer customer = new Customer();
        customer.setId(customerId);

        Order order = Order.builder()
                .customerId(customerId)
                .assetName("AAPL")
                .orderSide(Order.Side.BUY)
                .size(100.0)
                .price(150.0)
                .status(Order.Status.PENDING)
                .createDate(LocalDateTime.now())
                .build();

        when(customerService.getCustomerByUsername(username)).thenReturn(customer);
        when(orderService.createOrder(any(Order.class))).thenReturn(order);

        ResponseEntity<?> response = orderController.createOrder(order);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(order, response.getBody());
        verify(orderService).createOrder(order);
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void createOrder_NonAdminUser_OtherAccount_ReturnsForbidden() {
        Long requestedCustomerId = 1L;
        Long actualCustomerId = 2L;
        String username = "testuser";
        Customer customer = new Customer();
        customer.setId(actualCustomerId);

        Order order = Order.builder()
                .customerId(requestedCustomerId)
                .assetName("AAPL")
                .orderSide(Order.Side.BUY)
                .size(100.0)
                .price(150.0)
                .status(Order.Status.PENDING)
                .createDate(LocalDateTime.now())
                .build();

        when(customerService.getCustomerByUsername(username)).thenReturn(customer);

        ResponseEntity<?> response = orderController.createOrder(order);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("You can only create orders for your own account", response.getBody());
        verify(orderService, never()).createOrder(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void listOrders_AdminUser_ReturnsOrders() {
        Long customerId = 1L;
        LocalDateTime from = LocalDateTime.now().minusDays(1);
        LocalDateTime to = LocalDateTime.now();
        List<Order> expectedOrders = Arrays.asList(
                Order.builder().customerId(customerId).build(),
                Order.builder().customerId(customerId).build());

        when(orderService.listOrders(customerId, from, to)).thenReturn(expectedOrders);

        ResponseEntity<?> response = orderController.listOrders(customerId, from, to);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedOrders, response.getBody());
        verify(orderService).listOrders(customerId, from, to);
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void listOrders_NonAdminUser_OwnAccount_ReturnsOrders() {
        Long customerId = 1L;
        String username = "testuser";
        LocalDateTime from = LocalDateTime.now().minusDays(1);
        LocalDateTime to = LocalDateTime.now();
        Customer customer = new Customer();
        customer.setId(customerId);

        List<Order> expectedOrders = Arrays.asList(
                Order.builder().customerId(customerId).build(),
                Order.builder().customerId(customerId).build());

        when(customerService.getCustomerByUsername(username)).thenReturn(customer);
        when(orderService.listOrders(customerId, from, to)).thenReturn(expectedOrders);

        ResponseEntity<?> response = orderController.listOrders(customerId, from, to);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedOrders, response.getBody());
        verify(orderService).listOrders(customerId, from, to);
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void listOrders_NonAdminUser_OtherAccount_ReturnsForbidden() {
        Long requestedCustomerId = 1L;
        Long actualCustomerId = 2L;
        String username = "testuser";
        LocalDateTime from = LocalDateTime.now().minusDays(1);
        LocalDateTime to = LocalDateTime.now();
        Customer customer = new Customer();
        customer.setId(actualCustomerId);

        when(customerService.getCustomerByUsername(username)).thenReturn(customer);

        ResponseEntity<?> response = orderController.listOrders(requestedCustomerId, from, to);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("You can only list orders for your own account", response.getBody());
        verify(orderService, never()).listOrders(any(), any(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteOrder_AdminUser_ReturnsSuccess() {
        Long orderId = 1L;
        Order order = Order.builder().id(orderId).build();

        when(orderService.getOrderById(orderId)).thenReturn(order);
        when(orderService.deleteOrder(orderId)).thenReturn(true);

        ResponseEntity<?> response = orderController.deleteOrder(orderId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Order deleted", response.getBody());
        verify(orderService).deleteOrder(orderId);
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void deleteOrder_NonAdminUser_OwnOrder_ReturnsSuccess() {
        Long orderId = 1L;
        Long customerId = 1L;
        String username = "testuser";
        Order order = Order.builder().id(orderId).customerId(customerId).build();
        Customer customer = new Customer();
        customer.setId(customerId);

        when(orderService.getOrderById(orderId)).thenReturn(order);
        when(customerService.getCustomerByUsername(username)).thenReturn(customer);
        when(orderService.deleteOrder(orderId)).thenReturn(true);

        ResponseEntity<?> response = orderController.deleteOrder(orderId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Order deleted", response.getBody());
        verify(orderService).deleteOrder(orderId);
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void deleteOrder_NonAdminUser_OtherOrder_ReturnsForbidden() {
        Long orderId = 1L;
        Long orderCustomerId = 1L;
        Long actualCustomerId = 2L;
        String username = "testuser";
        Order order = Order.builder().id(orderId).customerId(orderCustomerId).build();
        Customer customer = new Customer();
        customer.setId(actualCustomerId);

        when(orderService.getOrderById(orderId)).thenReturn(order);
        when(customerService.getCustomerByUsername(username)).thenReturn(customer);

        ResponseEntity<?> response = orderController.deleteOrder(orderId);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("You can only delete your own orders", response.getBody());
        verify(orderService, never()).deleteOrder(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteOrder_OrderNotFound_ReturnsBadRequest() {
        Long orderId = 1L;

        when(orderService.getOrderById(orderId)).thenReturn(null);

        ResponseEntity<?> response = orderController.deleteOrder(orderId);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Order cannot be deleted", response.getBody());
        verify(orderService).deleteOrder(any());
    }
}