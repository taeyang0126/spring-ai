package com.lei.learn.spring.ai.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * <p>
 * User
 * </p>
 *
 * @author 伍磊
 */
@Data
@Document(collection = "users")
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    private String id; // MongoDB 内部 _id，由 Spring 自动生成（ObjectId → String）

    @Indexed(unique = true)
    private Integer userId;

    private String username;

    @Indexed(unique = true)
    private String email;

    private String password;

    @Indexed(unique = true)
    private String phoneNumber;

    public User(Integer userId, String username, String email, String password, String phoneNumber) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.password = password;
        this.phoneNumber = phoneNumber;
    }
}
