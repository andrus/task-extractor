package org.objectstyle.taskextractor;


import com.nhl.dflib.DataFrame;
import com.nhl.dflib.excel.Excel;
import io.bootique.cli.Cli;
import io.bootique.command.CommandOutcome;
import io.bootique.command.CommandWithMetadata;
import io.bootique.meta.application.CommandMetadata;
import io.bootique.meta.application.OptionMetadata;

import javax.inject.Inject;
import javax.inject.Provider;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

import static com.nhl.dflib.Exp.$col;

public class ExtractCommand extends CommandWithMetadata {

    private static final String MONTH_OPT = "month";
    private static final String OUT_FILE_OPT = "out-file";

    private Provider<TaskExtractor> extractorProvider;

    @Inject
    public ExtractCommand(Provider<TaskExtractor> extractorProvider) {
        super(CommandMetadata.builder(ExtractCommand.class)
                .addOption(OptionMetadata.builder(MONTH_OPT, "Report month.").valueRequired("YYYY-MM"))
                .addOption(OptionMetadata.builder(OUT_FILE_OPT, "Output Excel file").valueRequired()));
        this.extractorProvider = extractorProvider;
    }

    @Override
    public CommandOutcome run(Cli cli) {

        String monthString = cli.optionString(MONTH_OPT);
        if (monthString == null) {
            return CommandOutcome.failed(-1, "Month is not specified. Use -m / --month option.");
        }

        String outFile = cli.optionString(OUT_FILE_OPT);
        if (outFile == null) {
            return CommandOutcome.failed(-1, "Output Excel file is not specified. Use -o / --out-file option.");
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
                .sort($col(Commit.TIME.ordinal()).asc())
                .addColumn($col(Commit.TIME.ordinal()).mapVal(o -> Commit.weekend((ZonedDateTime) o)).as("WEEKEND"))
                .addColumn($col(Commit.TIME.ordinal()).mapVal(o -> ((ZonedDateTime) o).toLocalDate()).as("DATE"))
                .addColumn($col(Commit.TIME.ordinal()).mapVal(o -> ((ZonedDateTime) o).toLocalTime()).as("TIME_"))
                .dropColumns("TIME")
                .renameColumn("TIME_", "TIME")
                .selectColumns("DATE", "WEEKEND", "TIME", "REPO", "MESSAGE", "USER", "HASH");

        Excel.saver().autoSizeColumns().saveSheet(df, outFile, "Sheet 1");
        return CommandOutcome.succeeded();
    }
}
