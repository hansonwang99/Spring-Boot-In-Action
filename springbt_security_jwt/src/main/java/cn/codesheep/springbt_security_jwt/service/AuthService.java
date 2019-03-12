package cn.codesheep.springbt_security_jwt.service;

import cn.codesheep.springbt_security_jwt.model.entity.User;

/**
 * @www.codesheep.cn
 * 20190312
 */
public interface AuthService {

    User register( User userToAdd );
    String login( String username, String password );
}
