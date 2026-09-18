package com.example.service;

import com.example.entity.User;
import com.example.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {


    public final UserRepo userRepo;

    public final TextEncryptor textEnryprt;

    @Transactional(readOnly = true)
    public User getById(UUID id){
        return userRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("ID NOT FOUND"));
    }

    public String decryptAccessToken(User user){
        return textEnryprt.decrypt(user.getAccessToken());
    }


    @Transactional
    public User upsertFromGitHub(Map<String, Object> attributes, String token, String scopes) {
        Long githubId = ((Number) attributes.get("id")).longValue();
        String login = String.valueOf(attributes.get("login"));
        String name = attributes.get("name") != null ? String.valueOf(attributes.get("name")): login;
        String avatarUrl = attributes.get("avatar_url") != null
                ? String.valueOf(attributes.get("avatar_url"))
                : "";

        String encyptedToken = textEnryprt.encrypt(token);

        User user = userRepo.findByGithubId(githubId) ;
        if(user==null){
            user = new User();
        }
        user.setGithubId(githubId);
        user.setUname(login);
        user.setDisplayName(name);
        user.setAvatarUrl(avatarUrl);
        user.setAccessToken(encyptedToken);
        user.setTokenScopes(scopes);

        return userRepo.save(user);

    }

}
