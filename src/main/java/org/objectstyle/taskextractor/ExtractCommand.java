package org.objectstyle.taskextractor;


import org.dflib.DataFrame;
import org.dflib.excel.Excel;
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

import static org.dflib.Exp.$col;
import static org.dflib.Exp.$str;

public class ExtractCommand extends CommandWithMetadata {

    private static final String MONTH_OPT = "month";
    private static final String OUT_FILE_OPT = "out-file";

    private final Provider<TaskExtractor> extractorProvider;

    @Inject
    public ExtractCommand(Provider<TaskExtractor> extractorProvider) {
        super(CommandMetadata.builder(ExtractCommand.class)
                .addOption(OptionMetadata.builder(MONTH_OPT, "Report month.").valueRequired("YYYY-MM").build())
                .addOption(OptionMetadata.builder(OUT_FILE_OPT, "Output Excel file").valueRequired().build())
                .build());
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
                .sort($col(Commit.TIME.ordinal()).asc())
                .cols("MESSAGE", "WEEKEND", "DATE", "TIME")
                .merge(
                        $str(Commit.MESSAGE.ordinal()).mapVal(Commit.trimMessage()),
                        $col(Commit.TIME.ordinal()).mapVal(o -> Commit.weekend((ZonedDateTime) o)),
                        $col(Commit.TIME.ordinal()).mapVal(o -> ((ZonedDateTime) o).toLocalDate()),
                        $col(Commit.TIME.ordinal()).mapVal(o -> ((ZonedDateTime) o).toLocalTime()))
                .cols("DATE", "WEEKEND", "TIME", "REPO", "MESSAGE", "USER", "HASH").select();

        Excel.saver().autoSizeColumns().saveSheet(df, outFile, "Sheet 1");
        return CommandOutcome.succeeded();
    }
}
