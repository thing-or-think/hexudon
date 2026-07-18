module com.naprock.hexudon.sdk {


    requires com.fasterxml.jackson.databind;

    requires okhttp3;


    exports com.naprock.hexudon.sdk.api;

    exports com.naprock.hexudon.sdk.config;

    exports com.naprock.hexudon.sdk.model;

    exports com.naprock.hexudon.sdk.exception;


}
