package com.pro.miniSpring;

import com.pro.miniSpring.demo.User;
import com.pro.miniSpring.demo.UserController;
import com.pro.miniSpring.init.ApplicationContext;

public class Test {



    public static void main(String[] args) {
        ApplicationContext context = new ApplicationContext();
        UserController userController = context.getBean(UserController.class);
        User user = userController.getUser("1");
        System.out.println(user.getUserName());
    }
}
