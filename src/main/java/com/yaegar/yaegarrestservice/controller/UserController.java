package com.yaegar.yaegarrestservice.controller;

import com.yaegar.yaegarrestservice.model.User;
import com.yaegar.yaegarrestservice.service.UserService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Map;

import static com.yaegar.yaegarrestservice.util.AuthenticationUtils.getAuthenticatedUser;

@RestController
public class UserController {
    private AuthenticationManager authenticationManager;
    private UserService userService;

    public UserController(AuthenticationManager authenticationManager, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
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
        final HttpHeaders headers = getAuthenticatedUser(userResponse.values().stream().findFirst().get());
        return ResponseEntity.ok().headers(headers).body(userResponse);
    }

    @RequestMapping(value = "/get-logged-in-user", method = RequestMethod.POST)
    public ResponseEntity<Map<String, User>> getLoggedInUser(ModelMap model,  HttpServletRequest httpServletRequest) {
        final User user = (User) model.get("user");
        final HttpHeaders headers = getAuthenticatedUser(user);
        return ResponseEntity.ok().headers(headers).body(Collections.singletonMap("success", user));
    }

    @RequestMapping(value = "/do-nothing", method = RequestMethod.GET)
    public ResponseEntity<User> doNothing() {
        return ResponseEntity.ok(new User());
    }

}
