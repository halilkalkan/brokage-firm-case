package com.kalkan.brokage.service;

import com.kalkan.brokage.model.Order;
import com.kalkan.brokage.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import com.kalkan.brokage.model.Asset;
import com.kalkan.brokage.repository.AssetRepository;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final AssetRepository assetRepository;

    public OrderService(OrderRepository orderRepository, AssetRepository assetRepository) {
        this.orderRepository = orderRepository;
        this.assetRepository = assetRepository;
    }

    @Transactional
    public Order createOrder(Order order) {
        order.setStatus(Order.Status.PENDING);
        order.setCreateDate(LocalDateTime.now());

        // For SELL orders, check if customer has enough assets
        if (order.getOrderSide() == Order.Side.SELL) {
            Asset asset = assetRepository.findByCustomerIdAndAssetName(order.getCustomerId(), order.getAssetName());
            if (asset == null || asset.getUsableSize() < order.getSize()) {
                throw new IllegalStateException("Not enough assets available to sell");
            }

            // Decrement usable size to prevent double selling
            asset.setUsableSize(asset.getUsableSize() - order.getSize());
            assetRepository.save(asset);
        } else if (order.getOrderSide() == Order.Side.BUY) {
            // For buy orders, check and update TRY balance
            Asset tryAsset = assetRepository.findByCustomerIdAndAssetName(
                    order.getCustomerId(),
                    "TRY");

            double requiredAmount = order.getSize() * order.getPrice();

            if (tryAsset == null || tryAsset.getUsableSize() < requiredAmount) {
                throw new IllegalStateException(
                        "Not enough TRY balance for the buy order. Required: " + requiredAmount);
            }

            tryAsset.setUsableSize(tryAsset.getUsableSize() - requiredAmount);
            assetRepository.save(tryAsset);
        }

        Order savedOrder = orderRepository.save(order);
        return savedOrder;
    }

    public List<Order> listOrders(Long customerId, LocalDateTime from, LocalDateTime to) {
        return orderRepository.findByCustomerIdAndCreateDateBetween(customerId, from, to);
    }

    @Transactional
    public boolean deleteOrder(Long id) {
        Optional<Order> order = orderRepository.findById(id);
        if (order.isPresent() && order.get().getStatus() == Order.Status.PENDING) {
            Order orderToCancel = order.get();
            orderToCancel.setStatus(Order.Status.CANCELED);

            // For SELL orders, restore the usable size of the asset
            if (orderToCancel.getOrderSide() == Order.Side.SELL) {
                Asset asset = assetRepository.findByCustomerIdAndAssetName(
                        orderToCancel.getCustomerId(),
                        orderToCancel.getAssetName());

                if (asset != null) {
                    // Increment usable size back since the order is cancelled
                    asset.setUsableSize(asset.getUsableSize() + orderToCancel.getSize());
                    assetRepository.save(asset);
                }
            } else if (orderToCancel.getOrderSide() == Order.Side.BUY) {
                Asset tryAsset = assetRepository.findByCustomerIdAndAssetName(
                        orderToCancel.getCustomerId(),
                        "TRY");

                if (tryAsset != null) {
                    // Return the TRY amount that was reserved for this order
                    double requiredAmount = orderToCancel.getSize() * orderToCancel.getPrice();
                    tryAsset.setUsableSize(tryAsset.getUsableSize() + requiredAmount);
                    assetRepository.save(tryAsset);
                }
            }

            orderRepository.save(orderToCancel);
            return true;
        }
        return false;
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id).orElse(null);
    }

}