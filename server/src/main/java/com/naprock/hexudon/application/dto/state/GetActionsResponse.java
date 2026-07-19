package com.naprock.hexudon.application.dto.state;

import java.util.List;

/**
 * Response DTO containing all submitted action history.
 *
 * <p>Used by:
 * <ul>
 *     <li>GET /api/game/actions</li>
 * </ul>
 *
 * @param actions submitted action history
 */
public record GetActionsResponse(

        List<ActionHistoryResponse> actions

) {
}