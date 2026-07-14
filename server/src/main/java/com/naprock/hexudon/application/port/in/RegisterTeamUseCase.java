package com.naprock.hexudon.application.port.in;

import com.naprock.hexudon.application.dto.team.TeamRegisterRequest;
import com.naprock.hexudon.application.dto.team.TeamResponse;
import com.naprock.hexudon.application.dto.team.TeamScoreResponse;

public interface RegisterTeamUseCase {

    TeamResponse registerTeam(TeamRegisterRequest request);
}