package com.milan.Scheduler;

import com.milan.model.Product;
import com.milan.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductScheduler{

    private final ProductRepository productRepository;

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ProductScheduler.class);

    /**
     * Scheduled task to permanently delete soft-deleted products after 7 days.
     * Runs every day at 12:00 AM server time.
     */
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void deletePermanently() {

        // Calculate the cutoff date (products deleted more than 7 days ago)
        LocalDateTime deleteProductsDay = LocalDateTime.now().minusDays(7);

        // Fetch products where isDeleted = true and deletedOn < 7 days ago
        List<Product> oldDeletedProducts = productRepository.findByIsDeletedTrueAndDeletedOnBefore(deleteProductsDay);

        // Permanently delete them
        productRepository.deleteAll(oldDeletedProducts); // Hard delete
        logger.info("Permanently deleted {} products", oldDeletedProducts.size());
    }
}
