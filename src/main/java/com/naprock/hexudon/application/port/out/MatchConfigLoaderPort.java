package com.naprock.hexudon.application.port.out;


import com.naprock.hexudon.domain.model.match.MatchConfig;

public interface MatchConfigLoaderPort {

    MatchConfig loadConfig();
}