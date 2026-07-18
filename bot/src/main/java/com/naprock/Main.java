package com.naprock;

import com.naprock.hexudon.bot.BotApplication;

/**
 * Legacy entry point retained for IDE compatibility.
 *
 * <p>Delegates directly to {@link BotApplication#main(String[])}.
 * Prefer running {@code BotApplication} directly or using
 * {@code mvn exec:java -pl bot}.
 */
public class Main {

    private Main() {}

    public static void main(String[] args) {
        BotApplication.main(args);
    }
}