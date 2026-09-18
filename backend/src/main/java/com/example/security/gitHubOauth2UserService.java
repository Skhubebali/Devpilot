package com.example.security;

import com.example.entity.User;
import com.example.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class gitHubOauth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserService userService;

    private final DefaultOAuth2UserService deligate = new DefaultOAuth2UserService();

    @Override
    public  OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
       OAuth2User gitUser = deligate.loadUser(userRequest);
       String token = userRequest.getAccessToken().getTokenValue();
       String scopes = userRequest.getAccessToken().getScopes() != null ? String.join(",",userRequest.getAccessToken().getScopes()):"read:user,repo";
       User user = userService.upsertFromGitHub(gitUser.getAttributes(),token,scopes);
       return new  AppUserPrinciple(user,gitUser.getAttributes());
    }



}
