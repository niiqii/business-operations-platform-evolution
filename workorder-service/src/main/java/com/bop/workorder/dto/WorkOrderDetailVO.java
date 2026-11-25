package com.bop.workorder.dto;

import com.bop.workorder.entity.WorkOrder;
import com.bop.workorder.entity.WorkOrderLog;

import java.util.List;

public class WorkOrderDetailVO {

    private WorkOrder workOrder;
    private List<WorkOrderLog> logs;

    public WorkOrderDetailVO() {
    }

    public WorkOrderDetailVO(WorkOrder workOrder, List<WorkOrderLog> logs) {
        this.workOrder = workOrder;
        this.logs = logs;
    }

    public WorkOrder getWorkOrder() {
        return workOrder;
    }

    public void setWorkOrder(WorkOrder workOrder) {
        this.workOrder = workOrder;
    }

    public List<WorkOrderLog> getLogs() {
        return logs;
    }

    public void setLogs(List<WorkOrderLog> logs) {
        this.logs = logs;
    }
}
