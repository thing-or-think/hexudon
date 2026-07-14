package com.naprock.hexudon.application.port.in;


import com.naprock.hexudon.application.dto.match.MatchConfigResponse;

public interface GetMatchConfigUseCase {

    MatchConfigResponse getMatchConfig();
}