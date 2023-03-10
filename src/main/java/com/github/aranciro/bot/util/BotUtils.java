package com.github.aranciro.bot.util;

import com.github.aranciro.bot.config.BotConfig;
import com.github.aranciro.bot.config.Config;
import com.github.aranciro.bot.config.TwitchAuthConfig;
import com.github.aranciro.client.APIServerClient;
import com.github.aranciro.client.ChatGPTClient;
import com.github.aranciro.client.exception.PromptException;
import com.github.aranciro.client.pojo.model.APIServer;
import com.github.aranciro.client.pojo.model.Prompt;
import com.github.aranciro.client.pojo.model.PromptResponse;
import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public final class BotUtils {
    
    private BotUtils() {
        throw new IllegalStateException("Cannot instantiate utils class");
    }
    
    @NonNull
    public static PromptResponse sendPrompt(
            @NonNull final Prompt prompt,
            @NonNull final ChatGPTClient chatGPTClient) throws PromptException {
        log.info("Sending prompt to ChatGPT...");
        final PromptResponse resp = chatGPTClient.sendPrompt(prompt);
        log.info("ChatGPT response: {}", resp);
        return resp;
    }
    
    @NonNull
    public static Prompt getInitPrompt(@NonNull final Config config) {
        return getInitPrompt(config.getBot());
    }
    
    @NonNull
    public static Prompt getInitPrompt(@NonNull final BotConfig botConfig) {
        String initPromptText = botConfig.getInitPrompt().trim()
                .replace("$name", botConfig.getName().trim())
                .replace("$gender", StringUtils.defaultIfBlank(botConfig.getGender().trim(), ""))
                .replace("$channel", botConfig.getChannelName().trim())
                .replace("$owner", botConfig.getChannelName().trim())
                .replace("$altOwners",
                        StringUtils.joinWith(", ", botConfig.getOwners()
                                .stream()
                                .map(String::trim)
                                .collect(Collectors.toList())))
                .replace("$msgPrompt", botConfig.getMsgPrompt().trim())
                .replace("$language", botConfig.getLanguage().trim());
        return new Prompt(initPromptText);
    }
    
    @NonNull
    public static <T> Set<ConstraintViolation<T>> validateObject(final T object) {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        return validator.validate(object);
    }
    
    @NonNull
    public static APIServerClient buildChatGPTClient(@NonNull final Config config) {
        APIServer server = new APIServer(
                Objects.requireNonNull(config).getApiServer().getHost(),
                config.getApiServer().getPort());
        return APIServerClient.builder().server(server).build();
    }
    
    @NonNull
    public static TwitchClient buildTwitchClient(@NonNull final Config config) {
        @Valid @NotNull TwitchAuthConfig twitchAuth = config.getTwitchAuth();
        OAuth2Credential credential = new OAuth2Credential(
                "twitch",
                twitchAuth.getAccessToken()
        );
        return TwitchClientBuilder.builder()
                .withClientId(twitchAuth.getClientId())
                .withClientSecret(twitchAuth.getClientSecret())
                .withEnableHelix(true)
                .withChatAccount(credential)
                .withEnableChat(true)
                .withEnableTMI(true)
                .build();
    }
    
}
