package com.bop.workorder.client.dto;

import java.util.List;

public class LanderCallbackPayload {

    private String piid;
    private String systemId;
    private String templateId;
    private int templateVer;
    private String businessProcess;
    private String applyId;
    private long modifiedTime;
    private boolean endFlow;
    private List<CallbackInfo> callbackInfos;

    public String getPiid() {
        return piid;
    }

    public void setPiid(String piid) {
        this.piid = piid;
    }

    public String getSystemId() {
        return systemId;
    }

    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public int getTemplateVer() {
        return templateVer;
    }

    public void setTemplateVer(int templateVer) {
        this.templateVer = templateVer;
    }

    public String getBusinessProcess() {
        return businessProcess;
    }

    public void setBusinessProcess(String businessProcess) {
        this.businessProcess = businessProcess;
    }

    public String getApplyId() {
        return applyId;
    }

    public void setApplyId(String applyId) {
        this.applyId = applyId;
    }

    public long getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(long modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    public boolean isEndFlow() {
        return endFlow;
    }

    public void setEndFlow(boolean endFlow) {
        this.endFlow = endFlow;
    }

    public List<CallbackInfo> getCallbackInfos() {
        return callbackInfos;
    }

    public void setCallbackInfos(List<CallbackInfo> callbackInfos) {
        this.callbackInfos = callbackInfos;
    }

    public static class CallbackInfo {
        private String preNodeId;
        private String nodeId;
        private String nodeDisplayName;
        private boolean first;
        private List<CallbackTask> callbackTasks;
        private List<CallbackTask> preCallbackTasks;

        public String getPreNodeId() {
            return preNodeId;
        }

        public void setPreNodeId(String preNodeId) {
            this.preNodeId = preNodeId;
        }

        public String getNodeId() {
            return nodeId;
        }

        public void setNodeId(String nodeId) {
            this.nodeId = nodeId;
        }

        public String getNodeDisplayName() {
            return nodeDisplayName;
        }

        public void setNodeDisplayName(String nodeDisplayName) {
            this.nodeDisplayName = nodeDisplayName;
        }

        public boolean isFirst() {
            return first;
        }

        public void setFirst(boolean first) {
            this.first = first;
        }

        public List<CallbackTask> getCallbackTasks() {
            return callbackTasks;
        }

        public void setCallbackTasks(List<CallbackTask> callbackTasks) {
            this.callbackTasks = callbackTasks;
        }

        public List<CallbackTask> getPreCallbackTasks() {
            return preCallbackTasks;
        }

        public void setPreCallbackTasks(List<CallbackTask> preCallbackTasks) {
            this.preCallbackTasks = preCallbackTasks;
        }
    }

    public static class CallbackTask {
        private String tiid;
        private int state;
        private boolean first;
        private long createTime;
        private long lastModified;
        private LanderUserInfo originalOwner;

        public String getTiid() {
            return tiid;
        }

        public void setTiid(String tiid) {
            this.tiid = tiid;
        }

        public int getState() {
            return state;
        }

        public void setState(int state) {
            this.state = state;
        }

        public boolean isFirst() {
            return first;
        }

        public void setFirst(boolean first) {
            this.first = first;
        }

        public long getCreateTime() {
            return createTime;
        }

        public void setCreateTime(long createTime) {
            this.createTime = createTime;
        }

        public long getLastModified() {
            return lastModified;
        }

        public void setLastModified(long lastModified) {
            this.lastModified = lastModified;
        }

        public LanderUserInfo getOriginalOwner() {
            return originalOwner;
        }

        public void setOriginalOwner(LanderUserInfo originalOwner) {
            this.originalOwner = originalOwner;
        }
    }
}
