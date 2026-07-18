module com.thingorthink.hexudon.sdk {


    requires com.fasterxml.jackson.databind;

    requires okhttp3;


    exports com.thingorthink.hexudon.sdk.api;

    exports com.thingorthink.hexudon.sdk.config;

    exports com.thingorthink.hexudon.sdk.model;

    exports com.thingorthink.hexudon.sdk.exception;


}
