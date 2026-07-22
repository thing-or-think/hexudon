package com.naprock.hexudon.application.port.in;

import com.naprock.hexudon.application.dto.team.SubmitActionsRequest;

public interface SubmitActionsUseCase {
    void submitActions(SubmitActionsRequest request, String teamId);
}