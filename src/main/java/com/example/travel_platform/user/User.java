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

    private String provider;   // kakao, naver, google
    private String providerId; // SNS 고유 식별값

    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private boolean active = true;

    public static User create(String username, String password, String email, String tel, String role) {
        User user = new User();
        user.username = username;
        user.password = password;
        user.email = email;
        user.tel = tel;
        user.role = role;
        return user;
    }

    // SNS 유저 전용 생성 메서드
    public static User createSNS(String username, String email, String provider, String providerId) {
        User user = new User();
        user.username = username;
        user.password = java.util.UUID.randomUUID().toString(); // 랜덤 비밀번호
        user.email = email;
        user.provider = provider;
        user.providerId = providerId;
        user.role = "USER";
        return user;
    }

    public boolean isAdmin() {
        return "ADMIN".equals(this.role);
    }

    public void changePassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "User [id=" + id + ", username=" + username + ", email=" + email
                + ", tel=" + tel + ", role=" + role + ", provider=" + provider + ", createdAt=" + createdAt + "]";

    }

}
