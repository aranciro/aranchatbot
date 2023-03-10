package com.github.aranciro.bot.event;

import com.github.aranciro.bot.config.BotConfig;
import com.github.aranciro.bot.config.Config;
import com.github.aranciro.bot.util.BotUtils;
import com.github.aranciro.client.ChatGPTClient;
import com.github.aranciro.client.pojo.model.Prompt;
import com.github.aranciro.client.pojo.model.PromptResponse;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
public final class ChannelMessageEventConsumer implements TwitchEventConsumer<ChannelMessageEvent> {
    
    @NonNull
    private final ChatGPTClient chatGPTClient;
    @NonNull
    private final TwitchClient twitchClient;
    @NonNull
    private final Config config;
    
    // TODO implement threading (this method is blocking) but use waiting logic for serving (e.g. thread pool size 10
    //  but only 3 gets to be served while the others wait). This ensures that a single event handling process will
    //  never block the queue and prevents spamming and order mess.
    // TODO implement logic for collecting messages in a buffer with max size (e.g. 30). When a user mentions the bot,
    //  feed the whole buffer to ChatGPT at once and empty it, then reply to that user. The buffer should be FIFO and so
    //  once filled, the oldest message leaves the place to the newest one. Change the reply msgPrompt in config if
    //  needed.
    @Override
    public void accept(@NonNull final ChannelMessageEvent event) {
        final String msg = event.getMessage();
        final String user = event.getUser().getName();
        @Valid @NotNull BotConfig botConfig = this.config.getBot();
        String mention = String.format("@%s", botConfig.getName());
        boolean isIgnoredUser = StringUtils.equalsIgnoreCase(
                user.trim(),
                botConfig.getName().trim())
                || (botConfig.getIgnored() != null && botConfig.getIgnored()
                .stream()
                .filter(Objects::nonNull)
                .anyMatch(u -> u.trim().equalsIgnoreCase(user.trim())));
        boolean isMention = StringUtils.containsIgnoreCase(msg, mention);
        log.debug(isMention ? "[Message][MENTION] {}: {}" : "[Message] {}: {}", user, msg);
        if (isMention && !isIgnoredUser) {
            @NotBlank String msgPromptText = botConfig.getMsgPrompt()
                    .replace("<user>", user)
                    .replace("<message>", msg);
            Prompt msgPrompt = new Prompt(msgPromptText);
            PromptResponse response = null;
            try {
                // TODO add retry with failsafe
                response = BotUtils.sendPrompt(msgPrompt, this.chatGPTClient);
            } catch (Exception e) {
                log.error("Error while sending prompt to ChatGPT (Prompt: {})", msgPrompt, e);
            }
            // TODO handle messages > 500 characters
            if (response != null) {
                // TODO add retry with failsafe ?
                this.twitchClient.getChat().sendMessage(botConfig.getChannelName(), response.getText());
            }
        } else if (isMention) {
            log.warn("Ignoring mention from user: {}", user);
        } else {
            //
        }
    }
    
}
