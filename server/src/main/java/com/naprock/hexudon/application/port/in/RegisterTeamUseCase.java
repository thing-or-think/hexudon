package com.naprock.hexudon.application.port.in;

import com.naprock.hexudon.application.dto.team.TeamRegisterRequest;
import com.naprock.hexudon.application.dto.team.TeamResponse;

public interface RegisterTeamUseCase {

    void registerTeam(String teamId, TeamRegisterRequest request);
}