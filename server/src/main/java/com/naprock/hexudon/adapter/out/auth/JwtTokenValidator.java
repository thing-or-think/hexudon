package com.naprock.hexudon.adapter.out.auth;

import com.naprock.hexudon.application.dto.auth.TokenPayload;
import com.naprock.hexudon.application.port.out.auth.TokenValidator;
import com.naprock.hexudon.domain.exception.business.UnauthorizedException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.InvalidClaimException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
public class JwtTokenValidator implements TokenValidator {

    private final SecretKey secretKey;

    public JwtTokenValidator(
            @Value("${jwt.secret}") String secret
    ) {
        this.secretKey = Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );
    }

    @Override
    public TokenPayload validate(String token) {
        Claims claims;

        try {
            claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

        } catch (ExpiredJwtException e) {
            throw new UnauthorizedException(ErrorCode.UNAUTH_005);

        } catch (SecurityException e) {
            throw new UnauthorizedException(ErrorCode.UNAUTH_006);

        } catch (InvalidClaimException e) {
            String claimName = e.getClaimName();

            if ("iss".equalsIgnoreCase(claimName)) {
                throw new UnauthorizedException(ErrorCode.UNAUTH_007);
            }

            if ("aud".equalsIgnoreCase(claimName)) {
                throw new UnauthorizedException(ErrorCode.UNAUTH_008);
            }

            throw new UnauthorizedException(ErrorCode.UNAUTH_004);

        } catch (MalformedJwtException
                 | UnsupportedJwtException
                 | IllegalArgumentException e) {
            throw new UnauthorizedException(ErrorCode.UNAUTH_004);

        } catch (JwtException e) {
            throw new UnauthorizedException(ErrorCode.UNAUTH_004);
        }

        Object teamIdClaim = claims.get("teamId");

        if (teamIdClaim == null) {
            throw new UnauthorizedException(ErrorCode.UNAUTH_009);
        }

        String teamId = String.valueOf(teamIdClaim);

        if (teamId.isBlank()) {
            throw new UnauthorizedException(ErrorCode.UNAUTH_009);
        }

        return new TokenPayload(teamId);
    }
}