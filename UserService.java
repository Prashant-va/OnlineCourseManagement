package com.course.management.service;

import com.course.management.entity.User;
import com.course.management.exception.ResourceNotFoundException;
import com.course.management.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User registerUser(User user) {
        if (user == null || user.getPassword() == null) {
            throw new IllegalArgumentException("User and password must be provided");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Transactional
    public User login(String email, String password) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email must be provided");
        }
        if (password == null) {
            throw new IllegalArgumentException("Password must be provided");
        }
        password = password; // keep as-is; do not trim password characters arbitrarily

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        String stored = user.getPassword();
        if (stored == null) {
            throw new RuntimeException("Stored password is missing for this account");
        }

        boolean looksLikeBcrypt = stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$");

        if (looksLikeBcrypt) {
            if (!passwordEncoder.matches(password, stored)) {
                throw new RuntimeException("Invalid password!");
            }
            return user;
        }

        // legacy plain-text path
        if (stored.equals(password)) {
            user.setPassword(passwordEncoder.encode(password));
            userRepository.save(user);
            return user;
        }

        throw new RuntimeException("Invalid password!");
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public void migratePlainPasswordsToBcrypt() {
        List<User> users = userRepository.findAll();
        for (User u : users) {
            String pw = u.getPassword();
            if (pw != null && !(pw.startsWith("$2a$") || pw.startsWith("$2b$") || pw.startsWith("$2y$"))) {
                u.setPassword(passwordEncoder.encode(pw));
                userRepository.save(u);
            }
        }
    }
}