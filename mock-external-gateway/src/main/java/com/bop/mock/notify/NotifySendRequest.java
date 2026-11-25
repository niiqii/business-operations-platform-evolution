package com.bop.mock.notify;

import jakarta.validation.constraints.NotBlank;

public class NotifySendRequest {

    @NotBlank(message = "channel is required")
    private String channel;

    @NotBlank(message = "recipient is required")
    private String recipient;

    private String title;

    @NotBlank(message = "content is required")
    private String content;

    private String bizId;
    private String templateCode;

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getBizId() {
        return bizId;
    }

    public void setBizId(String bizId) {
        this.bizId = bizId;
    }

    public String getTemplateCode() {
        return templateCode;
    }

    public void setTemplateCode(String templateCode) {
        this.templateCode = templateCode;
    }
}
