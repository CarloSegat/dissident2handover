package com.dissident.dlg.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import com.dissident.common.config.AriesClientConfig;
import com.dissident.common.config.AssociationServiceConfig;
import com.dissident.common.config.ConnectionManagerServiceConfig;
import com.dissident.common.config.DidExchangeServiceConfig;
import com.dissident.common.config.DlgPropertiesConfig;
import com.dissident.common.config.JsonConfig;
import com.dissident.common.config.LogServiceConfig;
import com.dissident.common.config.MessagingServiceConfig;

@Configuration
@Import({
    AriesClientConfig.class,
    JsonConfig.class,
    ConnectionManagerServiceConfig.class,
    LogServiceConfig.class,
    MessagingServiceConfig.class,
    AssociationServiceConfig.class,
    DlgPropertiesConfig.class,
    DidExchangeServiceConfig.class,
})
public class Configs {}