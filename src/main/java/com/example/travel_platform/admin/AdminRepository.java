package com.example.travel_platform.admin;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.travel_platform.user.User;

public interface AdminRepository extends JpaRepository<User, Integer> {

    List<User> findAllByOrderByCreatedAtDescIdDesc();

    // 비활성 유저 count
    long countByActiveFalse();

    List<User> findByActiveOrderByCreatedAtDescIdDesc(boolean active);

    @Query("""
            select u from User u
            where
                u.username like concat('%', :keyword, '%')
                or u.email like concat('%', :keyword, '%')
            order by u.createdAt desc, u.id desc
            """)
    List<User> findByKeyword(@Param("keyword") String keyword);

    @Query("""
            select u from User u
            where u.active = :active
            and (
                u.username like concat('%', :keyword, '%')
                or u.email like concat('%', :keyword, '%')
            )
            order by u.createdAt desc, u.id desc
            """)
    List<User> findByActiveAndKeyword(@Param("active") boolean active, @Param("keyword") String keyword);

    @Query("""
            select u
            from User u
            order by u.createdAt desc, u.id desc
            """)
    List<User> findRecentUsers(Pageable pageable);
}
