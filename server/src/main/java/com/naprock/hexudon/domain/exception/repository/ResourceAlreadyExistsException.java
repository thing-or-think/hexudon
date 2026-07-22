package com.naprock.hexudon.domain.exception.repository;

import com.naprock.hexudon.domain.exception.base.BusinessException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;

public class ResourceAlreadyExistsException extends BusinessException {

    public ResourceAlreadyExistsException(
            String resource,
            String id
    ) {
        super(
                ErrorCode.RESOURCE_ALREADY_EXISTS,
                409,
                resource + " already exists: " + id
        );
    }
}