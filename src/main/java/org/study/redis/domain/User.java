package org.study.redis.domain;

import java.io.Serializable;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    private String userId;
    private String password;
    private final Collection<? extends GrantedAuthority> authorities;
}
