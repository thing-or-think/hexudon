module com.naprock.hexudon.sdk {

    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.annotation;

    requires okhttp3;
    requires okio;
    requires kotlin.stdlib;

    exports com.naprock.hexudon.sdk.api;
    exports com.naprock.hexudon.sdk.config;
    exports com.naprock.hexudon.sdk.model;
    exports com.naprock.hexudon.sdk.exception;
}