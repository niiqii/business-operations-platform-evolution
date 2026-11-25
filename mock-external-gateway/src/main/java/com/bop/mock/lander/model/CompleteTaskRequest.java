package com.bop.mock.lander.model;

import java.util.List;
import java.util.Map;

public class CompleteTaskRequest {

    private String baseSchema;
    private String tiid;
    private LanderUserInfo handler;
    private String decision;
    private String option;
    private String notion;
    private String nextNode;
    private List<LanderUserInfo> nextHandlers;
    private Map<String, String> extendData;

    public String getBaseSchema() {
        return baseSchema;
    }

    public void setBaseSchema(String baseSchema) {
        this.baseSchema = baseSchema;
    }

    public String getTiid() {
        return tiid;
    }

    public void setTiid(String tiid) {
        this.tiid = tiid;
    }

    public LanderUserInfo getHandler() {
        return handler;
    }

    public void setHandler(LanderUserInfo handler) {
        this.handler = handler;
    }

    public String getDecision() {
        return decision;
    }

    public void setDecision(String decision) {
        this.decision = decision;
    }

    public String getOption() {
        return option;
    }

    public void setOption(String option) {
        this.option = option;
    }

    public String getNotion() {
        return notion;
    }

    public void setNotion(String notion) {
        this.notion = notion;
    }

    public String getNextNode() {
        return nextNode;
    }

    public void setNextNode(String nextNode) {
        this.nextNode = nextNode;
    }

    public List<LanderUserInfo> getNextHandlers() {
        return nextHandlers;
    }

    public void setNextHandlers(List<LanderUserInfo> nextHandlers) {
        this.nextHandlers = nextHandlers;
    }

    public Map<String, String> getExtendData() {
        return extendData;
    }

    public void setExtendData(Map<String, String> extendData) {
        this.extendData = extendData;
    }
}
