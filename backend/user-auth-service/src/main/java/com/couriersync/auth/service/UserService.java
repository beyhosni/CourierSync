package com.couriersync.auth.service;

import com.couriersync.auth.model.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class UserService implements UserDetailsService {

    private final List<User> users = new ArrayList<>();

    public UserService() {
        // Initialize with a default admin user
        User admin = User.builder()
                .id(UUID.randomUUID())
                .email("admin@couriersync.com")
                .passwordHash("$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi") // admin
                .firstName("Admin")
                .lastName("User")
                .role(User.Role.ADMIN)
                .status(User.Status.ACTIVE)
                .build();
        users.add(admin);
    }

    @Override
    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        return users.stream()
                .filter(u -> u.getEmail().equals(username))
                .findFirst()
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    public User findByEmail(String email) {
        return users.stream()
                .filter(u -> u.getEmail().equals(email))
                .findFirst()
                .orElse(null);
    }

    public User save(User user) {
        users.add(user);
        return user;
    }

    public List<User> findAll() {
        return new ArrayList<>(users);
    }

    public User findById(UUID id) {
        return users.stream()
                .filter(u -> u.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public void deleteById(UUID id) {
        users.removeIf(u -> u.getId().equals(id));
    }
}
