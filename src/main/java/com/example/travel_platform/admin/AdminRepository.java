package com.example.travel_platform.admin;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.travel_platform.user.User;

public interface AdminRepository extends JpaRepository<User, Integer> {

    List<User> findByActive(boolean active);

    List<User> findByUsernameContainingOrEmailContaining(String username, String email);

    // 비활성 유저 count
    long countByActiveFalse();

    @Query("""
            select u from User u
            where u.active = :active
            and (
                u.username like concat('%', :keyword, '%')
                or u.email like concat('%', :keyword, '%')
            )
            order by u.id desc
            """)
    List<User> findByActiveAndKeyword(@Param("active") boolean active, @Param("keyword") String keyword);
}
