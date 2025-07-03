package com.pro.miniSpring.demo;

import com.pro.miniSpring.init.Autowired;
import com.pro.miniSpring.init.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserDao userDao;

    @Override
    public User getUserById(String userId) {
        return userDao.getUserById(userId);
    }
}
