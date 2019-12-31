package org.objectstyle.taskextractor;


import com.nhl.dflib.DataFrame;
import com.nhl.dflib.csv.Csv;
import io.bootique.cli.Cli;
import io.bootique.command.CommandOutcome;
import io.bootique.command.CommandWithMetadata;
import io.bootique.log.BootLogger;
import io.bootique.meta.application.CommandMetadata;
import io.bootique.meta.application.OptionMetadata;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.StringWriter;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;

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

        DataFrame df = extractorProvider.get()
                .extract(month.atDay(1), month.atEndOfMonth())
                .convertColumn(Commit.MESSAGE.ordinal(), Commit.trimMessage())
                .sort(Commit.TIME.ordinal(), true);

        StringWriter csv = new StringWriter();
        Csv.saver().save(df, csv);
        logger.stdout(csv.toString());

        return CommandOutcome.succeeded();
    }
}
