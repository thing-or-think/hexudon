package com.naprock.hexudon.application.port.in;

import com.naprock.hexudon.application.dto.admin.InitGameRequest;

public interface InitializeGameUseCase {

    void initialize(InitGameRequest request);

}