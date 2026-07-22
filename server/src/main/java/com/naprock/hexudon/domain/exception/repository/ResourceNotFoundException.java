package com.naprock.hexudon.domain.exception.repository;

import com.naprock.hexudon.domain.exception.base.BusinessException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;

public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(
            String resource,
            String id
    ) {
        super(
                ErrorCode.RESOURCE_NOT_FOUND,
                404,
                resource + " not found: " + id
        );
    }
}