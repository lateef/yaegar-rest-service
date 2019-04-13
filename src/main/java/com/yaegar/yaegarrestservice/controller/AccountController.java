package com.yaegar.yaegarrestservice.controller;

import com.yaegar.yaegarrestservice.model.Account;
import com.yaegar.yaegarrestservice.model.User;
import com.yaegar.yaegarrestservice.service.AccountService;
import com.yaegar.yaegarrestservice.service.CompanyService;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonMap;

@RestController
@RequestMapping(value = "/secure-api")
public class AccountController {
    private CompanyService companyService;
    private AccountService accountService;

    public AccountController(CompanyService companyService, AccountService accountService) {
        this.companyService = companyService;
        this.accountService = accountService;
    }

    @RequestMapping(value = "/add-account", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Account>> addAccount(@RequestBody final Account account) {
        Account account1 = accountService.addAccount(account);
        return ResponseEntity.ok().body(singletonMap("success", account1));
    }

    @RequestMapping(value = "/get-leaf-accounts", method = RequestMethod.GET)
    public ResponseEntity<Map<String, List<Account>>> getLeafAccounts(@RequestParam final Long chartOfAccountsId, Principal principal, ModelMap model) {
        final User user = (User) model.get("user");
        List<Account> accounts = new ArrayList<>();
        if (companyService.userCanAccessChartOfAccounts(user, chartOfAccountsId)) {
            accounts = accountService.getLeafAccounts(chartOfAccountsId);
        }
        return ResponseEntity.ok().body(singletonMap("success", accounts));
    }

    @RequestMapping(value = "/get-account", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Account>> getAccount(@RequestParam final Long accountId) {
        final Account account = accountService.findById(accountId)
                .orElseThrow(NullPointerException::new);//this should be able to return segregated data
        return ResponseEntity.ok().body(singletonMap("success", account));
    }

    @RequestMapping(value = "/compute-accounts", method = RequestMethod.POST)
    public ResponseEntity<Map<String, List<Account>>> computeAccounts(@RequestBody final List<Account> accounts) {
        final List<Account> accounts1 = accountService.computeAccountTotal(accounts);
        return ResponseEntity.ok().body(singletonMap("success", accounts1));
    }
}
