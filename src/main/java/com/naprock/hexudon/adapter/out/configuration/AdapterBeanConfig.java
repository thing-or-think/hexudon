package com.naprock.hexudon.adapter.out.configuration;

import com.naprock.hexudon.application.service.MatchApplicationService;
import com.naprock.hexudon.application.port.out.MatchConfigLoaderPort;
import com.naprock.hexudon.application.port.out.MatchStateStorePort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration for adapter layer beans.
 *
 * <p>Responsible for creating application service beans
 * and wiring required outbound ports.</p>
 */
@Configuration
public class AdapterBeanConfig {

    /**
     * Creates MatchApplicationService bean.
     *
     * @param storePort  match state storage port
     * @param loaderPort match configuration loader port
     * @return configured MatchApplicationService instance
     */
    @Bean
    public MatchApplicationService matchApplicationService(
            MatchStateStorePort storePort,
            MatchConfigLoaderPort loaderPort
    ) {
        return new MatchApplicationService(storePort, loaderPort);
    }
}