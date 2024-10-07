package org.study.redis.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class UserEventVO {

    private String timestamp;
    private String userAgent;
    private String colorName;
    private String userName;

}
