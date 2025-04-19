package com.kalkan.brokage.service;

import com.kalkan.brokage.model.Customer;
import com.kalkan.brokage.repository.CustomerRepository;

import org.springframework.stereotype.Service;

@Service
public class CustomerService {
    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Customer getCustomerByUsername(String username) {
        return customerRepository.findByUsername(username);
    }
}