package com.naprock.hexudon.domain.exception.system;

import com.naprock.hexudon.domain.exception.base.SystemException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;

/**
 * Exception thrown when system configuration cannot be loaded.
 *
 * <p>Examples:
 * <ul>
 *     <li>Configuration file does not exist</li>
 *     <li>Configuration file has invalid syntax</li>
 *     <li>Configuration cannot be parsed</li>
 * </ul>
 *
 * <p>This exception is typically used by:
 * <ul>
 *     <li>MatchConfigLoader</li>
 *     <li>FileUtils</li>
 * </ul>
 */
public class ConfigLoadException extends SystemException {

    /**
     * Creates a configuration load exception.
     *
     * <p>Error code is fixed:
     * {@link ErrorCode#CONFIG_ERROR}
     *
     * @param message error message
     */
    public ConfigLoadException(String message) {
        super(
                ErrorCode.CONFIG_ERROR,
                message
        );
    }

    /**
     * Creates a configuration load exception with root cause.
     *
     * <p>Error code is fixed:
     * {@link ErrorCode#CONFIG_ERROR}
     *
     * @param message error message
     * @param cause root cause
     */
    public ConfigLoadException(
            String message,
            Throwable cause
    ) {
        super(
                ErrorCode.CONFIG_ERROR,
                message,
                cause
        );
    }
}