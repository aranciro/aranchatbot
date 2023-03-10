package com.github.aranciro.bot;

import com.github.aranciro.bot.config.BotConfig;
import com.github.aranciro.bot.config.Config;
import com.github.aranciro.bot.event.ChannelMessageEventConsumer;
import com.github.aranciro.bot.util.BotUtils;
import com.github.aranciro.client.ChatGPTClient;
import com.github.aranciro.client.pojo.model.PromptResponse;
import com.github.philippheuer.events4j.core.EventManager;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import javax.validation.ConstraintViolation;
import java.io.File;
import java.io.FileInputStream;
import java.util.Set;

@Data
@Slf4j
public final class Bot {
    
    @NonNull
    private final Config config;
    @NonNull
    private final ChatGPTClient chatGPTClient;
    @NonNull
    private final TwitchClient twitchClient;
    
    public Bot(final String configurationFilePath) {
        //TODO ensure default config.yaml path is current jar folder
        final String configPath = configurationFilePath == null ? "config.yaml" : configurationFilePath;
        log.info("Config file path: {}", configPath);
        File configFile = new File(configPath);
        if (!configFile.exists()) {
            String msg = String.format("No config.yaml file in the given path: %s", configPath);
            throw new IllegalArgumentException(msg);
        }
        // TODO replace with snakeyaml
        Constructor constructor = new Constructor(new LoaderOptions());
        Yaml yaml = new Yaml(constructor);
        try {
            this.config = yaml.loadAs(new FileInputStream(configFile), Config.class);
        } catch (Exception e) {
            String msg = String.format("Unable to parse config from file: %s", configPath);
            throw new IllegalArgumentException(msg, e);
        }
        this.validateConfig(this.config);
        log.debug("Loaded configuration: \n{}", this.config);
        this.chatGPTClient = BotUtils.buildChatGPTClient(this.config);
        this.twitchClient = BotUtils.buildTwitchClient(this.config);
    }
    
    public static void main(String[] args) {
        log.info("Bot - START");
        String configPath = args[0];
        Bot bot = new Bot(configPath);
        bot.run();
        log.info("Bot - STOP");
    }
    
    // TODO threading
    public void run() {
        BotConfig botConfig = this.getConfig().getBot();
        String channelName = botConfig.getChannelName();
        this.twitchClient.getChat().joinChannel(channelName);
        EventManager eventManager = this.twitchClient.getEventManager();
        eventManager.onEvent(
                ChannelMessageEvent.class,
                new ChannelMessageEventConsumer(this.chatGPTClient, this.twitchClient, this.config));
        if (StringUtils.isNotBlank(botConfig.getInitPrompt())) {
            // TODO add retry with failsafe
            try {
                // Sending init prompt
                PromptResponse response = BotUtils.sendPrompt(BotUtils.getInitPrompt(botConfig), this.chatGPTClient);
                if (Boolean.FALSE.equals(botConfig.getQuietInit())) {
                    this.twitchClient.getChat().sendMessage(channelName, response.getText());
                }
            } catch (Exception e) {
                throw new IllegalStateException("Unable to send init prompt", e);
            }
        }
        this.keepAlive(); // TODO remove?
    }
    
    private void validateConfig(@NonNull final Config config) {
        Set<ConstraintViolation<Config>> violations = BotUtils.validateObject(config);
        if (!violations.isEmpty()) {
            for (ConstraintViolation<Config> v : violations) {
                log.warn("Configuration constraint violation: {}", v);
            }
            throw new IllegalArgumentException("Invalid configuration");
        }
    }
    
    private void keepAlive() {
        boolean alive = true;
        while (alive) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                alive = false;
                log.error("Error while sleeping", e);
                Thread.currentThread().interrupt();
            }
        }
    }
    
}
