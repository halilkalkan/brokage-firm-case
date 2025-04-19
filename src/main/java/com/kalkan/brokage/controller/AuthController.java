package com.kalkan.brokage.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kalkan.brokage.model.Customer;
import com.kalkan.brokage.repository.CustomerRepository;
import com.kalkan.brokage.service.CustomerDetailsService;
import com.kalkan.brokage.util.JwtUtil;

import lombok.AllArgsConstructor;
import lombok.Data;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final CustomerDetailsService customerDetailsService;
    private final JwtUtil jwtUtil;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder encoder;

    public AuthController(
            AuthenticationManager authenticationManager,
            CustomerDetailsService customerDetailsService,
            JwtUtil jwtUtil,
            CustomerRepository customerRepository,
            PasswordEncoder encoder) {
        this.authenticationManager = authenticationManager;
        this.customerDetailsService = customerDetailsService;
        this.jwtUtil = jwtUtil;
        this.customerRepository = customerRepository;
        this.encoder = encoder;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid credentials");
        }

        final UserDetails userDetails = customerDetailsService
                .loadUserByUsername(loginRequest.getUsername());

        final String jwt = jwtUtil.generateToken(userDetails);

        return ResponseEntity.ok(new AuthResponse(jwt));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody Customer customer) {
        if (customerRepository.existsByUsername(customer.getUsername())) {
            return ResponseEntity.badRequest().body("Error: Username is already taken!");
        }

        Customer newCustomer = new Customer(
                null,
                customer.getUsername(),
                encoder.encode(customer.getPassword()));
        return ResponseEntity
                .ok(new SignupResponse(customerRepository.save(newCustomer).getId(), customer.getUsername()));
    }
}

@Data
@AllArgsConstructor
class AuthResponse {
    private String token;
}

@Data
@AllArgsConstructor
class SignupResponse {
    private Long id;
    private String username;
}

@Data
class LoginRequest {
    private String username;
    private String password;
}
