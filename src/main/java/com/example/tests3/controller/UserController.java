package com.example.tests3.controller;

import com.example.tests3.entity.User;
import com.example.tests3.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/user")
    public User saveUser(@RequestBody User user) {
        return this.userRepository.save(user);
    }

    @GetMapping("/user/{id}")
    public User getUser(@PathVariable("id") String userId) {
        return userRepository.getUserById(userId);
    }

    @DeleteMapping("/user/{id}")
    public String deleteUser(@PathVariable("id") String userId) {
        return  userRepository.delete(userId);
    }

    @PutMapping("/user/{id}")
    public String updateUser(@PathVariable("id") String userId, @RequestBody User user) {
        return this.userRepository.update(userId,user);
    }
}