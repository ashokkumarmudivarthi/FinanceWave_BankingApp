package com.financewave.transaction.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FeatureToggleConfig {

    @Value("${feature.fraud.check:true}")
    private boolean fraudCheck;

    @Value("${feature.notifications:false}")
    private boolean notifications;

    @Value("${feature.audit.enabled:true}")
    private boolean auditEnabled;

    public boolean isFraudCheckEnabled() {
        return fraudCheck;
    }

    public boolean isNotificationsEnabled() {
        return notifications;
    }

    public boolean isAuditEnabled() {
        return auditEnabled;
    }
}