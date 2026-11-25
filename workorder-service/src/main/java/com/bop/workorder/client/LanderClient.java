package com.bop.workorder.client;

import com.bop.workorder.client.dto.LanderCompleteTaskRequest;
import com.bop.workorder.client.dto.LanderStartProcessRequest;
import com.bop.workorder.client.dto.LanderTaskObject;
import com.bop.workorder.client.dto.LanderUserInfo;
import com.bop.workorder.common.BizException;
import com.bop.workorder.config.ExternalServiceProperties;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Component
public class LanderClient {

    private static final String START_PROCESS_PATH = "/UniflowControlCenter/rest/RestProcessService/startProcess";
    private static final String COMPLETE_TASK_PATH = "/UniflowControlCenter/rest/RestTaskService/completeTask";
    private static final String GET_TASK_PATH = "/UniflowControlCenter/rest/RestTaskService/getTask/";

    private final RestTemplate restTemplate;
    private final ExternalServiceProperties properties;

    public LanderClient(RestTemplate restTemplate, ExternalServiceProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    public String buildDispatchString(String account, String displayName) {
        String normalizedAccount = extractAccount(account);
        String safeName = StringUtils.hasText(displayName) ? displayName : normalizedAccount;
        return "U#:" + normalizedAccount + "#:#:2#:0#:0#:0#:#:" + safeName + "#:#:#;";
    }

    public List<LanderUserInfo> parseDispatchString(String dispatchStr) {
        List<LanderUserInfo> users = new ArrayList<LanderUserInfo>();
        if (!StringUtils.hasText(dispatchStr)) {
            return users;
        }
        String[] entries = dispatchStr.split("#;");
        for (String entry : entries) {
            if (!StringUtils.hasText(entry)) {
                continue;
            }
            String[] parts = entry.split("#:", -1);
            if (parts.length < 2 || !StringUtils.hasText(parts[1])) {
                continue;
            }
            String prefix = StringUtils.hasText(parts[0]) ? parts[0].trim() : "U";
            String account = parts[1].trim();
            String userName = parts.length > 8 && StringUtils.hasText(parts[8]) ? parts[8].trim() : account;
            users.add(new LanderUserInfo(prefix + ":" + account, userName, "DEPT-01", "默认部门", ""));
        }
        return users;
    }

    public String extractAccount(String rawUserId) {
        if (!StringUtils.hasText(rawUserId)) {
            return null;
        }
        String trimmed = rawUserId.trim();
        if (trimmed.contains("#:")) {
            String[] parts = trimmed.split("#:", -1);
            return parts.length > 1 ? parts[1].trim() : trimmed;
        }
        int idx = trimmed.indexOf(':');
        return idx >= 0 ? trimmed.substring(idx + 1).trim() : trimmed;
    }

    public String startProcess(LanderStartProcessRequest request) {
        String url = properties.getLanderBaseUrl() + START_PROCESS_PATH;
        HttpEntity<LanderStartProcessRequest> entity = new HttpEntity<LanderStartProcessRequest>(request, buildAuthHeaders());
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, new ParameterizedTypeReference<Map<String, Object>>() {
                });
        Map<String, Object> body = response.getBody();
        if (!response.getStatusCode().is2xxSuccessful() || body == null || !Boolean.TRUE.equals(body.get("success"))) {
            String err = body != null && body.get("errmsg") != null ? String.valueOf(body.get("errmsg")) : "Lander startProcess failed";
            throw new BizException(50001, err);
        }
        Object piid = body.get("piid");
        if (piid == null || !StringUtils.hasText(String.valueOf(piid))) {
            throw new BizException(50001, "Lander startProcess returned empty piid");
        }
        return String.valueOf(piid);
    }

    public void completeTask(LanderCompleteTaskRequest request) {
        String url = properties.getLanderBaseUrl() + COMPLETE_TASK_PATH;
        HttpEntity<LanderCompleteTaskRequest> entity = new HttpEntity<LanderCompleteTaskRequest>(request, buildAuthHeaders());
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, new ParameterizedTypeReference<Map<String, Object>>() {
                });
        Map<String, Object> body = response.getBody();
        if (!response.getStatusCode().is2xxSuccessful() || body == null || !Boolean.TRUE.equals(body.get("success"))) {
            String err = body != null && body.get("errmsg") != null ? String.valueOf(body.get("errmsg")) : "Lander completeTask failed";
            throw new BizException(50001, err);
        }
    }

    public LanderTaskObject getTask(String tiid) {
        String url = properties.getLanderBaseUrl() + GET_TASK_PATH + tiid;
        HttpEntity<Void> entity = new HttpEntity<Void>(buildAuthHeaders());
        ResponseEntity<LanderTaskObject> response = restTemplate.exchange(url, HttpMethod.GET, entity, LanderTaskObject.class);
        return response.getBody();
    }

    private HttpHeaders buildAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String credentials = properties.getLanderUsername() + ":" + properties.getLanderPassword();
        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        headers.set(HttpHeaders.AUTHORIZATION, "Basic " + encoded);
        return headers;
    }
}
