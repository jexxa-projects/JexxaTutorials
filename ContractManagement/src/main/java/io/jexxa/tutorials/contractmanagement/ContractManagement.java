package io.jexxa.tutorials.contractmanagement;

import io.jexxa.core.JexxaMain;
import io.jexxa.drivingadapter.rest.RESTfulRPCAdapter;
import io.jexxa.tutorials.contractmanagement.application.service.ContractService;
import io.jexxa.tutorials.contractmanagement.application.service.ContractAuditService;

public class ContractManagement
{
    static void main()
    {
        var jexxaMain = new JexxaMain(ContractManagement.class);

        jexxaMain
                .bootstrap(ContractAuditService.class).with(ContractAuditService::initialize)      // Bootstrap audit service first so that we capture all DomainEvents

                .bind(RESTfulRPCAdapter.class).to(ContractService.class)
                .bind(RESTfulRPCAdapter.class).to(ContractAuditService.class)
                .bind(RESTfulRPCAdapter.class).to(jexxaMain.getBoundedContext())

                .run();
    }

}
