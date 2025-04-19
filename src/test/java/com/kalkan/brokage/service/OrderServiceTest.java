package com.kalkan.brokage.service;

import com.kalkan.brokage.model.Order;
import com.kalkan.brokage.model.Asset;
import com.kalkan.brokage.repository.OrderRepository;
import com.kalkan.brokage.repository.AssetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private AssetRepository assetRepository;

    @InjectMocks
    private OrderService orderService;

    private Order testOrder;
    private Order testSellOrder;
    private Asset testAsset;
    private Asset testTryAsset;

    @BeforeEach
    void setUp() {
        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setCustomerId(1L);
        testOrder.setAssetName("BTC");
        testOrder.setSize(10.0);
        testOrder.setPrice(100.0);
        testOrder.setOrderSide(Order.Side.BUY);

        testSellOrder = new Order();
        testSellOrder.setId(2L);
        testSellOrder.setCustomerId(1L);
        testSellOrder.setAssetName("BTC");
        testSellOrder.setSize(5.0);
        testSellOrder.setPrice(200.0);
        testSellOrder.setOrderSide(Order.Side.SELL);

        testAsset = new Asset();
        testAsset.setCustomerId(1L);
        testAsset.setAssetName("BTC");
        testAsset.setSize(100.0);
        testAsset.setUsableSize(100.0);

        testTryAsset = new Asset();
        testTryAsset.setCustomerId(1L);
        testTryAsset.setAssetName("TRY");
        testTryAsset.setSize(10000.0);
        testTryAsset.setUsableSize(10000.0);
    }

    @Test
    void createOrder_SuccessfulBuyOrder() {
        when(assetRepository.findByCustomerIdAndAssetName(1L, "TRY")).thenReturn(testTryAsset);
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        Order result = orderService.createOrder(testOrder);

        assertNotNull(result);
        assertEquals(Order.Status.PENDING, result.getStatus());
        assertNotNull(result.getCreateDate());
        assertEquals(9000, testTryAsset.getUsableSize());
        verify(assetRepository).save(testTryAsset);
        verify(orderRepository).save(testOrder);
    }

    @Test
    void createOrder_SuccessfulSellOrder() {
        testOrder.setOrderSide(Order.Side.SELL);
        when(assetRepository.findByCustomerIdAndAssetName(1L, "BTC")).thenReturn(testAsset);
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        Order result = orderService.createOrder(testOrder);

        assertNotNull(result);
        assertEquals(Order.Status.PENDING, result.getStatus());
        assertNotNull(result.getCreateDate());
        assertEquals(90, testAsset.getUsableSize()); // 100 - 10
        verify(assetRepository).save(testAsset);
        verify(orderRepository).save(testOrder);
    }

    @Test
    void createOrder_ThrowsExceptionWhenNotEnoughAssetsForSell() {
        testOrder.setOrderSide(Order.Side.SELL);
        testAsset.setUsableSize(5.0); // Less than order size
        when(assetRepository.findByCustomerIdAndAssetName(1L, "BTC")).thenReturn(testAsset);

        assertThrows(IllegalStateException.class, () -> orderService.createOrder(testOrder));
        assertEquals(5, testAsset.getUsableSize()); // Size should remain unchanged
        verify(assetRepository, never()).save(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void createOrder_ThrowsExceptionWhenNotEnoughTRYForBuy() {
        testTryAsset.setUsableSize(500.0); // Less than required amount (1000)
        when(assetRepository.findByCustomerIdAndAssetName(1L, "TRY")).thenReturn(testTryAsset);

        assertThrows(IllegalStateException.class, () -> orderService.createOrder(testOrder));
        assertEquals(500, testTryAsset.getUsableSize()); // Size should remain unchanged
        verify(assetRepository, never()).save(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void listOrders_ReturnsOrdersInDateRange() {
        LocalDateTime from = LocalDateTime.now().minusDays(1);
        LocalDateTime to = LocalDateTime.now();
        List<Order> expectedOrders = Arrays.asList(testOrder);
        when(orderRepository.findByCustomerIdAndCreateDateBetween(1L, from, to)).thenReturn(expectedOrders);

        List<Order> result = orderService.listOrders(1L, from, to);

        assertEquals(expectedOrders, result);
        verify(orderRepository).findByCustomerIdAndCreateDateBetween(1L, from, to);
    }

    @Test
    void deleteOrder_SuccessfulCancellation() {
        testOrder.setStatus(Order.Status.PENDING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(assetRepository.findByCustomerIdAndAssetName(1L, "TRY")).thenReturn(testTryAsset);
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        boolean result = orderService.deleteOrder(1L);

        assertTrue(result);
        assertEquals(Order.Status.CANCELED, testOrder.getStatus());
        assertEquals(11000, testTryAsset.getUsableSize());
        verify(orderRepository).save(testOrder);
        verify(assetRepository).save(testTryAsset);
    }

    @Test
    void deleteOrder_SuccessfulCancellationOfSellOrder() {
        testSellOrder.setStatus(Order.Status.PENDING);
        when(orderRepository.findById(2L)).thenReturn(Optional.of(testSellOrder));
        when(assetRepository.findByCustomerIdAndAssetName(1L, "BTC")).thenReturn(testAsset);
        when(orderRepository.save(any(Order.class))).thenReturn(testSellOrder);

        boolean result = orderService.deleteOrder(2L);

        assertTrue(result);
        assertEquals(Order.Status.CANCELED, testSellOrder.getStatus());
        assertEquals(105, testAsset.getUsableSize());
        verify(orderRepository).save(testSellOrder);
        verify(assetRepository).save(testAsset);
    }

    @Test
    void deleteOrder_ReturnsFalseForNonExistentOrder() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        boolean result = orderService.deleteOrder(1L);

        assertFalse(result);
        verify(orderRepository, never()).save(any());
    }

    @Test
    void deleteOrder_ReturnsFalseForNonPendingOrder() {
        testOrder.setStatus(Order.Status.MATCHED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        boolean result = orderService.deleteOrder(1L);

        assertFalse(result);
        verify(orderRepository, never()).save(any());
    }
}
