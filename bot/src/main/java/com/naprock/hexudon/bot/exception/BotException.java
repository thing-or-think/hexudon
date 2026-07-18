package com.naprock.hexudon.bot.exception;

/**
 * Base unchecked exception for the Hexudon Bot.
 *
 * <p>Wraps any bot-specific error that occurs outside of SDK-level exceptions,
 * such as configuration issues, AI computation failures, or unexpected
 * game-loop states.
 */
public class BotException extends RuntimeException {

    /**
     * Creates a new bot exception with the specified detail message.
     *
     * @param message the detail message
     */
    public BotException(String message) {
        super(message);
    }

    /**
     * Creates a new bot exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the root cause
     */
    public BotException(String message, Throwable cause) {
        super(message, cause);
    }
}
