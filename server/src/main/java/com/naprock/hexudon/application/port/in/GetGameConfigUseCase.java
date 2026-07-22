package com.naprock.hexudon.application.port.in;

import com.naprock.hexudon.application.dto.config.GameConfigResponse;

public interface GetGameConfigUseCase {

    GameConfigResponse getConfig(String teamId, String gameId);

}