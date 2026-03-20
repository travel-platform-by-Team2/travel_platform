package com.example.travel_platform.user;

import java.util.Optional;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserSessionChecker {

    private final UserRepository userRepository;

    public boolean isBlocked(SessionUser sessionUser) {
        if (sessionUser == null) {
            return false;
        }

        if (sessionUser.isAdmin()) {
            return false;
        }

        Optional<User> optUser = userRepository.findById(sessionUser.getId());

        if (optUser.isEmpty()) {
            return true;
        }

        return !optUser.get().isActive();
    }
}
