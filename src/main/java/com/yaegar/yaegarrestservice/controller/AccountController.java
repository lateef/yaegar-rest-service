package com.yaegar.yaegarrestservice.controller;

import com.yaegar.yaegarrestservice.model.Account;
import com.yaegar.yaegarrestservice.model.User;
import com.yaegar.yaegarrestservice.service.AccountService;
import com.yaegar.yaegarrestservice.service.CompanyService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.yaegar.yaegarrestservice.util.AuthenticationUtils.getAuthenticatedUser;
import static java.util.Collections.singletonMap;

@RestController
public class AccountController {
    private CompanyService companyService;
    private AccountService accountService;

    public AccountController(CompanyService companyService, AccountService accountService) {
        this.companyService = companyService;
        this.accountService = accountService;
    }

    @RequestMapping(value = "/add-account", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Account>> addAccount(@RequestBody final Account account, ModelMap model, HttpServletRequest httpServletRequest) {
        final User user = (User) model.get("user");
        HttpHeaders headers = getAuthenticatedUser(user);
        Account account1 = accountService.addAccount(account, user);
        return ResponseEntity.ok().headers(headers).body(singletonMap("success", account1));
    }

    @RequestMapping(value = "/get-leaf-accounts", method = RequestMethod.GET)
    public ResponseEntity<Map<String, List<Account>>> getLeafAccounts(@RequestParam final Long chartOfAccountsId, ModelMap model, HttpServletRequest httpServletRequest) {
        final User user = (User) model.get("user");
        HttpHeaders headers = getAuthenticatedUser(user);
        List<Account> accounts = new ArrayList<>();
        if (companyService.userCanAccessChartOfAccounts(user, chartOfAccountsId)) {
            accounts = accountService.getLeafAccounts(chartOfAccountsId);
        }
        return ResponseEntity.ok().headers(headers).body(singletonMap("success", accounts));
    }
}
