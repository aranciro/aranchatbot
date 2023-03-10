package com.github.aranciro.bot.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Setter
@Getter
@NoArgsConstructor
@ToString
public final class Config implements Serializable {
    
    @Valid
    @NotNull
    private TwitchAuthConfig twitchAuth;
    @Valid
    @NotNull
    private BotConfig bot;
    @Valid
    @NotNull
    private ApiServerConfig apiServer;
    
    public Config(@Valid @NotNull final TwitchAuthConfig twitchAuth,
                  @Valid @NotNull final BotConfig bot,
                  @Valid @NotNull final ApiServerConfig apiServer) {
        this.twitchAuth = twitchAuth;
        this.bot = bot;
        this.apiServer = apiServer;
    }
    
}
