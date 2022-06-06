package io.jexxa.tutorials.contractmanagement;

import io.jexxa.core.JexxaMain;
import io.jexxa.infrastructure.drivingadapter.rest.RESTfulRPCAdapter;
import io.jexxa.tutorials.contractmanagement.applicationservice.ContractService;
import io.jexxa.utils.JexxaLogger;

import static io.jexxa.infrastructure.drivenadapterstrategy.messaging.MessageSenderManager.getDefaultMessageSender;
import static io.jexxa.infrastructure.drivenadapterstrategy.persistence.objectstore.ObjectStoreManager.getDefaultObjectStore;
import static io.jexxa.infrastructure.drivenadapterstrategy.persistence.repository.RepositoryManager.getDefaultRepository;

public class ContractManagement
{
    public static void main(String[] args)
    {
        // Define the default strategies via command line.
        // In this tutorial we use an ObjectStore which is either an IMDB database or a JDBC based repository.

        var jexxaMain = new JexxaMain(ContractManagement.class);

        JexxaLogger.getLogger(ContractManagement.class).info("Used Repository    : {}", getDefaultRepository(jexxaMain.getProperties()).getSimpleName());
        JexxaLogger.getLogger(ContractManagement.class).info("Used ObjectStore   : {}", getDefaultObjectStore(jexxaMain.getProperties()).getSimpleName());
        JexxaLogger.getLogger(ContractManagement.class).info("Used MessageSender : {}", getDefaultMessageSender(jexxaMain.getProperties()).getSimpleName());

        jexxaMain
                .bind(RESTfulRPCAdapter.class).to(ContractService.class)
                .bind(RESTfulRPCAdapter.class).to(jexxaMain.getBoundedContext())

                .run();
    }

    private ContractManagement()
    {
        //Private constructor since we only offer main
    }

}
