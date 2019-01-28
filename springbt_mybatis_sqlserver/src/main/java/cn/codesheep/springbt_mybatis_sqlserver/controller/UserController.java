package cn.codesheep.springbt_mybatis_sqlserver.controller;

import cn.codesheep.springbt_mybatis_sqlserver.entity.User;
import cn.codesheep.springbt_mybatis_sqlserver.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UserController {

    @Autowired
    private IUserService userService;

    @RequestMapping(value = "/getAllUser", method = RequestMethod.GET)
    public List<User> getAllUser() {
        return userService.getAllUsers();
    }

    @RequestMapping(value = "/addUser", method = RequestMethod.POST)
    public int addUser( @RequestBody User user ) {
        return userService.addUser( user );
    }

    @RequestMapping(value = "/deleteUser", method = RequestMethod.POST)
    public int deleteUser( @RequestBody User user ) {
        return userService.deleteUser( user );
    }

}
