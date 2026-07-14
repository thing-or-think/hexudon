package com.naprock.hexudon.application.port.in;

import com.naprock.hexudon.application.dto.match.MatchStateResponse;

public interface GetMatchStateUseCase {

    MatchStateResponse getMatchState(String teamName);
}