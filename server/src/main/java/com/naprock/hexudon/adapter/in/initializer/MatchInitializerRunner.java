package com.naprock.hexudon.adapter.in.initializer;

import com.naprock.hexudon.application.port.in.InitializeMatchUseCase;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class MatchInitializerRunner implements CommandLineRunner {

    private final InitializeMatchUseCase initializeMatchUseCase;

    public MatchInitializerRunner(InitializeMatchUseCase initializeMatchUseCase) {
        this.initializeMatchUseCase = initializeMatchUseCase;
    }

    @Override
    public void run(String... args) throws Exception {
        initializeMatchUseCase.initializeMatch();
        System.out.println("[HEXUDON] Game Map has been generated and initialized successfully at startup.");
    }
}