package com.fifa.fifarest.service;

import com.fifa.fifarest.domain.User;
import com.fifa.fifarest.exception.ResourceNotFoundException;
import com.fifa.fifarest.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("No account found for email: " + email));
    }
}
