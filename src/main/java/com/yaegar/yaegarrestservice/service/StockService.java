package com.yaegar.yaegarrestservice.service;

import com.yaegar.yaegarrestservice.model.Account;
import com.yaegar.yaegarrestservice.model.Product;
import com.yaegar.yaegarrestservice.model.Stock;
import com.yaegar.yaegarrestservice.model.User;
import com.yaegar.yaegarrestservice.repository.StockRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class StockService {
    private static final Logger LOGGER = LoggerFactory.getLogger(StockService.class);

    private final StockRepository stockRepository;

    public StockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    public Stock addStock(Stock stock, Set<Account> accounts, User user) {
        stock.setAccounts(accounts);
        findByProductAndCompanyStockId(stock.getProduct(), stock.getCompanyStockId())
                .ifPresent(e -> {
                    throw new IllegalStateException("Exception:: Stock already exists");
                });
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
