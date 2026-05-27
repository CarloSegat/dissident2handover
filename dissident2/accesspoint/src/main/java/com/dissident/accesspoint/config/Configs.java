package com.dissident.accesspoint.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.dissident.common.config.AriesClientConfig;
import com.dissident.common.config.AssociationServiceConfig;
import com.dissident.common.config.ConnectionManagerServiceConfig;
import com.dissident.common.config.DidExchangeServiceConfig;
import com.dissident.common.config.DidResolutionConfig;
import com.dissident.common.config.DlgPropertiesConfig;
import com.dissident.common.config.JsonConfig;
import com.dissident.common.config.LogServiceConfig;

@Configuration
@Import({
        AriesClientConfig.class,
        ConnectionManagerServiceConfig.class,
        JsonConfig.class,
        AssociationServiceConfig.class,
        LogServiceConfig.class,
        DlgPropertiesConfig.class,
        DidResolutionConfig.class,
        DidExchangeServiceConfig.class,
})
public class Configs {}
