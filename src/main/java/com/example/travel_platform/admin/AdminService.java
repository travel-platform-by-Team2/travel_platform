package com.example.travel_platform.admin;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.travel_platform.user.User;
import com.example.travel_platform.user.UserResponse;

import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;

    public List<UserResponse.AdminListDTO> getAdminUsers(Boolean active, String keyword) {
        List<User> users;
        String searchKeyword = keyword;

        if (searchKeyword != null) {
            searchKeyword = searchKeyword.trim();
        }

        if (searchKeyword != null && !searchKeyword.isEmpty()) {
            users = adminRepository.findByUsernameContainingOrEmailContaining(searchKeyword, searchKeyword);
        } else if (active == null) {
            users = adminRepository.findAll();
        } else {
            users = adminRepository.findByActive(active);
        }

        return users.stream()
                .map(user -> UserResponse.AdminListDTO.fromUser(user))
                .toList();
    }

    public long getTotalUserCount() {
        return adminRepository.count();
    }

    public long getInactiveUserCount() {
        return adminRepository.countByActiveFalse();
    }

}
