package com.pro.miniSpring.demo;

import com.pro.miniSpring.init.Auth;
import com.pro.miniSpring.init.Autowired;
import com.pro.miniSpring.init.Controller;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @Auth("user:getUser")
    public User getUser(String userId) {
        return userService.getUserById(userId);
    }
}
