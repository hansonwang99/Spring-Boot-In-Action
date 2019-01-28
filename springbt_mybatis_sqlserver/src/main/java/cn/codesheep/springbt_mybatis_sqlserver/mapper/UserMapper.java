package cn.codesheep.springbt_mybatis_sqlserver.mapper;

import cn.codesheep.springbt_mybatis_sqlserver.entity.User;

import java.util.List;

public interface UserMapper {

    List<User> getAllUsers();
    int addUser( User user );
    int deleteUser( User user );
}
