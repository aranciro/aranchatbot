package com.github.aranciro.bot.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Setter
@Getter
@NoArgsConstructor
@ToString
public final class TwitchAuthConfig implements Serializable {
    
    @NotBlank
    private String accessToken;
    @NotBlank
    private String clientId;
    @NotBlank
    private String clientSecret;
    
    public TwitchAuthConfig(@NotBlank final String accessToken,
                            @NotBlank final String clientId,
                            @NotBlank final String clientSecret) {
        this.accessToken = accessToken;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }
    
}
