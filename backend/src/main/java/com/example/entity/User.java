
package com.example.entity;

import jakarta.persistence.*;

import java.time.Instant;
import lombok.*;

import java.util.UUID;

import java.io.Serializable;

@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class User implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name="github_id",unique = true,nullable = false)
    private Long githubId;

    @Column(name="github_name",nullable = false)
    private String uname;



    @Column(name="display_name",nullable = false)
    private String displayName;

    @Column(name="avatar_url",length = 500,nullable=false)
    private String avatarUrl;

    @Column(name="access_token",columnDefinition="TEXT",nullable = false)
    private String accessToken;

    @Column(name="token_scopes",columnDefinition = "TEXT", nullable = false)
    private String tokenScopes;

    @Column(name="created_at",unique = true,updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate(){
        if(createdAt == null){
            createdAt = Instant.now();
        }
    }

}