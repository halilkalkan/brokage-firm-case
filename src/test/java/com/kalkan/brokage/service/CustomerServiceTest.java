package com.kalkan.brokage.service;

import com.kalkan.brokage.model.Customer;
import com.kalkan.brokage.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        customerService = new CustomerService(customerRepository);
    }

    @Test
    void getCustomerByUsername_WhenCustomerExists_ShouldReturnCustomer() {
        String username = "testuser";
        Customer expectedCustomer = Customer.builder()
                .id(1L)
                .username(username)
                .password("password123")
                .build();

        when(customerRepository.findByUsername(username)).thenReturn(expectedCustomer);

        Customer actualCustomer = customerService.getCustomerByUsername(username);

        assertNotNull(actualCustomer);
        assertEquals(expectedCustomer.getId(), actualCustomer.getId());
        assertEquals(expectedCustomer.getUsername(), actualCustomer.getUsername());
        assertEquals(expectedCustomer.getPassword(), actualCustomer.getPassword());
        verify(customerRepository, times(1)).findByUsername(username);
    }

    @Test
    void getCustomerByUsername_WhenCustomerDoesNotExist_ShouldReturnNull() {
        String username = "nonexistentuser";
        when(customerRepository.findByUsername(username)).thenReturn(null);

        Customer actualCustomer = customerService.getCustomerByUsername(username);

        assertNull(actualCustomer);
        verify(customerRepository, times(1)).findByUsername(username);
    }
}
