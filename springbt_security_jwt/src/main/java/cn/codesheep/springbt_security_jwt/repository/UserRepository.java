package cn.codesheep.springbt_security_jwt.repository;

import cn.codesheep.springbt_security_jwt.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @www.codesheep.cn
 * 20190312
 */
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername( String username );
}
