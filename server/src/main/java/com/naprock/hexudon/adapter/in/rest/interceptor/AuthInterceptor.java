package com.naprock.hexudon.adapter.in.rest.interceptor;

import com.naprock.hexudon.adapter.in.rest.auth.RequestContext;
import com.naprock.hexudon.application.dto.auth.TokenPayload;
import com.naprock.hexudon.application.port.out.auth.TokenValidator;
import com.naprock.hexudon.domain.exception.business.UnauthorizedException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(AuthInterceptor.class);

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final TokenValidator tokenValidator;

    public AuthInterceptor(TokenValidator tokenValidator) {
        this.tokenValidator = tokenValidator;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) {
        boolean requiresRequestContext = isRequestContextRequired(handler);

        String authorization = request.getHeader(AUTHORIZATION_HEADER);

        if (authorization == null || authorization.isBlank()) {
            if (!requiresRequestContext) {
                return true;
            }
            log.debug("Missing Authorization header for URI: {}", request.getRequestURI());
            throw new UnauthorizedException(ErrorCode.UNAUTH_001);
        }

        if (!authorization.startsWith(BEARER_PREFIX)) {
            if (!requiresRequestContext) {
                return true;
            }
            log.debug("Invalid Authorization scheme for URI: {}", request.getRequestURI());
            throw new UnauthorizedException(ErrorCode.UNAUTH_002);
        }

        String rawToken = authorization.substring(BEARER_PREFIX.length()).trim();

        if (rawToken.isEmpty()) {
            if (!requiresRequestContext) {
                return true;
            }
            log.debug("Empty token string for URI: {}", request.getRequestURI());
            throw new UnauthorizedException(ErrorCode.UNAUTH_003);
        }

        TokenPayload payload = tokenValidator.validate(rawToken);

        RequestContext context = new RequestContext(
                payload.teamId()
        );

        request.setAttribute(
                RequestAttributes.REQUEST_CONTEXT,
                context
        );

        log.debug("Successfully created and bound RequestContext [teamId={}] for URI: {}", payload.teamId(), request.getRequestURI());

        return true;
    }

    private boolean isRequestContextRequired(Object handler) {
        if (handler instanceof HandlerMethod handlerMethod) {
            for (MethodParameter param : handlerMethod.getMethodParameters()) {
                if (param.hasParameterAnnotation(RequestAttribute.class)) {
                    RequestAttribute attr = param.getParameterAnnotation(RequestAttribute.class);
                    if (attr != null && RequestAttributes.REQUEST_CONTEXT.equals(attr.value())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}