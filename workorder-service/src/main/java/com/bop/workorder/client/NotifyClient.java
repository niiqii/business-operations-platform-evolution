package com.bop.workorder.client;

import com.bop.workorder.client.dto.NotifySendRequest;
import com.bop.workorder.common.BizException;
import com.bop.workorder.config.ExternalServiceProperties;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class NotifyClient {

    private static final String SEND_PATH = "/mock/notify/send";

    private final RestTemplate restTemplate;
    private final ExternalServiceProperties properties;

    public NotifyClient(RestTemplate restTemplate, ExternalServiceProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    public void send(String recipient, String title, String content, String bizId, String templateCode) {
        NotifySendRequest req = new NotifySendRequest();
        req.setChannel("SMS");
        req.setRecipient(recipient);
        req.setTitle(title);
        req.setContent(content);
        req.setBizId(bizId);
        req.setTemplateCode(templateCode);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<NotifySendRequest> entity = new HttpEntity<NotifySendRequest>(req, headers);

        String url = properties.getNotifyBaseUrl() + SEND_PATH;
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, new ParameterizedTypeReference<Map<String, Object>>() {
                });
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new BizException(50002, "Notify service returned HTTP " + response.getStatusCodeValue());
        }
        Map<String, Object> body = response.getBody();
        if (body == null || !Integer.valueOf(0).equals(body.get("code"))) {
            String msg = body != null && body.get("message") != null ? String.valueOf(body.get("message")) : "Notify send failed";
            throw new BizException(50002, msg);
        }
    }
}
