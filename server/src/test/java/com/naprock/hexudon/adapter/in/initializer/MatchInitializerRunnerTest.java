package com.naprock.hexudon.adapter.in.initializer;

import com.naprock.hexudon.application.port.in.InitializeMatchUseCase;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;

class MatchInitializerRunnerTest {

    @Test
    void shouldCallInitializeMatchOnRun() throws Exception {
        InitializeMatchUseCase initializeMatchUseCase = mock(InitializeMatchUseCase.class);
        MatchInitializerRunner runner = new MatchInitializerRunner(initializeMatchUseCase);

        runner.run();

        verify(initializeMatchUseCase, times(1)).initializeMatch();
    }
}
