package com.bop.workorder.service;

import com.bop.workorder.client.dto.LanderCallbackPayload;
import com.bop.workorder.common.PageResult;
import com.bop.workorder.dto.WorkOrderApproveRequest;
import com.bop.workorder.dto.WorkOrderCloseRequest;
import com.bop.workorder.dto.WorkOrderCreateRequest;
import com.bop.workorder.dto.WorkOrderDetailVO;
import com.bop.workorder.dto.WorkOrderQueryRequest;
import com.bop.workorder.dto.WorkOrderSubmitRequest;
import com.bop.workorder.entity.WorkOrder;

public interface WorkOrderService {

    WorkOrder createWorkOrder(WorkOrderCreateRequest request);

    WorkOrder submitWorkOrder(String woNo, WorkOrderSubmitRequest request);

    WorkOrder approveWorkOrder(String woNo, WorkOrderApproveRequest request);

    WorkOrder closeWorkOrder(String woNo, WorkOrderCloseRequest request);

    WorkOrderDetailVO getDetail(String woNo);

    PageResult<WorkOrder> queryPage(WorkOrderQueryRequest request);

    void handleLanderCallback(LanderCallbackPayload payload);
}
