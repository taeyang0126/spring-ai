package com.lei.learn.spring.ai.repository;

import com.lei.learn.spring.ai.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * <p>
 * DataMongoTest
 * </p>
 *
 * @author 伍磊
 */
@DataMongoTest
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldInsertUsersIntoMongoDB() {
        // userRepository.deleteAll();

        // 创建测试用户
        User user1 = new User(1001, "alice", "alice@example.com", "pass123", "13800000001");
        User user2 = new User(1002, "bob", "bob@example.com", "pass456", "13800000002");

        // 保存到 MongoDB
        User saved1 = userRepository.save(user1);
        User saved2 = userRepository.save(user2);

        assertThat(saved1.getId()).isNotNull();
        assertThat(saved1.getUserId()).isEqualTo(1001);

        var found = userRepository.findByUserId(1001);
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("alice@example.com");

        long count = userRepository.count();
        assertThat(count).isEqualTo(2);
    }

}
