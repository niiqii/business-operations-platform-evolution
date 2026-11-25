package com.bop.workorder.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "bop.external")
public class ExternalServiceProperties {

    private String landerBaseUrl = "http://127.0.0.1:18080";
    private String landerUsername = "lander_mock";
    private String landerPassword = "lander_mock_pass";
    private String notifyBaseUrl = "http://127.0.0.1:18080";
    private int connectTimeoutMs = 3000;
    private int readTimeoutMs = 8000;

    public String getLanderBaseUrl() {
        return landerBaseUrl;
    }

    public void setLanderBaseUrl(String landerBaseUrl) {
        this.landerBaseUrl = landerBaseUrl;
    }

    public String getLanderUsername() {
        return landerUsername;
    }

    public void setLanderUsername(String landerUsername) {
        this.landerUsername = landerUsername;
    }

    public String getLanderPassword() {
        return landerPassword;
    }

    public void setLanderPassword(String landerPassword) {
        this.landerPassword = landerPassword;
    }

    public String getNotifyBaseUrl() {
        return notifyBaseUrl;
    }

    public void setNotifyBaseUrl(String notifyBaseUrl) {
        this.notifyBaseUrl = notifyBaseUrl;
    }

    public int getConnectTimeoutMs() {
        return connectTimeoutMs;
    }

    public void setConnectTimeoutMs(int connectTimeoutMs) {
        this.connectTimeoutMs = connectTimeoutMs;
    }

    public int getReadTimeoutMs() {
        return readTimeoutMs;
    }

    public void setReadTimeoutMs(int readTimeoutMs) {
        this.readTimeoutMs = readTimeoutMs;
    }
}
