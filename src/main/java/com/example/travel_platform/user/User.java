package com.example.travel_platform.user;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private UserAuthProvider provider;   // kakao, naver, google
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
        user.role = UserRole.fromCode(role);
        return user;
    }

    // SNS 유저 전용 생성 메서드
    public static User createSNS(String username, String email, String provider, String providerId) {
        User user = new User();
        user.username = username;
        user.password = java.util.UUID.randomUUID().toString(); // 랜덤 비밀번호
        user.email = email;
        user.provider = UserAuthProvider.fromCode(provider);
        user.providerId = providerId;
        user.role = UserRole.USER;
        return user;
    }

    public boolean isAdmin() {
        return this.role == UserRole.ADMIN;
    }

    public void changePassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role == null ? null : role.getCode();
    }

    public void setRole(String roleCode) {
        this.role = UserRole.fromCode(roleCode);
    }

    public UserRole getRoleType() {
        return role;
    }

    public void setRoleType(UserRole role) {
        this.role = role;
    }

    public String getProvider() {
        return provider == null ? null : provider.getCode();
    }

    public void setProvider(String providerCode) {
        this.provider = UserAuthProvider.fromCodeOrNull(providerCode);
    }

    public UserAuthProvider getProviderType() {
        return provider;
    }

    public void setProviderType(UserAuthProvider provider) {
        this.provider = provider;
    }

    @Override
    public String toString() {
        return "User [id=" + id + ", username=" + username + ", email=" + email
                + ", tel=" + tel + ", role=" + getRole() + ", provider=" + getProvider() + ", createdAt=" + createdAt + "]";

    }

}
