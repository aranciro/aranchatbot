package com.github.aranciro.bot.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.io.Serializable;

@Setter
@Getter
@NoArgsConstructor
@ToString
public final class ApiServerConfig implements Serializable {
    
    @NotBlank
    private String host;
    @NotNull
    @Positive
    @Max(65535)
    private Integer port;
    
    public ApiServerConfig(@NotBlank final String host,
                           @NotNull @Positive @Max(65535) final Integer port) {
        this.host = host;
        this.port = port;
    }
    
}
