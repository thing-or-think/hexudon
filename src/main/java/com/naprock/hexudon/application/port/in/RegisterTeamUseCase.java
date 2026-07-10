package com.naprock.hexudon.application.port.in;

import com.naprock.hexudon.domain.model.Team;

/**
 * Inbound port for registering a new team.
 */
public interface RegisterTeamUseCase {

    /**
     * Registers a new team.
     *
     * @param teamName the name of the team
     * @return the registered {@link Team}
     */
    Team registerTeam(String teamName);
}