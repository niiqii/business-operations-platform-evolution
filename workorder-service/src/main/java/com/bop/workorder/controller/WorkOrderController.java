package com.bop.workorder.controller;

import com.bop.workorder.common.ApiResult;
import com.bop.workorder.common.PageResult;
import com.bop.workorder.dto.WorkOrderApproveRequest;
import com.bop.workorder.dto.WorkOrderCloseRequest;
import com.bop.workorder.dto.WorkOrderCreateRequest;
import com.bop.workorder.dto.WorkOrderDetailVO;
import com.bop.workorder.dto.WorkOrderQueryRequest;
import com.bop.workorder.dto.WorkOrderSubmitRequest;
import com.bop.workorder.entity.WorkOrder;
import com.bop.workorder.service.WorkOrderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/workorders")
public class WorkOrderController {

    private final WorkOrderService workOrderService;

    public WorkOrderController(WorkOrderService workOrderService) {
        this.workOrderService = workOrderService;
    }

    @PostMapping
    public ApiResult<WorkOrder> create(@Valid @RequestBody WorkOrderCreateRequest request) {
        return ApiResult.ok(workOrderService.createWorkOrder(request));
    }

    @PostMapping("/{woNo}/submit")
    public ApiResult<WorkOrder> submit(
            @PathVariable String woNo,
            @Valid @RequestBody WorkOrderSubmitRequest request) {
        return ApiResult.ok(workOrderService.submitWorkOrder(woNo, request));
    }

    @PostMapping("/{woNo}/approve")
    public ApiResult<WorkOrder> approve(
            @PathVariable String woNo,
            @Valid @RequestBody WorkOrderApproveRequest request) {
        return ApiResult.ok(workOrderService.approveWorkOrder(woNo, request));
    }

    @PostMapping("/{woNo}/close")
    public ApiResult<WorkOrder> close(
            @PathVariable String woNo,
            @RequestBody(required = false) WorkOrderCloseRequest request) {
        return ApiResult.ok(workOrderService.closeWorkOrder(woNo, request));
    }

    @GetMapping("/{woNo}")
    public ApiResult<WorkOrderDetailVO> detail(@PathVariable String woNo) {
        return ApiResult.ok(workOrderService.getDetail(woNo));
    }

    @GetMapping
    public ApiResult<PageResult<WorkOrder>> list(WorkOrderQueryRequest request) {
        return ApiResult.ok(workOrderService.queryPage(request));
    }
}
