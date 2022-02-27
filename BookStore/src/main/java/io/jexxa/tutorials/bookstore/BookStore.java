package io.jexxa.tutorials.bookstore;


import io.jexxa.core.JexxaMain;
import io.jexxa.infrastructure.drivenadapterstrategy.messaging.MessageSender;
import io.jexxa.infrastructure.drivenadapterstrategy.messaging.MessageSenderManager;
import io.jexxa.infrastructure.drivenadapterstrategy.messaging.jms.JMSSender;
import io.jexxa.infrastructure.drivenadapterstrategy.messaging.logging.MessageLogger;
import io.jexxa.infrastructure.drivenadapterstrategy.persistence.repository.IRepository;
import io.jexxa.infrastructure.drivenadapterstrategy.persistence.repository.RepositoryManager;
import io.jexxa.infrastructure.drivenadapterstrategy.persistence.repository.imdb.IMDBRepository;
import io.jexxa.infrastructure.drivenadapterstrategy.persistence.repository.jdbc.JDBCKeyValueRepository;
import io.jexxa.infrastructure.drivingadapter.rest.RESTfulRPCAdapter;
import io.jexxa.tutorials.bookstore.applicationservice.BookStoreService;
import io.jexxa.tutorials.bookstore.domainservice.ReferenceLibrary;
import io.jexxa.utils.JexxaLogger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public final class BookStore
{
    public static void main(String[] args)
    {
        // Define the default strategies.
        // In this tutorial the Repository is either an IMDB database or a JDBC based repository.
        // In case of JDBC we use a simple key value approach which stores the key and the value as json strings.
        // Using json strings might be very inconvenient if you come from typical relational databases but in terms
        // of DDD our aggregate is responsible to ensure consistency of our data and not the database.
        RepositoryManager.setDefaultStrategy(getRepositoryStrategy(args));
        // The message sender is either a simple MessageLogger or a JMS sender.
        MessageSenderManager.setDefaultStrategy(getMessagingStrategy(args));

        // Define the default strategy for messaging which is either a simple logger called `MessageLogger.class` or `JMSSender.class` for JMS messages
        MessageSenderManager.setDefaultStrategy(MessageLogger.class);

        var jexxaMain = new JexxaMain(BookStore.class);

        //print some application information
        JexxaLogger.getLogger(BookStore.class)
                .info( "{}", jexxaMain.getBoundedContext().getContextVersion() );
        jexxaMain
                //Define the default packages for inbound and outbound ports
                .addDDDPackages(BookStore.class)

                //Get the latest books when starting the application
                .bootstrap(ReferenceLibrary.class).with(ReferenceLibrary::addLatestBooks)

                .bind(RESTfulRPCAdapter.class).to(BookStoreService.class)
                .bind(RESTfulRPCAdapter.class).to(jexxaMain.getBoundedContext())

                .start()

                .waitForShutdown()

                .stop();
    }

    // Methods for command line parsing
    static Options getOptions()
    {
        var options = new Options();
        options.addOption("j", "jdbc", false, "jdbc driven adapter strategy");
        options.addOption("J", "jms", false, "JMS message sender");
        return options;
    }


    @SuppressWarnings("rawtypes")
    private static Class<? extends IRepository> getRepositoryStrategy(String[] args)
    {
        if (parameterAvailable("jdbc", args))
        {
            JexxaLogger.getLogger(BookStore.class).info("Use persistence strategy: {} ", JDBCKeyValueRepository.class.getSimpleName());
            return JDBCKeyValueRepository.class;
        }

        JexxaLogger.getLogger(BookStore.class).info("Use persistence strategy: {} ", IMDBRepository.class.getSimpleName());
        return IMDBRepository.class;
    }

    private static Class<? extends MessageSender> getMessagingStrategy(String[] args)
    {
        if (parameterAvailable("jms", args))
        {
            JexxaLogger.getLogger(BookStore.class).info("Use messaging strategy: {} ", JMSSender.class.getSimpleName());
            return JMSSender.class;
        }

        JexxaLogger.getLogger(BookStore.class).info("Use messaging strategy: {} ", MessageLogger.class.getSimpleName());
        return MessageLogger.class;
    }

    static boolean parameterAvailable(String parameter, String[] args)
    {
        CommandLineParser parser = new DefaultParser();
        try
        {
            CommandLine line = parser.parse( getOptions(), args );

            return line.hasOption(parameter);
        }
        catch( ParseException exp ) {
            JexxaLogger.getLogger(BookStore.class)
                    .error( "Parsing failed.  Reason: {}", exp.getMessage() );
        }
        return false;
    }



    private BookStore()
    {
        //Private constructor since we only offer main
    }


}
