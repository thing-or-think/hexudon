package com.naprock.hexudon.application.model.team;

public record TeamRegistrationData(
        String teamName,
        int amountPatrol,
        int amountRefuel
) {}