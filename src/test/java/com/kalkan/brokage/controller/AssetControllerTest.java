package com.kalkan.brokage.controller;

import com.kalkan.brokage.model.Customer;
import com.kalkan.brokage.model.Asset;
import com.kalkan.brokage.service.AssetService;
import com.kalkan.brokage.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class AssetControllerTest {

    @Mock
    private AssetService assetService;

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private AssetController assetController;

    @Test
    @WithMockUser(roles = "ADMIN")
    void listAssets_AdminUser_ReturnsAssets() {
        Long customerId = 1L;
        List<Asset> expectedAssets = new ArrayList<>();
        Asset asset = new Asset();
        asset.setId(1L);
        asset.setAssetName("AAPL");
        asset.setSize(100.0);
        asset.setUsableSize(100.0);
        asset.setCustomerId(customerId);
        expectedAssets.add(asset);
        
        when(assetService.listAssets(customerId)).thenReturn(expectedAssets);

        ResponseEntity<?> response = assetController.listAssets(customerId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedAssets, response.getBody());
        Asset returnedAsset = ((List<Asset>) response.getBody()).get(0);
        assertEquals("AAPL", returnedAsset.getAssetName());
        assertEquals(100, returnedAsset.getSize());
        assertEquals(100, returnedAsset.getUsableSize());
        assertEquals(customerId, returnedAsset.getCustomerId());
        verify(assetService).listAssets(customerId);
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void listAssets_NonAdminUser_OwnAccount_ReturnsAssets() {
        Long customerId = 1L;
        String username = "testuser";
        Customer customer = new Customer();
        customer.setId(customerId);
        List<Asset> expectedAssets = new ArrayList<>();
        Asset asset = new Asset();
        asset.setId(1L);
        asset.setAssetName("AAPL");
        asset.setSize(100.0);
        asset.setUsableSize(100.0);
        asset.setCustomerId(customerId);
        expectedAssets.add(asset);
        when(customerService.getCustomerByUsername(username)).thenReturn(customer);
        when(assetService.listAssets(customerId)).thenReturn(expectedAssets);

        ResponseEntity<?> response = assetController.listAssets(customerId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedAssets, response.getBody());
        Asset returnedAsset = ((List<Asset>) response.getBody()).get(0);
        assertEquals("AAPL", returnedAsset.getAssetName());
        assertEquals(100, returnedAsset.getSize());
        assertEquals(100, returnedAsset.getUsableSize());
        assertEquals(customerId, returnedAsset.getCustomerId());
        verify(assetService).listAssets(customerId);
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void listAssets_NonAdminUser_OtherAccount_ReturnsForbidden() {
        Long requestedCustomerId = 1L;
        Long actualCustomerId = 2L;
        String username = "testuser";
        Customer customer = new Customer();
        customer.setId(actualCustomerId);
        
        when(customerService.getCustomerByUsername(username)).thenReturn(customer);

        ResponseEntity<?> response = assetController.listAssets(requestedCustomerId);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("You can only list assets for your own account", response.getBody());
        verify(assetService, never()).listAssets(any());
    }
} 