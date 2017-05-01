package org.objectstyle.taskextractor;


import com.google.inject.Inject;
import com.google.inject.Provider;
import io.bootique.cli.Cli;
import io.bootique.command.CommandOutcome;
import io.bootique.command.CommandWithMetadata;
import io.bootique.log.BootLogger;
import io.bootique.meta.application.CommandMetadata;
import io.bootique.meta.application.OptionMetadata;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.Comparator;

public class ExtractCommand extends CommandWithMetadata {

    private static final String MONTH_OPT = "month";

    private Provider<TaskExtractor> extractorProvider;
    private BootLogger logger;

    @Inject
    public ExtractCommand(Provider<TaskExtractor> extractorProvider, BootLogger logger) {
        super(CommandMetadata.builder(ExtractCommand.class)
                .addOption(OptionMetadata.builder(MONTH_OPT, "Report month.").valueRequired("YYYY-MM")));
        this.extractorProvider = extractorProvider;
        this.logger = logger;
    }

    @Override
    public CommandOutcome run(Cli cli) {

        String monthString = cli.optionString(MONTH_OPT);

        if (monthString == null) {
            return CommandOutcome.failed(-1, "Month is not specified. Use -m / --month option.");
        }

        YearMonth month;
        try {
            month = YearMonth.parse(monthString);
        } catch (DateTimeParseException e) {
            return CommandOutcome.failed(-1, "Invalid month argument format: " + monthString + ". Must be YYYY-mm.");
        }

        StringBuilder result = new StringBuilder();
        extractorProvider.get()
                .extract(month.atDay(1), month.atEndOfMonth())
                .sorted(Comparator.comparing(Commit::getTime))
                .forEach(c -> result.append(c.toTabSeparated()).append("\n"));

        logger.stdout(result.toString());
        return CommandOutcome.succeeded();
    }
}
