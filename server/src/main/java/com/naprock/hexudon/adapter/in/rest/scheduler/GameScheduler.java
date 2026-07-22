package com.naprock.hexudon.adapter.in.rest.scheduler;

import com.naprock.hexudon.application.port.in.ProcessGameScheduleUseCase;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class GameScheduler {

    private final ProcessGameScheduleUseCase useCase;

    public GameScheduler(ProcessGameScheduleUseCase useCase) {
        this.useCase = useCase;
    }

    @Scheduled(fixedRateString = "${game.scheduler.rate-ms:1000}")
    public void process() {
        useCase.process();
    }
}