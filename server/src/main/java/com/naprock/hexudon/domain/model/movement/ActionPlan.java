package com.naprock.hexudon.domain.model.movement;

import java.util.ArrayList;
import java.util.List;

import static com.naprock.hexudon.domain.validation.DomainValidator.requireNonNull;

public record ActionPlan(List<Action> actions) {

    public ActionPlan {
        requireNonNull(actions, "actions");
        actions = List.copyOf(actions);
    }

    public boolean isEmpty() {
        return actions.isEmpty();
    }

    public int size() {
        return actions.size();
    }

    public Action get(int index) {
        return actions.get(index);
    }

    public List<Action> copy() {
        return new ArrayList<>(actions);
    }
}