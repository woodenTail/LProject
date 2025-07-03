package com.pro.miniSpring.demo;

import com.pro.miniSpring.init.Service;
import com.pro.miniSpring.security.PermissionService;

import java.util.Arrays;
import java.util.List;

@Service
public class PermissionServiceImpl implements PermissionService {

    List<String> permissions = Arrays.asList("user:getUser", "user:list", "user:add");

    @Override
    public boolean hasPermission(String permission) {
        return permissions.contains(permission);
    }
}
