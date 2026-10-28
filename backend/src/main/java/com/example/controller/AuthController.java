package com.example.controller;

import com.example.dto.UserRecord;
import com.example.entity.User;
import com.example.security.AppUserPrinciple;
import com.example.security.CurrentUser;
import com.example.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final CurrentUser currentUser;
    private final UserService userService;

    @GetMapping("/login-url")
    public Map<String, String> loginUrl() {
        return Map.of("url", "/oauth2/authorization/github");
    }

    @GetMapping("me")
    public ResponseEntity<UserRecord> me() {

        AppUserPrinciple principle = currentUser.require();
        User user = principle.getUser();
        return ResponseEntity.ok(new UserRecord(
                user.getId(),
                user.getGithubId(),
                user.getUname(),
                user.getDisplayName(),
                user.getAvatarUrl()
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }

        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
}
