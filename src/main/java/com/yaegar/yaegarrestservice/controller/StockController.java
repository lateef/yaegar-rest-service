package com.yaegar.yaegarrestservice.controller;

import com.yaegar.yaegarrestservice.model.Stock;
import com.yaegar.yaegarrestservice.service.StockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static java.util.Collections.singletonMap;

@RestController
@RequestMapping(value = "/secure-api")
public class StockController {
    private static final Logger LOGGER = LoggerFactory.getLogger(StockController.class);

    private StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @RequestMapping(value = "/save-stock", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Stock>> saveStock(@RequestBody final Stock stock) {
        Stock stock1 = null;
        if (Objects.isNull(stock.getId())) {
            stock1 = stockService.addStock(stock);
        }
        return ResponseEntity.ok().body(singletonMap("success", stock1));
    }

    @RequestMapping(value = "/get-company-stock/{companyId}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, List<Stock>>> getStocks(@PathVariable UUID companyId) {
        List<Stock> stock = stockService.findByCompanyStockId(companyId);
        return ResponseEntity.ok().body(singletonMap("success", stock));
    }
}
