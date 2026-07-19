package com.naprock.hexudon.application.dto.state;

/**
 * Replay state of one agent.
 *
 * @param cell current cell index
 * @param fuel remaining fuel
 * @param type patrol or refuel
 */
public record ReplayAgentResponse(

        int cell,

        Integer fuel,

        String type

) {
}