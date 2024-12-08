package org.objectstyle.taskextractor;


import io.bootique.BootiqueException;
import io.bootique.cli.Cli;
import io.bootique.command.CommandOutcome;
import io.bootique.command.CommandWithMetadata;
import io.bootique.meta.application.CommandMetadata;
import io.bootique.meta.application.OptionMetadata;
import org.dflib.DataFrame;
import org.dflib.excel.Excel;

import javax.inject.Inject;
import javax.inject.Provider;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

import static org.dflib.Exp.$col;
import static org.dflib.Exp.$str;

public class ExtractCommand extends CommandWithMetadata {

    private static final String MONTHS_OPT = "months";
    private static final String MONTH_OPT = "month";
    private static final String YEAR_OPT = "year";

    private static final String OUT_FILE_OPT = "out-file";

    private final Provider<TaskExtractor> extractorProvider;

    @Inject
    public ExtractCommand(Provider<TaskExtractor> extractorProvider) {
        super(CommandMetadata.builder(ExtractCommand.class)
                .addOption(OptionMetadata.builder(YEAR_OPT, "Report year.").valueRequired("YYYY").build())
                .addOption(OptionMetadata.builder(MONTH_OPT, "Report month.").valueRequired("YYYY-MM").build())
                .addOption(OptionMetadata.builder(MONTHS_OPT, "How many months starting from this month to include in the report").valueRequired("nnn").build())
                .addOption(OptionMetadata.builder(OUT_FILE_OPT, "Output Excel file").valueRequired().build())
                .build());
        this.extractorProvider = extractorProvider;
    }

    @Override
    public CommandOutcome run(Cli cli) {

        LocalDate[] dateRange = extractionRange(cli);
        String outFile = cli.optionString(OUT_FILE_OPT);
        if (outFile == null) {
            return CommandOutcome.failed(-1, "Output Excel file is not specified. Use -o / --out-file option.");
        }

        DataFrame df = extractorProvider.get()
                .extract(dateRange[0], dateRange[1])
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

    private LocalDate[] extractionRange(Cli cli) {

        String[] opts = new String[]{
                cli.optionString(YEAR_OPT),
                cli.optionString(MONTH_OPT),
                cli.optionString(MONTHS_OPT)
        };

        int cardinality = 0;
        for (int i = 0; i < opts.length; i++) {
            if (opts[i] != null) {
                cardinality++;
            }
        }

        if (cardinality == 0) {
            YearMonth lastMonth = YearMonth.now().minusMonths(1);
            return new LocalDate[]{lastMonth.atDay(1), lastMonth.atEndOfMonth()};
        }

        if (cardinality > 1) {
            throw new BootiqueException(-1, "Only one of '--year', '--month' and '--months' can be specified.");
        }

        if (opts[0] != null) {
            Year year;
            try {
                year = Year.parse(opts[0]);
            } catch (DateTimeParseException e) {
                throw new BootiqueException(-1, "Invalid year argument format: " + opts[0] + ". Must be YYYY.");
            }

            return new LocalDate[]{year.atDay(1), year.atMonth(12).atEndOfMonth()};
        }

        if (opts[1] != null) {
            YearMonth month;
            try {
                month = YearMonth.parse(opts[1]);
            } catch (DateTimeParseException e) {
                throw new BootiqueException(-1, "Invalid month argument format: " + opts[1] + ". Must be YYYY-mm.");
            }

            return new LocalDate[]{month.atDay(1), month.atEndOfMonth()};
        }

        int months;
        try {
            months = Integer.parseInt(opts[2]);
        } catch (DateTimeParseException e) {
            throw new BootiqueException(-1, "Invalid months argument format: " + opts[2] + ". Must be nnn");
        }

        LocalDate now = LocalDate.now();
        return new LocalDate[]{now.minusMonths(months), now};
    }
}
