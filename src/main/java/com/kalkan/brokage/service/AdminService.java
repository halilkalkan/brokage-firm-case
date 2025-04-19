package com.kalkan.brokage.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kalkan.brokage.model.Asset;
import com.kalkan.brokage.model.Order;
import com.kalkan.brokage.repository.AssetRepository;
import com.kalkan.brokage.repository.OrderRepository;

@Service
public class AdminService {
    private final OrderRepository orderRepository;
    private final AssetRepository assetRepository;

    public AdminService(OrderRepository orderRepository, AssetRepository assetRepository) {
        this.orderRepository = orderRepository;
        this.assetRepository = assetRepository;
    }

    @Transactional
    public String matchOrder(Long orderId) {
        // Get the order and verify it exists and is pending
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalStateException("Order not found"));

        if (order.getStatus() != Order.Status.PENDING) {
            throw new IllegalStateException("Order is not in PENDING status");
        }

        // Update assets based on order type
        if (order.getOrderSide() == Order.Side.BUY) {
            Asset boughtAsset = assetRepository.findByCustomerIdAndAssetName(
                    order.getCustomerId(),
                    order.getAssetName());

            if (boughtAsset == null) {
                // Create new asset if customer doesn't have it yet
                boughtAsset = new Asset();
                boughtAsset.setCustomerId(order.getCustomerId());
                boughtAsset.setAssetName(order.getAssetName());
                boughtAsset.setSize(order.getSize());
                boughtAsset.setUsableSize(order.getSize());
            } else {
                // Add to existing asset
                boughtAsset.setSize(boughtAsset.getSize() + order.getSize());
                boughtAsset.setUsableSize(boughtAsset.getUsableSize() + order.getSize());
            }
            assetRepository.save(boughtAsset);

            // Decrement TRY asset's size after buy
            Asset tryAsset = assetRepository.findByCustomerIdAndAssetName(
                    order.getCustomerId(),
                    "TRY");
            double cost = order.getSize() * order.getPrice();
            tryAsset.setSize(tryAsset.getSize() - cost);
            assetRepository.save(tryAsset);

        } else if (order.getOrderSide() == Order.Side.SELL) {
            Asset tryAsset = assetRepository.findByCustomerIdAndAssetName(
                    order.getCustomerId(),
                    "TRY");

            double proceeds = order.getSize() * order.getPrice();

            if (tryAsset == null) {
                tryAsset = new Asset();
                tryAsset.setCustomerId(order.getCustomerId());
                tryAsset.setAssetName("TRY");
                tryAsset.setSize(proceeds);
                tryAsset.setUsableSize(proceeds);
            } else {
                tryAsset.setSize(tryAsset.getSize() + proceeds);
                tryAsset.setUsableSize(tryAsset.getUsableSize() + proceeds);
            }
            assetRepository.save(tryAsset);

            // 2. Reduce the sold asset amount from total size
            Asset soldAsset = assetRepository.findByCustomerIdAndAssetName(
                    order.getCustomerId(),
                    order.getAssetName());
            soldAsset.setSize(soldAsset.getSize() - order.getSize());
            assetRepository.save(soldAsset);
        }

        // Update order status
        order.setStatus(Order.Status.MATCHED);
        orderRepository.save(order);

        return "Order successfully matched";
    }
}
