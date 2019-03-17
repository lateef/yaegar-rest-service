package com.yaegar.yaegarrestservice.service;

import com.yaegar.yaegarrestservice.model.*;
import com.yaegar.yaegarrestservice.repository.StockRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class StockService {
    private static final Logger LOGGER = LoggerFactory.getLogger(StockService.class);

    private final AccountService accountService;
    private final StockRepository stockRepository;

    public StockService(AccountService accountService, StockRepository stockRepository) {
        this.accountService = accountService;
        this.stockRepository = stockRepository;
    }

    @Transactional
    public Stock addStock(Stock stock, User user) {
        return addStock(
                stock.getProduct(),
                stock.getCompanyStockId(),
                stock.getCostPrice(),
                stock.getSellPrice(),
                stock.getSku(),
                stock.getQuantity(),
                stock.getLocation(),
                user
        );
    }

    @Transactional
    public Stock addStock(
            Product product,
            Long companyStockId,
            BigDecimal costPrice,
            BigDecimal sellPrice,
            String sku,
            Double quantity,
            Location location,
            User user
    ) {
        findByProductAndCompanyStockId(product, companyStockId)
                .ifPresent(e -> {
                    throw new IllegalStateException("Exception:: Stock already exists");
                });

        Stock stock = new Stock();
        stock.setProduct(product);
        stock.setCompanyStockId(companyStockId);
        stock.setLocation(location);
        stock.setCostPrice(costPrice);
        stock.setSellPrice(sellPrice);
        stock.setQuantity(quantity);
        stock.setSku(sku);
        stock.setQuantity(quantity);
        stock.setCreatedBy(user.getId());
        stock.setUpdatedBy(user.getId());


        final Set<Account> purchasesAndSalesAccounts = accountService.createStockAccounts(stock, user);
        stock.setAccounts(purchasesAndSalesAccounts);
        stock.setCreatedBy(user.getId());
        stock.setUpdatedBy(user.getId());
        return stockRepository.save(stock);
    }

    public List<Stock> findByAccountsIn(List<Account> accounts) {
        return stockRepository.findByAccountsIn(accounts);
    }

    public List<Stock> findByCompanyStockId(Long companyStockId) {
        return stockRepository.findByCompanyStockId(companyStockId);
    }

    public Optional<Stock> findByProductAndCompanyStockId(Product product, Long companyStockId) {
        return stockRepository.findByProductAndCompanyStockId(product, companyStockId);
    }
}
