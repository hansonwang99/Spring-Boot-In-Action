package cn.codesheep.springbt_ehcache.service;

import cn.codesheep.springbt_ehcache.entity.User;
import cn.codesheep.springbt_ehcache.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    public List<User> getUsers() {
        return userMapper.getUsers();
    }

    public int addUser( User user ) {
        return userMapper.addUser(user);
    }

    @Cacheable(value = "user", key = "#userName")
    public List<User> getUsersByName( String userName ) {
        List<User> users = userMapper.getUsersByName( userName );
        System.out.println( "从数据库读取，而非读取缓存！" );
        return users;
    }
}
