package com.naprock.hexudon.domain.model.match;

import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;

import static com.naprock.hexudon.domain.validation.DomainValidator.requireTrue;

public class MatchState {

    private MatchStatus status;

    private int currentDay;

    private final long startTime;

    private long registrationEndTime;

    private long dayEndTime;

    public MatchState(long startTime) {
        status = MatchStatus.NOT_STARTED;
        currentDay = 0;
        this.startTime = startTime;
    }

    public MatchStatus getStatus() {
        return status;
    }

    public int getCurrentDay() {
        return currentDay;
    }

    public long getDayEndTime() {
        return dayEndTime;
    }

    public void openRegistration(long registrationEndTime) {
        requireTrue(
                status == MatchStatus.NOT_STARTED,
                "Registration can only be opened when match is NOT_STARTED."
        );

        this.status = MatchStatus.REGISTERING;
        this.registrationEndTime = registrationEndTime;
    }

    public void start(long now, long firstDayEndTime) {
        requireTrue(
                status == MatchStatus.REGISTERING,
                "Match is not registering."
        );

        requireTrue(
                now >= registrationEndTime,
                "Registration period has not ended yet."
        );

        status = MatchStatus.PLAYING;
        currentDay = 0;
        dayEndTime = firstDayEndTime;
    }

    public void nextDay(long nextDayEndTime) {
        requireTrue(
                status == MatchStatus.PLAYING,
                "Cannot advance day because match is not PLAYING. Current status: " + status
        );

        requireTrue(
                nextDayEndTime > dayEndTime,
                "Next day end time must be greater than current day end time."
        );

        currentDay++;
        dayEndTime = nextDayEndTime;
    }

    public void finish() {
        requireTrue(
                status == MatchStatus.PLAYING,
                "Only a PLAYING match can be finished. Current status: " + status
        );

        status = MatchStatus.FINISHED;
    }

    public void requireRegistering() {
        if (status != MatchStatus.REGISTERING) {
            throw new GameRuleViolationException(
                    ErrorCode.MATCH_NOT_REGISTERING,
                    "Match is not in registering state. Current status: " + status
            );
        }
    }

    public void requirePlaying() {
        if (status != MatchStatus.PLAYING) {
            throw new GameRuleViolationException(
                    ErrorCode.MATCH_NOT_PLAYING,
                    "Match is not in playing state. Current status: " + status
            );
        }
    }

    public void requireCurrentDay(int dayIndex) {
        requireTrue(
                dayIndex == currentDay,
                "Invalid day index. Expected: " + currentDay + ", actual: " + dayIndex
        );
    }

    public boolean hasStarted(long now) {
        return now >= startTime;
    }

    public boolean isRegistrationFinished(long now) {
        return now >= registrationEndTime;
    }

    public boolean isDayFinished(long now) {
        return now >= dayEndTime;
    }

    public boolean isNotStarted() {
        return status == MatchStatus.NOT_STARTED;
    }

    public boolean isRegistering() {
        return status == MatchStatus.REGISTERING;
    }

    public boolean isPlaying() {
        return status == MatchStatus.PLAYING;
    }

    public boolean isFinished() {
        return status == MatchStatus.FINISHED;
    }

    public long getRemainingTime(long now) {
        return switch (status) {
            case NOT_STARTED -> Math.max(0, startTime - now);
            case REGISTERING -> Math.max(0, registrationEndTime - now);
            case PLAYING -> Math.max(0, dayEndTime - now);
            case FINISHED -> 0;
        };
    }
}