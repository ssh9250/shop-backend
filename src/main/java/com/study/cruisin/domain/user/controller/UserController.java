package com.study.cruisin.controller;

import com.study.cruisin.domain.user.entity.User;
import com.study.cruisin.domain.user.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserRepository userRepository;

    @GetMapping("/users/{id}")
    public String findUser(@PathVariable("id") User user) {
        return user.getUsername();
    }

    @PostConstruct
    public void init() {
        userRepository.save(new User("userA"));
    }
}
