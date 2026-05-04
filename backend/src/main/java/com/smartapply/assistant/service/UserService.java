package com.smartapply.assistant.service;

import com.smartapply.assistant.entity.AppUser;
import com.smartapply.assistant.repository.AppUserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final AppUserRepository userRepository;

    public UserService(AppUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public AppUser getOrCreateDefaultUser() {
        Optional<AppUser> user = userRepository.findByEmail("test@smartapply.com");
        if (user.isPresent()) {
            return user.get();
        }
        AppUser newUser = new AppUser();
        newUser.setEmail("test@smartapply.com");
        newUser.setName("Test User");
        newUser.setPassword("password");
        return userRepository.save(newUser);
    }

    public AppUser save(AppUser user) {
        return userRepository.save(user);
    }
}
