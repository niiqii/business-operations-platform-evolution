package com.bop.mock.lander.model;

import java.util.List;

public record CallbackPayload(
        String piid,
        String systemId,
        String templateId,
        int templateVer,
        String businessProcess,
        String applyId,
        long modifiedTime,
        boolean endFlow,
        List<CallbackInfo> callbackInfos
) {

    public CallbackPayload withModifiedTime(long newModifiedTime) {
        return new CallbackPayload(
                piid, systemId, templateId, templateVer,
                businessProcess, applyId, newModifiedTime, endFlow, callbackInfos
        );
    }

    public record CallbackInfo(
            String preNodeId,
            String nodeId,
            String nodeDisplayName,
            boolean first,
            List<CallbackTask> callbackTasks,
            List<CallbackTask> preCallbackTasks
    ) {
    }

    public record CallbackTask(
            String tiid,
            int state,
            boolean first,
            long createTime,
            long lastModified,
            LanderUserInfo originalOwner
    ) {
    }
}
