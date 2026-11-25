package com.bop.workorder.client.dto;

public class LanderTaskObject {

    private String tiid;
    private String piid;
    private String applyId;
    private String templateId;
    private String nodeId;
    private String nodeDisplayName;
    private int state;
    private boolean first;
    private long createTime;
    private long lastModified;
    private LanderUserInfo applicant;
    private LanderUserInfo originalOwner;

    public String getTiid() {
        return tiid;
    }

    public void setTiid(String tiid) {
        this.tiid = tiid;
    }

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

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
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

    public LanderUserInfo getApplicant() {
        return applicant;
    }

    public void setApplicant(LanderUserInfo applicant) {
        this.applicant = applicant;
    }

    public LanderUserInfo getOriginalOwner() {
        return originalOwner;
    }

    public void setOriginalOwner(LanderUserInfo originalOwner) {
        this.originalOwner = originalOwner;
    }
}
