package com.naprock.hexudon.domain.model.submission;

import java.util.*;

import static com.naprock.hexudon.domain.validation.DomainValidator.*;

public class SubmissionHistory {

    private final Map<Integer, Map<String, ActionSubmission>> submissions = new HashMap<>();

    /**
     * Submit actions for a team on a specific day.
     * If the team has already submitted, replace the plan and increase submit count.
     */
    public void submit(ActionSubmission submission) {
        requireNonNull(submission, "submission");

        submissions
                .computeIfAbsent(submission.getDay(), ignored -> new HashMap<>())
                .merge(
                        submission.getTeamId(),
                        submission,
                        (existing, latest) -> {
                            existing.resubmit(
                                    latest.getPlans(),
                                    latest.getSubmittedAt()
                            );
                            return existing;
                        }
                );
    }

    /**
     * Find submission of a team on a specific day.
     * If no submission exists, return a STAY submission.
     */
    public ActionSubmission find(
            int day,
            String teamId
    ) {
        requireNonNegative(day, "day");
        requireNotBlank(teamId, "teamId");

        return Optional.ofNullable(submissions.get(day))
                .map(submissionsByTeam -> submissionsByTeam.get(teamId))
                .orElseGet(() -> ActionSubmission.stay(day, teamId));
    }

    public boolean hasSubmitted(int day, String teamId) {
        return find(day, teamId).isSubmitted();
    }

    public List<ActionSubmission> getSubmissions(int day) {
        return submissions.containsKey(day)
                ? List.copyOf(submissions.get(day).values())
                : List.of();
    }

    public void clear() {
        submissions.clear();
    }
}