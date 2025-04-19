package com.kalkan.brokage.controller;

import com.kalkan.brokage.service.CustomerService;
import com.kalkan.brokage.service.AssetService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/assets")
public class AssetController {
    private final AssetService assetService;
    private final CustomerService customerService;

    public AssetController(AssetService assetService, CustomerService customerService) {
        this.assetService = assetService;
        this.customerService = customerService;
    }

    @GetMapping
    public ResponseEntity<?> listAssets(@RequestParam Long customerId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            String authenticatedUsername = authentication.getName();
            Long actualCustomerId = customerService.getCustomerByUsername(authenticatedUsername).getId();

            if (!actualCustomerId.equals(customerId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You can only list assets for your own account");
            }
        }

        return ResponseEntity.ok(assetService.listAssets(customerId));
    }
}