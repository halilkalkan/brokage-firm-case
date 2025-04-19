package com.kalkan.brokage.service;

import com.kalkan.brokage.model.Asset;
import com.kalkan.brokage.model.Order;
import com.kalkan.brokage.repository.AssetRepository;
import com.kalkan.brokage.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;

@ExtendWith(MockitoExtension.class)
public class AdminServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private AssetRepository assetRepository;

    private AdminService adminService;

    @BeforeEach
    void setUp() {
        adminService = new AdminService(orderRepository, assetRepository);
    }

    @Test
    void matchOrder_BuyOrder_Successful() {
        Long orderId = 1L;
        Long customerId = 1L;
        String assetName = "AAPL";
        double size = 100.0;
        double price = 150.0;

        Order order = Order.builder()
                .id(orderId)
                .customerId(customerId)
                .assetName(assetName)
                .orderSide(Order.Side.BUY)
                .size(size)
                .price(price)
                .status(Order.Status.PENDING)
                .build();

        Asset tryAsset = new Asset();
        tryAsset.setCustomerId(customerId);
        tryAsset.setAssetName("TRY");
        tryAsset.setSize(20000.0);
        tryAsset.setUsableSize(20000.0);

        when(orderRepository.findById(orderId)).thenReturn(java.util.Optional.of(order));
        when(assetRepository.findByCustomerIdAndAssetName(customerId, "TRY")).thenReturn(tryAsset);
        when(assetRepository.findByCustomerIdAndAssetName(customerId, assetName)).thenReturn(null);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(assetRepository.save(any(Asset.class))).thenAnswer(i -> i.getArguments()[0]);

        String result = adminService.matchOrder(orderId);

        assertEquals("Order successfully matched", result);
        verify(orderRepository).findById(orderId);
        verify(assetRepository).findByCustomerIdAndAssetName(customerId, "TRY");
        verify(assetRepository).findByCustomerIdAndAssetName(customerId, assetName);
        verify(assetRepository, times(2)).save(any(Asset.class));
        verify(orderRepository).save(any(Order.class));

        // Verify TRY asset's size was reduced correctly
        assertEquals(20000.0 - (size * price), tryAsset.getSize());
    }

    @Test
    void matchOrder_SellOrder_Successful() {
        Long orderId = 1L;
        Long customerId = 1L;
        String assetName = "AAPL";
        double size = 100.0;
        double price = 150.0;

        Order order = Order.builder()
                .id(orderId)
                .customerId(customerId)
                .assetName(assetName)
                .orderSide(Order.Side.SELL)
                .size(size)
                .price(price)
                .status(Order.Status.PENDING)
                .build();

        Asset asset = new Asset();
        asset.setCustomerId(customerId);
        asset.setAssetName(assetName);
        asset.setSize(200.0);
        asset.setUsableSize(200.0);

        when(orderRepository.findById(orderId)).thenReturn(java.util.Optional.of(order));
        when(assetRepository.findByCustomerIdAndAssetName(customerId, assetName)).thenReturn(asset);
        when(assetRepository.findByCustomerIdAndAssetName(customerId, "TRY")).thenReturn(null);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(assetRepository.save(any(Asset.class))).thenAnswer(i -> i.getArguments()[0]);

        String result = adminService.matchOrder(orderId);

        assertEquals("Order successfully matched", result);
        verify(orderRepository).findById(orderId);
        verify(assetRepository).findByCustomerIdAndAssetName(customerId, assetName);
        verify(assetRepository).findByCustomerIdAndAssetName(customerId, "TRY");

        ArgumentCaptor<Asset> assetCaptor = ArgumentCaptor.forClass(Asset.class);
        verify(assetRepository, times(2)).save(assetCaptor.capture());

        List<Asset> savedAssets = assetCaptor.getAllValues();
        Asset savedTryAsset = savedAssets.stream()
                .filter(a -> a.getAssetName().equals("TRY"))
                .findFirst()
                .get();

        assertEquals(customerId, savedTryAsset.getCustomerId());
        assertEquals("TRY", savedTryAsset.getAssetName());
        assertEquals(size * price, savedTryAsset.getSize());
        assertEquals(size * price, savedTryAsset.getUsableSize());

        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void matchOrder_OrderNotFound_ThrowsException() {
        Long orderId = 1L;
        when(orderRepository.findById(orderId)).thenReturn(java.util.Optional.empty());

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> adminService.matchOrder(orderId));
        assertEquals("Order not found", exception.getMessage());
        verify(orderRepository).findById(orderId);
        verifyNoMoreInteractions(orderRepository, assetRepository);
    }

    @Test
    void matchOrder_OrderNotPending_ThrowsException() {
        Long orderId = 1L;
        Order order = Order.builder()
                .id(orderId)
                .status(Order.Status.MATCHED)
                .build();

        when(orderRepository.findById(orderId)).thenReturn(java.util.Optional.of(order));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> adminService.matchOrder(orderId));
        assertEquals("Order is not in PENDING status", exception.getMessage());
        verify(orderRepository).findById(orderId);
        verifyNoMoreInteractions(orderRepository, assetRepository);
    }
}
