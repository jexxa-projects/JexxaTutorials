package io.jexxa.tutorials.contractmanagement;

import io.jexxa.core.JexxaMain;
import io.jexxa.infrastructure.drivingadapter.rest.RESTfulRPCAdapter;
import io.jexxa.tutorials.contractmanagement.applicationservice.ContractService;
import io.jexxa.tutorials.contractmanagement.domainservice.ContractAuditService;

public class ContractManagement
{
    public static void main(String[] args)
    {
        var jexxaMain = new JexxaMain(ContractManagement.class);

        jexxaMain
                .bootstrap(ContractAuditService.class).and()      // Bootstrap audit service first so that we capture all DomainEvents

                .bind(RESTfulRPCAdapter.class).to(ContractService.class)
                .bind(RESTfulRPCAdapter.class).to(jexxaMain.getBoundedContext())

                .run();
    }

    private ContractManagement()
    {
        //Private constructor since we only offer main
    }

}
