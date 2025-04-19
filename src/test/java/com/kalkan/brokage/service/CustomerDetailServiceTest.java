package com.kalkan.brokage.service;

import com.kalkan.brokage.model.Customer;
import com.kalkan.brokage.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomerDetailServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerDetailsService customerDetailsService;

    private Customer testCustomer;

    @BeforeEach
    void setUp() {
        testCustomer = new Customer();
        testCustomer.setUsername("testuser");
        testCustomer.setPassword("testpass");
    }

    @Test
    void loadUserByUsername_WhenUserExists_ShouldReturnUserDetails() {
        when(customerRepository.findByUsername("testuser")).thenReturn(testCustomer);

        UserDetails userDetails = customerDetailsService.loadUserByUsername("testuser");

        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertEquals("testpass", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().isEmpty());
        verify(customerRepository, times(1)).findByUsername("testuser");
    }

    @Test
    void loadUserByUsername_WhenUserDoesNotExist_ShouldThrowUsernameNotFoundException() {
        when(customerRepository.findByUsername("nonexistent")).thenReturn(null);

        UsernameNotFoundException exception = assertThrows(
            UsernameNotFoundException.class,
            () -> customerDetailsService.loadUserByUsername("nonexistent")
        );
        
        assertEquals("User not found: nonexistent", exception.getMessage());
        verify(customerRepository, times(1)).findByUsername("nonexistent");
    }
}
