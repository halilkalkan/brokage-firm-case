package com.kalkan.brokage.service;

import com.kalkan.brokage.model.Asset;
import com.kalkan.brokage.repository.AssetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AssetServiceTest {

    @Mock
    private AssetRepository assetRepository;

    private AssetService assetService;

    @BeforeEach
    void setUp() {
        assetService = new AssetService(assetRepository);
    }

    @Test
    void listAssets_shouldReturnAssetsForCustomer() {
        Long customerId = 1L;
        List<Asset> expectedAssets = Arrays.asList(
                Asset.builder()
                        .id(1L)
                        .customerId(customerId)
                        .assetName("Asset1")
                        .size(100.0)
                        .usableSize(100.0)
                        .build(),
                Asset.builder()
                        .id(2L)
                        .customerId(customerId)
                        .assetName("Asset2")
                        .size(200.0)
                        .usableSize(200.0)
                        .build());

        when(assetRepository.findByCustomerId(customerId)).thenReturn(expectedAssets);

        List<Asset> actualAssets = assetService.listAssets(customerId);

        assertEquals(expectedAssets, actualAssets);
    }

    @Test
    void listAssets_shouldReturnEmptyListWhenNoAssetsFound() {
        Long customerId = 1L;
        List<Asset> expectedAssets = List.of();

        when(assetRepository.findByCustomerId(customerId)).thenReturn(expectedAssets);

        List<Asset> actualAssets = assetService.listAssets(customerId);

        assertEquals(expectedAssets, actualAssets);
    }
}
