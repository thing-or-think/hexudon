package com.naprock.hexudon.application.port.in;

import com.naprock.hexudon.application.dto.match.SubmitActionRequest;

public interface SubmitActionsUseCase {

    void submitActions(String teamId, SubmitActionRequest submitActionRequest);
}