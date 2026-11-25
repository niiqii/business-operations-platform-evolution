package com.bop.mock.lander.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MockProcessInstance {

    private String piid;
    private String applyId;
    private String baseSchema;
    private String title;
    private String externalFormUrl;
    private int state;
    private String currentNodeId;
    private String currentNodeDisplayName;
    private String terminateReason;
    private LanderUserInfo creator;
    private Map<String, String> extendData = new HashMap<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<TaskObject> tasks = new ArrayList<>();

    public String getPiid() {
        return piid;
    }

    public void setPiid(String piid) {
        this.piid = piid;
    }

    public String getApplyId() {
        return applyId;
    }

    public void setApplyId(String applyId) {
        this.applyId = applyId;
    }

    public String getBaseSchema() {
        return baseSchema;
    }

    public void setBaseSchema(String baseSchema) {
        this.baseSchema = baseSchema;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getExternalFormUrl() {
        return externalFormUrl;
    }

    public void setExternalFormUrl(String externalFormUrl) {
        this.externalFormUrl = externalFormUrl;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getCurrentNodeId() {
        return currentNodeId;
    }

    public void setCurrentNodeId(String currentNodeId) {
        this.currentNodeId = currentNodeId;
    }

    public String getCurrentNodeDisplayName() {
        return currentNodeDisplayName;
    }

    public void setCurrentNodeDisplayName(String currentNodeDisplayName) {
        this.currentNodeDisplayName = currentNodeDisplayName;
    }

    public String getTerminateReason() {
        return terminateReason;
    }

    public void setTerminateReason(String terminateReason) {
        this.terminateReason = terminateReason;
    }

    public LanderUserInfo getCreator() {
        return creator;
    }

    public void setCreator(LanderUserInfo creator) {
        this.creator = creator;
    }

    public Map<String, String> getExtendData() {
        return extendData;
    }

    public void setExtendData(Map<String, String> extendData) {
        this.extendData = extendData;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<TaskObject> getTasks() {
        return tasks;
    }

    public void setTasks(List<TaskObject> tasks) {
        this.tasks = tasks;
    }
}
