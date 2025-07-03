package com.pro.miniSpring.demo;

import com.pro.miniSpring.init.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class UserDaoImpl implements UserDao {

    List<User> users = new ArrayList<User>();

    @Override
    public User getUserById(String userId) {
        User user = new User();
        user.setUserId("1");
        user.setUserName("wangsan");
        return user;
    }
}
