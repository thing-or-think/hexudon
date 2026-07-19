package com.naprock.hexudon.application.dto.state;

import java.util.List;

/**
 * Response DTO containing replay data for the whole match.
 *
 * <p>Used by:
 * <ul>
 *     <li>GET /api/game/replay</li>
 * </ul>
 *
 * @param days replay data grouped by day
 */
public record ReplayResponse(

        List<ReplayDayResponse> days

) {
}