package com.kalkan.brokage.service;

import java.util.List;
import com.kalkan.brokage.model.Asset;
import com.kalkan.brokage.repository.AssetRepository;
import org.springframework.stereotype.Service;

@Service
public class AssetService {
    private final AssetRepository assetRepository;

    public AssetService(AssetRepository assetRepository) {
        this.assetRepository = assetRepository;
    }

    public List<Asset> listAssets(Long customerId) {
        return assetRepository.findByCustomerId(customerId);
    }
}
