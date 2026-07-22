package com.naprock.hexudon.domain.model.submission;

import com.naprock.hexudon.domain.model.movement.ActionPlan;

import java.util.ArrayList;
import java.util.List;

import static com.naprock.hexudon.domain.validation.DomainValidator.*;

public class ActionSubmission {

    private final int day;

    private final String teamId;

    private List<ActionPlan> plans;

    private long submittedAt;

    private int submitCount;

    public ActionSubmission(
            int day,
            String teamId,
            List<ActionPlan> plans,
            long submittedAt
    ) {
        requireNonNegative(day, "day");
        requireNotBlank(teamId, "teamId");
        requireNonNull(plans, "plans");

        this.day = day;
        this.teamId = teamId;
        this.plans = List.copyOf(plans);
        this.submittedAt = submittedAt;
        this.submitCount = 1;
    }

    public static ActionSubmission stay(
            int day,
            String teamId
    ) {
        return new ActionSubmission(
                day,
                teamId,
                new ArrayList<>(),
                0
        );
    }

    public int getDay() {
        return day;
    }

    public String getTeamId() {
        return teamId;
    }

    public List<ActionPlan> getPlans() {
        return plans;
    }

    public long getSubmittedAt() {
        return submittedAt;
    }

    public int getSubmitCount() {
        return submitCount;
    }

    /**
     * Team submits again for the same day.
     */
    public void resubmit(List<ActionPlan> plans, long submittedAt) {
        requireNonNull(plans, "plans");

        this.plans = List.copyOf(plans);
        this.submittedAt = submittedAt;
        this.submitCount++;
    }

    public ActionPlan getPlan(int index) {
        requireNonNegative(index, "index");

        if (index >= plans.size()) {
            throw new IndexOutOfBoundsException(
                    "Action plan index out of bounds: " + index
            );
        }

        return plans.get(index);
    }

    public boolean isSubmitted() {
        return submitCount > 0;
    }
}