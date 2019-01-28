package cn.codesheep.springbt_ehcache.mapper;

import cn.codesheep.springbt_ehcache.entity.User;

import java.util.List;

public interface UserMapper {

    List<User> getUsers();

    int addUser(User user);

    List<User> getUsersByName( String userName );
}
