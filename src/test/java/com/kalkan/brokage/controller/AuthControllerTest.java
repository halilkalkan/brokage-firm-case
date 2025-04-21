package com.kalkan.brokage.controller;

import com.kalkan.brokage.model.Customer;
import com.kalkan.brokage.repository.CustomerRepository;
import com.kalkan.brokage.service.CustomerDetailsService;
import com.kalkan.brokage.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class AuthControllerTest {

        @Mock
        private AuthenticationManager authenticationManager;

        @Mock
        private CustomerDetailsService customerDetailsService;

        @Mock
        private JwtUtil jwtUtil;

        @Mock
        private CustomerRepository customerRepository;

        @Mock
        private PasswordEncoder passwordEncoder;

        @InjectMocks
        private AuthController authController;

        private LoginRequest validLoginRequest;
        private Customer validCustomer;
        private UserDetails mockUserDetails;

        @BeforeEach
        void setUp() {
                validLoginRequest = new LoginRequest();
                validLoginRequest.setUsername("testuser");
                validLoginRequest.setPassword("password");

                validCustomer = new Customer();
                validCustomer.setUsername("testuser");
                validCustomer.setPassword("encodedPassword");

                mockUserDetails = new User("testuser", "password", Collections.emptyList());
        }

        @Test
        void login_WithValidCredentials_ReturnsJwtToken() {
                when(authenticationManager.authenticate(any()))
                                .thenReturn(new UsernamePasswordAuthenticationToken(validLoginRequest.getUsername(),
                                                validLoginRequest.getPassword()));
                when(customerDetailsService.loadUserByUsername(validLoginRequest.getUsername()))
                                .thenReturn(mockUserDetails);
                when(jwtUtil.generateToken(mockUserDetails))
                                .thenReturn("mockJwtToken");

                ResponseEntity<?> response = authController.login(validLoginRequest);

                assertNotNull(response);
                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertTrue(response.getBody() instanceof AuthResponse);
                assertEquals("mockJwtToken", ((AuthResponse) response.getBody()).getToken());
        }

        @Test
        void login_WithInvalidCredentials_ReturnsUnauthorized() {
                when(authenticationManager.authenticate(any()))
                                .thenThrow(new BadCredentialsException("Invalid credentials"));

                ResponseEntity<?> response = authController.login(validLoginRequest);

                assertNotNull(response);
                assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
                assertEquals("Invalid credentials", response.getBody());
        }

        @Test
        void signup_WithNewUsername_ReturnsSuccessResponse() {
                when(customerRepository.existsByUsername(validCustomer.getUsername()))
                                .thenReturn(false);
                when(passwordEncoder.encode(validCustomer.getPassword()))
                                .thenReturn("encodedPassword");
                when(customerRepository.save(any(Customer.class)))
                                .thenReturn(validCustomer);

                ResponseEntity<?> response = authController.registerUser(validCustomer);

                assertNotNull(response);
                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertTrue(response.getBody() instanceof SignupResponse);
                assertEquals(validCustomer.getUsername(), ((SignupResponse) response.getBody()).getUsername());
        }

        @Test
        void signup_WithExistingUsername_ReturnsBadRequest() {
                when(customerRepository.existsByUsername(validCustomer.getUsername()))
                                .thenReturn(true);

                ResponseEntity<?> response = authController.registerUser(validCustomer);

                assertNotNull(response);
                assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                assertEquals("Error: Username is already taken!", response.getBody());
        }
}