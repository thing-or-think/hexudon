package com.naprock.hexudon.domain.valueobject;

public class Action {

    private int order;
    private ActionType actionType;
    private Integer targetX;
    private Integer targetY;
    private long timestamp;

    public Action() {
    }

    public Action(
            int order,
            ActionType actionType,
            Integer targetX,
            Integer targetY,
            long timestamp
    ) {
        this.order = order;
        this.actionType = actionType;
        this.targetX = targetX;
        this.targetY = targetY;
        this.timestamp = timestamp;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }

    public Integer getTargetX() {
        return targetX;
    }

    public void setTargetX(Integer targetX) {
        this.targetX = targetX;
    }

    public Integer getTargetY() {
        return targetY;
    }

    public void setTargetY(Integer targetY) {
        this.targetY = targetY;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public int getOrder() {
        return order;
    }
}
