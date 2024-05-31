package org.study.redis.domain;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    private String userId;
    private String password;

}
