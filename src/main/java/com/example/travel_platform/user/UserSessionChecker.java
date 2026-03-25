package com.example.travel_platform.user;

import java.util.Optional;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserSessionChecker {

    private final UserQueryRepository userQueryRepository;

    public boolean isBlocked(SessionUser sessionUser) {
        if (sessionUser == null) {
            return false;
        }

        if (sessionUser.isAdmin()) {
            return false;
        }

        Optional<User> user = userQueryRepository.findUser(sessionUser.getId());
        if (user.isEmpty()) {
            return true;
        }

        return !user.get().isActive();
    }
}
