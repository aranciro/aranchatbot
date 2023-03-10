package com.github.aranciro.bot.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@ToString
public final class BotConfig implements Serializable {
    
    @NotBlank
    private String name;
    private String gender;
    @NotEmpty
    private List<@NotBlank String> owners;
    @NotBlank
    private String channelName;
    private List<@NotBlank String> ignored;
    @NotBlank
    private String language;
    private String initPrompt;
    @NotBlank
    private String msgPrompt;
    @NotNull
    private Boolean quietInit;
    
    public BotConfig(@NotBlank final String name,
                     final String gender,
                     @NotEmpty final List<@NotBlank String> owners,
                     @NotBlank final String channelName,
                     final List<@NotBlank String> ignored,
                     @NotBlank final String language,
                     final String initPrompt,
                     @NotBlank final String msgPrompt,
                     @NotNull final Boolean quietInit) {
        this.name = name;
        this.gender = gender;
        this.owners = owners;
        this.channelName = channelName;
        this.ignored = ignored;
        this.language = language;
        this.initPrompt = initPrompt;
        this.msgPrompt = msgPrompt;
        this.quietInit = quietInit;
    }
    
}
