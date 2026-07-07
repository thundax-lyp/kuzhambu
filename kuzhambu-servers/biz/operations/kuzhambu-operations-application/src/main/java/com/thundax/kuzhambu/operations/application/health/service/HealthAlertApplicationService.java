package com.thundax.kuzhambu.operations.application.health.service;

import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.operations.application.health.command.OperationsHealthAlertAckCommand;
import com.thundax.kuzhambu.operations.application.health.command.OperationsHealthAlertRecoverCommand;
import com.thundax.kuzhambu.operations.application.health.query.OperationsHealthAlertPageQuery;
import com.thundax.kuzhambu.operations.application.health.result.OperationsHealthAlertPageResult;

public interface HealthAlertApplicationService {

    PageResult<OperationsHealthAlertPageResult> page(OperationsHealthAlertPageQuery query, PageQuery pageQuery);

    void ack(OperationsHealthAlertAckCommand command);

    void recover(OperationsHealthAlertRecoverCommand command);
}
