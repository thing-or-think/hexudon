package com.naprock.hexudon.infrastructure.scheduler;

import com.naprock.hexudon.application.port.in.CheckAndSimulateTurnUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.annotation.Scheduled;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.*;

class SchedulerConfigTest {

    @Test
    void checkAndSimulateTurn_shouldInvokeUseCase() {
        // Arrange
        CheckAndSimulateTurnUseCase useCase = mock(CheckAndSimulateTurnUseCase.class);
        SchedulerConfig config = new SchedulerConfig(useCase);

        // Act
        config.checkAndSimulateTurn();

        // Assert
        verify(useCase, times(1)).checkAndSimulateTurn();
    }

    @Test
    void checkAndSimulateTurn_shouldCatchExceptionAndNotPropagate() {
        // Arrange
        CheckAndSimulateTurnUseCase useCase = mock(CheckAndSimulateTurnUseCase.class);
        doThrow(new RuntimeException("Simulated exception")).when(useCase).checkAndSimulateTurn();
        SchedulerConfig config = new SchedulerConfig(useCase);

        // Act & Assert
        assertThatCode(config::checkAndSimulateTurn).doesNotThrowAnyException();
        verify(useCase, times(1)).checkAndSimulateTurn();
    }

    @Test
    void checkAndSimulateTurn_shouldHaveScheduledAnnotationWithFixedDelay() throws NoSuchMethodException {
        // Arrange
        Method method = SchedulerConfig.class.getMethod("checkAndSimulateTurn");

        // Act
        Scheduled scheduled = method.getAnnotation(Scheduled.class);

        // Assert
        assertThat(scheduled).isNotNull();
        assertThat(scheduled.fixedDelay()).isEqualTo(1000L);
    }
}
