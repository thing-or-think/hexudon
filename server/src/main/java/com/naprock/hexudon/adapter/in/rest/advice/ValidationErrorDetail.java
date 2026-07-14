package com.naprock.hexudon.adapter.in.rest.advice;

public record ValidationErrorDetail(

        String field,
        String rejectedValue,
        String message

) {
}