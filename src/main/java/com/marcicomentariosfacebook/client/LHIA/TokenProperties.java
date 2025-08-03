package com.marcicomentariosfacebook.client.LHIA;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "lhia.token")
public class TokenProperties {
    private String url;
    private String username;
    private String password;
    private String client_id;
    private String grant_type;
}