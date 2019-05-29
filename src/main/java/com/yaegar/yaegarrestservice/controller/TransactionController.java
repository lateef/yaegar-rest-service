package com.yaegar.yaegarrestservice.controller;

import com.yaegar.yaegarrestservice.model.Transaction;
import com.yaegar.yaegarrestservice.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Collections.singletonMap;

@RestController
@RequestMapping(value = "/secure-api")
public class TransactionController {
    private TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Transactional
    @RequestMapping(value = "/add-transaction", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Transaction>> addTransaction(@RequestBody final Transaction transaction) {
        Transaction transaction1 = transactionService.saveTransaction(transaction);
        return ResponseEntity.ok().body(singletonMap("success", transaction1));
    }

    @RequestMapping(value = "/get-account-transactions", method = RequestMethod.GET)
    public ResponseEntity<Map<String, List<Transaction>>> getAccountTransactions(@RequestParam final UUID accountId) {
        List<Transaction> transactions = transactionService.getAccountTransactions(accountId);
        return ResponseEntity.ok().body(singletonMap("success", transactions));
    }
}
