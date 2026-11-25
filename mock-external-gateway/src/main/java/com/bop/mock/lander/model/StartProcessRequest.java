package com.bop.mock.lander.model;

import java.util.List;
import java.util.Map;

public class StartProcessRequest {

    private String baseSchema;
    private String applyId;
    private String title;
    private Boolean submit;
    private LanderUserInfo handler;
    private List<LanderUserInfo> nextHandlers;
    private String externalFormUrl;
    private Map<String, String> extendData;

    public String getBaseSchema() {
        return baseSchema;
    }

    public void setBaseSchema(String baseSchema) {
        this.baseSchema = baseSchema;
    }

    public String getApplyId() {
        return applyId;
    }

    public void setApplyId(String applyId) {
        this.applyId = applyId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Boolean getSubmit() {
        return submit;
    }

    public void setSubmit(Boolean submit) {
        this.submit = submit;
    }

    public LanderUserInfo getHandler() {
        return handler;
    }

    public void setHandler(LanderUserInfo handler) {
        this.handler = handler;
    }

    public List<LanderUserInfo> getNextHandlers() {
        return nextHandlers;
    }

    public void setNextHandlers(List<LanderUserInfo> nextHandlers) {
        this.nextHandlers = nextHandlers;
    }

    public String getExternalFormUrl() {
        return externalFormUrl;
    }

    public void setExternalFormUrl(String externalFormUrl) {
        this.externalFormUrl = externalFormUrl;
    }

    public Map<String, String> getExtendData() {
        return extendData;
    }

    public void setExtendData(Map<String, String> extendData) {
        this.extendData = extendData;
    }
}
