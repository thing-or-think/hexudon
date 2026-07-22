package com.naprock.hexudon.application.port.out.auth;

import com.naprock.hexudon.application.dto.auth.TokenPayload;

public interface TokenValidator {

    TokenPayload validate(String token);

}