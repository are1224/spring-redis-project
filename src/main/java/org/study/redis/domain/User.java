package org.study.redis.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    private String userId;
    private String password;

}
