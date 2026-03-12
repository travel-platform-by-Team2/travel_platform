package com.example.travel_platform.user;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor // object mapping을 hibernate가 할 때 디폴트 생성자를 new 한다
@Data
@Entity
@Table(name = "user_tb")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(unique = true) // pk, uk(unique) 일때 인덱스를 만들어준다
    private String username; // unique 제약조건 추가 (username은 중복되면 안됨)

    @Column(nullable = false, length = 100) // not null 제약조건 추가 (password는 null이 되면 안됨) length는 10자 이하
    private String password;
    private String email;
    private String tel;
    private String role;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @Override
    public String toString() {
        return "User [id=" + id + ", username=" + username + ", password=" + password + ", email=" + email
                + ", tel=" + tel + ", role=" + role + ", createdAt=" + createdAt + "]";
    }

}
