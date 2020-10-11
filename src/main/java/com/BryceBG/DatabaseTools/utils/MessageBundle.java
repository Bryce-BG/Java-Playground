package com.BryceBG.DatabaseTools.utils;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * This class is used to get text from our resources/localization/messages_<language> files so that the website can be used by multple languages.
 * At this time we only have one language but we could add future message_<language> to support diverse use of the app. Until that time this is 
 * just a placeholder.
 * @author Bryce-BG
 *
 */
public class MessageBundle {

    private ResourceBundle messages;

	//constructor
    public MessageBundle(String languageTag) {
        Locale locale = languageTag != null ? new Locale(languageTag) : Locale.ENGLISH;
        this.messages = ResourceBundle.getBundle("localization/messages", locale);
    }

    public String get(String message) {
        return messages.getString(message);
    }

    public final String get(final String key, final Object... args) {
        return MessageFormat.format(get(key), args);
    }

}