package com.naprock.hexudon.application.port.in;

import com.naprock.hexudon.application.dto.admin.AddTeamRequest;
import com.naprock.hexudon.application.dto.admin.AddTeamResponse;

public interface AddTeamUseCase {
    public AddTeamResponse addTeam(AddTeamRequest request);
}
