package com.yaegar.yaegarrestservice.service;

import com.yaegar.yaegarrestservice.model.Account;
import com.yaegar.yaegarrestservice.model.Location;
import com.yaegar.yaegarrestservice.model.Product;
import com.yaegar.yaegarrestservice.model.Stock;
import com.yaegar.yaegarrestservice.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class StockService {
    private final AccountService accountService;
    private final StockRepository stockRepository;

    @Transactional
    public Stock addStock(Stock stock) {
        return addStock(stock.getProduct(), stock.getCompanyStockId(), stock.getCostPrice(), stock.getSellPrice(), stock.getSku(), stock.getQuantity(), stock.getLocation());
    }

    @Transactional
    public Stock addStock(Product product, UUID companyStockId, BigDecimal costPrice, BigDecimal sellPrice, String sku, Double quantity, Location location) {
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

        final Set<Account> purchasesAndSalesAccounts = accountService.createStockAccounts(stock);
        stock.setAccounts(purchasesAndSalesAccounts);
        return stockRepository.save(stock);
    }

    public List<Stock> findByAccountsIn(List<Account> accounts) {
        return stockRepository.findByAccountsIn(accounts);
    }

    public List<Stock> findByCompanyStockId(UUID companyStockId) {
        return stockRepository.findByCompanyStockId(companyStockId);
    }

    public Optional<Stock> findByProductAndCompanyStockId(Product product, UUID companyStockId) {
        return stockRepository.findByProductAndCompanyStockId(product, companyStockId);
    }
}
