package com.yaegar.yaegarrestservice.controller;

import com.yaegar.yaegarrestservice.model.User;
import com.yaegar.yaegarrestservice.provider.Authenticator;
import com.yaegar.yaegarrestservice.service.UserService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Map;

import static java.util.Collections.singletonMap;

@RestController
public class UserController {
    private final Authenticator authenticator;
    private final UserService userService;

    public UserController(Authenticator authenticator, UserService userService) {
        this.authenticator = authenticator;
        this.userService = userService;
    }

    @RequestMapping(value = "/do-nothing", method = RequestMethod.GET)
    public ResponseEntity<User> doNothing() {
        return ResponseEntity.ok(new User());
    }

    @RequestMapping(value = "/create-account", method = RequestMethod.POST)
    public ResponseEntity<Map<String, User>> createAccount(@RequestBody final User user) {
        return ResponseEntity.ok().body(userService.createAccount(user));
    }

    @RequestMapping(value = "/log-in", method = RequestMethod.POST)
    public ResponseEntity<Map<String, User>> logIn(@RequestBody final User user) {
        return ResponseEntity.ok().body(userService.logIn(user));
    }

    @RequestMapping(value = "/confirm-account", method = RequestMethod.POST)
    public ResponseEntity<Map<String, User>> confirmAccount(@RequestBody final User user) {
        final Map<String, User> userResponse = userService.confirmAccount(user);
        final HttpHeaders headers = authenticator.getAuthenticatedUser(userResponse.values().stream().findFirst().get());
        return ResponseEntity.ok().headers(headers).body(userResponse);
    }

    @RequestMapping(value = "/secure-api/get-logged-in-user", method = RequestMethod.GET)
    public ResponseEntity<Map<String, User>> getLoggedInUser(Principal principal, ModelMap model) {
        final User user = (User) model.get("user");
        return ResponseEntity.ok().body(singletonMap("success", user));
    }
}
