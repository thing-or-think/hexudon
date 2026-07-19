package com.naprock.hexudon.application.dto.state;

import java.util.List;

/**
 * Replay frame for one simulation step.
 *
 * @param step simulation step
 * @param agents current agent states
 * @param collected collected cell indices
 * @param servings total servings after this step
 * @param types distinct udon types after this step
 */
public record ReplayFrameResponse(

        int step,

        List<ReplayAgentResponse> agents,

        List<Integer> collected,

        int servings,

        int types

) {
}