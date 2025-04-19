package com.kalkan.brokage;

import com.kalkan.brokage.model.Asset;
import com.kalkan.brokage.repository.AssetRepository;
import com.kalkan.brokage.repository.CustomerRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class BrokageApplication {

	public static void main(String[] args) {
		SpringApplication.run(BrokageApplication.class, args);
	}

	@Bean
	CommandLineRunner initDatabase(AssetRepository assetRepository, CustomerRepository customerRepository) {
		return args -> {
			// Sample assets for different customers
			assetRepository.save(Asset.builder()
					.customerId(1L)
					.assetName("TRY")
					.size(10000.0)
					.usableSize(10000.0)
					.build());

			assetRepository.save(Asset.builder()
					.customerId(1L)
					.assetName("BTC")
					.size(1000.0)
					.usableSize(1000.0)
					.build());

			assetRepository.save(Asset.builder()
					.customerId(2L)
					.assetName("TRY")
					.size(25000.0)
					.usableSize(25000.0)
					.build());

			assetRepository.save(Asset.builder()
					.customerId(2L)
					.assetName("BTC")
					.size(2500.0)
					.usableSize(2500.0)
					.build());

			assetRepository.save(Asset.builder()
					.customerId(3L)
					.assetName("TRY")
					.size(50000.0)
					.usableSize(50000.0)
					.build());

			assetRepository.save(Asset.builder()
					.customerId(3L)
					.assetName("BTC")
					.size(5000.0)
					.usableSize(5000.0)
					.build());
		};
	}
}
