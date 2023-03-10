package com.github.aranciro.bot.event;

import com.github.twitch4j.chat.events.TwitchEvent;

import java.util.function.Consumer;

public interface TwitchEventConsumer<T extends TwitchEvent> extends Consumer<T> {

}
