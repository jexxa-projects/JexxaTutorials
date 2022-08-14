package io.jexxa.tutorials.contractmanagement;

import io.jexxa.addend.applicationcore.DomainService;
import io.jexxa.core.JexxaMain;
import io.jexxa.infrastructure.drivingadapter.rest.RESTfulRPCAdapter;
import io.jexxa.tutorials.contractmanagement.applicationservice.ContractService;

public class ContractManagement
{
    public static void main(String[] args)
    {
        var jexxaMain = new JexxaMain(ContractManagement.class);

        jexxaMain
                .bootstrapAnnotation(DomainService.class)

                .bind(RESTfulRPCAdapter.class).to(ContractService.class)
                .bind(RESTfulRPCAdapter.class).to(jexxaMain.getBoundedContext())

                .run();
    }

    private ContractManagement()
    {
        //Private constructor since we only offer main
    }

}
