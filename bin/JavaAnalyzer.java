import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.function.Supplier;

final class Main {
    private static boolean debug = false;

    private static boolean noConcenationWarning = false;
    private static boolean noMethodFinalWarning = false;
    private static boolean noCatchFinalWarning = false;

    private static long processedFiles;
    private static long processedLines;

	private static long printedWarnings;

	private static final Pattern PATTERN_ON_SPACE = Pattern.compile(" ", Pattern.LITERAL);
	private static final Pattern PATTERN_ON_BRACKET = Pattern.compile("(", Pattern.LITERAL);

    public static final void main(final String[] args) throws IOException {
        System.out.println("");

        // We don't catch the IOException, so, we need this to handle it.

        final Thread.UncaughtExceptionHandler uncaughtExceptionHandler = (final var thread, final var exception) -> exception.printStackTrace();

        Thread.setDefaultUncaughtExceptionHandler(uncaughtExceptionHandler);
        Thread.currentThread().setUncaughtExceptionHandler(uncaughtExceptionHandler);

        if (null == args || args.length < 1) {
            enterDirectory();
            return;
        }

        final var directory = new File(args[0]);

        if (!directory.exists() || !directory.isDirectory()) {
            enterDirectory();
            return;
        }

        if (args.length > 1)
            for (final var arg : args)
                if ("--debug".equalsIgnoreCase(arg))
                    debug = true;
                else if ("--no-concenation-warning".equalsIgnoreCase(arg))
                    noConcenationWarning = true;
                else if ("--no-method-final-warning".equalsIgnoreCase(arg))
                    noMethodFinalWarning = true;
                else if ("--no-catch-final-warning".equalsIgnoreCase(arg))
                    noCatchFinalWarning = true;

        debug(() -> "Concenation warnings " + (noConcenationWarning ? "disabled" : "enabled"));
        debug(() -> "Method may be final warnings " + (noMethodFinalWarning ? "disabled" : "enabled"));
        debug(() -> "Catched exception may be final warnings " + (noCatchFinalWarning ? "disabled" : "enabled"));
        debug(() -> "");

        System.out.println("Processing files, please wait...");
        System.out.println();

        processDirectory(directory);
		System.out.flush();

        System.out.println();
        System.out.println("Processed total of " + processedFiles + " files and " + processedLines + " lines. Total of " + printedWarnings + " warnings found.");
		System.out.println();
    }

    private static final void processDirectory(final File directory) throws IOException {
        debug(() -> "Processing directory " + directory.getName());
        if (!directory.exists())
            throw new IllegalArgumentException("Non-existent directory given");
        if (!directory.isDirectory())
            throw new IllegalArgumentException("Not a directory");

        for (final var file : directory.listFiles()) {
            if (file.isDirectory())
                processDirectory(file);
			else
				analyzeFile(file);
        }
    }

    private static final void enterDirectory() {
        System.out.println("Please enter a directory name!");
    }

    private static final void debug(final Supplier<String> message) {
        if (debug)
            System.out.println(message.get());
    }

    private static final void analyzeFile(final File file) throws IOException {
        if (!file.exists())
            throw new IllegalArgumentException("Non-existent file given");
        if (file.isDirectory())
            throw new IllegalArgumentException("Can't analyze a directory");

        analyzeFile(file, Files.readAllLines(Paths.get(file.getPath()), StandardCharsets.UTF_8));
    }

    private static final void analyzeFile(final File file, final String contents) throws IOException {
        analyzeFile(file, contents.split(File.separator));
    }

    private static final void analyzeFile(final File file, final String[] lines) throws IOException {
        analyzeFile(file, Arrays.asList(lines));
    }

    private static final void analyzeFile(final File file, final List<String> lines) throws IOException {
        final var fileName = file.getName().trim();

        if (!fileName.endsWith(".java")) {
            debug(() -> "Skipping analyzing of " + fileName + " as it does not seem to be a java source file");
			return;
		}

        if (fileName.equalsIgnoreCase("package-info.java")) {
            debug(() -> "Skipping package info file " + fileName);
			return;
		}

		if (fileName.equalsIgnoreCase("module-info.java")) {
			debug(() -> "Skipping module info file " + fileName);
			return;
		}

        debug(() -> "Processing file " + fileName);

        var i = 0;

        for (final var line : lines) {
			i++;

			final var trimmedLine = line.trim();

			if (trimmedLine.startsWith("//"))
				continue; // Commented out line does not matter

			if (line.contains("// JavaAnalyzer ignore"))
				continue; // Ignored by supression comment

            if ((line.contains("static") && !line.contains("statically") && !line.contains("initializer") && !line.contains("is static") && !line.contains("non-static") && !line.contains("static {")) && !line.contains("=") && line.contains("(") && !line.contains("static final") && !noMethodFinalWarning)
                warning(file, line, i, "Static method maybe final");
            else if ((line.contains("private")) && !line.contains("*") /* javadoc */ && !line.contains("static") && !line.contains("private final") && !line.contains("=") && !line.contains("val") && line.contains("(") && !noMethodFinalWarning) {
				if (PATTERN_ON_SPACE.split(PATTERN_ON_BRACKET.split(trimmedLine)[0]).length != 2) // constructor
					warning(file, line, i, "Private method maybe final");
			} else if ((line.contains("\"\"+") || line.contains("\"\" +")) && !line.contains("\\\"\"") && !noConcenationWarning)
                warning(file, line, i, "Redundant empty string concenation");
            else if ((line.contains("catch(") || line.contains("catch (")) && !line.contains("(final ") && !noCatchFinalWarning)
                warning(file, line, i, "Catched exception may be final");
        }

        processedLines += i;
        processedFiles++;
    }

	private static final void warning(final File file, final String line, final int lineNumber, final String message) {
		System.out.println(message + " (" + file.getName() + ", line " + lineNumber + "): " + line);
		printedWarnings++;
	}
}
