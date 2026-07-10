package com.naprock.hexudon.application.port.in;

/**
 * Inbound port for updating team spam violations.
 */
public interface IncreaseSpamViolationUseCase {

    /**
     * Increases spam violation count for the specified team.
     *
     * @param teamName team name
     */
    void increaseSpamViolationCount(String teamName);

}