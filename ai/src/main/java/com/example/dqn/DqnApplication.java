package com.example.dqn;

import com.example.dqn.config.ApplicationConfig;
import com.example.dqn.adapter.in.cli.DqnCli;

/**
 * Main application entry point for the modular DQN project.
 */
public class DqnApplication {

    public static void main(String[] args) {
        System.out.println("===============================================================");
        System.out.println("     Java 21 DQN Framework HexWorld Demo via Clean Architecture");
        System.out.println("===============================================================");
        
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "info");
        
        try {
            ApplicationConfig appConfig = new ApplicationConfig();
            DqnCli cli = appConfig.bootstrap();
            cli.start(args);
        } catch (Exception e) {
            System.err.println("Error bootstrapping and running DQN application:");
            e.printStackTrace();
        }
    }
}
