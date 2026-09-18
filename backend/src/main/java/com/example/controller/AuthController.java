package com.example.controller;

import com.example.dto.UserRecord;
import com.example.entity.User;
import com.example.security.AppUserPrinciple;
import com.example.security.CurrentUser;
import com.example.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final CurrentUser currentUser;
    private final UserService userService;

    @GetMapping("/login-url")
    public Map<String ,String> loginUrl(){
        return Map.of("url","/oauth2/authorization/github");
    }

    @GetMapping("me")
    public ResponseEntity<UserRecord> me(){

        AppUserPrinciple principle = currentUser.require();
        User user = principle.getUser();
        return ResponseEntity.ok(new UserRecord(user.getId(),user.getGithubId(),user.getUname(),user.getDisplayName(),user.getAvatarUrl()));
    }
}
