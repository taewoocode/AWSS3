package com.example.tests3.controller;

import com.example.tests3.entity.User;
import com.example.tests3.repository.UserRepository;
import com.example.tests3.service.StorageService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StorageService storageService;

    @GetMapping("/")
    public String test() {
        return "Server Running!";
    }

    @PostMapping("/user")
    public User saveUser(@RequestBody User user) {
        return this.userRepository.save(user);
    }

    @PostMapping("/login")
    public ResponseEntity<String> loginUser(@RequestBody User loginRequest) {
        User user = userRepository.findUserByEmail(loginRequest.getEmail());
        if (user != null && user.getPassword().equals(loginRequest.getPassword())) {
            // 로그인 성공
            storageService.loginAndSetBucketName(loginRequest.getEmail());
            return ResponseEntity.ok("Login successful. User ID: " + user.getUserId());
        } else {
            // 로그인 실패
            return ResponseEntity.badRequest().body("오류발생!");
        }
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