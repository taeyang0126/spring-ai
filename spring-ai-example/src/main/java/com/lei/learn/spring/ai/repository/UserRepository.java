package com.lei.learn.spring.ai.repository;

import com.lei.learn.spring.ai.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * <p>
 * UserRepository
 * </p>
 *
 * @author 伍磊
 */
@Repository
public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByUserId(Integer userId);

    boolean existsByUserId(Integer userId);

    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    Optional<User> findByPhoneNumber(String phoneNumber);
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);

}
