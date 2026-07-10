package com.naprock.hexudon.infrastructure.scheduler;

import com.naprock.hexudon.application.port.in.CheckAndSimulateTurnUseCase;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;

class SchedulerConfigTest {

    @Test
    void checkAndSimulateTurn_shouldInvokeUseCase() {
        CheckAndSimulateTurnUseCase useCase = mock(CheckAndSimulateTurnUseCase.class);
        SchedulerConfig config = new SchedulerConfig(useCase);

        config.checkAndSimulateTurn();

        verify(useCase, times(1)).checkAndSimulateTurn();
    }
}
