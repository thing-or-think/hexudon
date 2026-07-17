package com.naprock.hexudon.sdk.internal.dto.request;


/**
 * DTO request used to copy a practice game progress.
 *
 * <p>
 * This DTO represents the JSON payload sent to the practice
 * copy endpoint.
 * </p>
 *
 * <pre>
 * {
 *   "gameId": "practice-new",
 *   "fromGameId": "practice-old",
 *   "fromTeamId": "team-alpha",
 *   "uptoDay": 5
 * }
 * </pre>
 *
 * <p>
 * This class is immutable and only used internally for transport
 * serialization.
 * </p>
 *
 * @param gameId target practice game identifier
 * @param fromGameId source practice game identifier
 * @param fromTeamId source team identifier
 * @param uptoDay maximum day to copy
 */
public record PracticeCopyRequest(

        String gameId,

        String fromGameId,

        String fromTeamId,

        int uptoDay

) {


    /**
     * Compact constructor.
     *
     * @throws IllegalArgumentException
     *         when any identifier is blank or uptoDay is negative
     */
    public PracticeCopyRequest {


        validateNotBlank(
                gameId,
                "gameId"
        );


        validateNotBlank(
                fromGameId,
                "fromGameId"
        );


        validateNotBlank(
                fromTeamId,
                "fromTeamId"
        );


        if (uptoDay < 0) {

            throw new IllegalArgumentException(
                    "uptoDay must not be negative"
            );
        }
    }



    /**
     * Validates required string fields.
     *
     * @param value field value
     * @param fieldName field name
     */
    private static void validateNotBlank(
            String value,
            String fieldName
    ) {

        if (value == null || value.isBlank()) {

            throw new IllegalArgumentException(
                    fieldName + " must not be blank"
            );
        }
    }
}