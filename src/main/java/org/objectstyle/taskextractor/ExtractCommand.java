package org.objectstyle.taskextractor;


import com.google.inject.Inject;
import com.google.inject.Provider;
import io.bootique.cli.Cli;
import io.bootique.command.Command;
import io.bootique.command.CommandOutcome;
import io.bootique.log.BootLogger;

import java.util.Comparator;

public class ExtractCommand implements Command {


    private Provider<TaskExtractor> extractorProvider;
    private BootLogger logger;

    @Inject
    public ExtractCommand(Provider<TaskExtractor> extractorProvider, BootLogger logger) {
        this.extractorProvider = extractorProvider;
        this.logger = logger;
    }

    @Override
    public CommandOutcome run(Cli cli) {
        StringBuilder result = new StringBuilder();
        extractorProvider
                .get().extract().stream()
                .sorted(Comparator.comparing(Commit::getTime))
                .forEach(c -> result.append(c.toTabSeparated()).append("\n"));

        logger.stdout(result.toString());
        return CommandOutcome.succeeded();
    }
}
