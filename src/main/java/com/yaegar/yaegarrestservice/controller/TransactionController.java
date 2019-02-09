package com.yaegar.yaegarrestservice.controller;

import com.yaegar.yaegarrestservice.model.Transaction;
import com.yaegar.yaegarrestservice.model.User;
import com.yaegar.yaegarrestservice.service.TransactionService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

import static com.yaegar.yaegarrestservice.util.AuthenticationUtils.getAuthenticatedUser;
import static java.util.Collections.singletonMap;

@RestController
public class TransactionController {
    private TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @RequestMapping(value = "/add-transaction", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Transaction>> addTransaction(@RequestBody final Transaction transaction, ModelMap model, HttpServletRequest httpServletRequest) {
        final User user = (User) model.get("user");
        HttpHeaders headers = getAuthenticatedUser(user);
        Transaction transaction1 = transactionService.addTransaction(transaction, user);
        return ResponseEntity.ok().headers(headers).body(singletonMap("success", transaction1));
    }

    @RequestMapping(value = "/get-account-transactions", method = RequestMethod.GET)
    public ResponseEntity<Map<String, List<Transaction>>> getAccountTransactions(@RequestParam final Long accountId, ModelMap model, HttpServletRequest httpServletRequest) {
        final User user = (User) model.get("user");
        HttpHeaders headers = getAuthenticatedUser(user);
        List<Transaction>  transactions = transactionService.getAccountTransactions(accountId);
        return ResponseEntity.ok().headers(headers).body(singletonMap("success", transactions));
    }
}