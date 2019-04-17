package ch.njol.skript.util;

import ch.njol.skript.Skript;
import ch.njol.skript.localization.Language;
import org.eclipse.jdt.annotation.Nullable;

import java.io.IOException;

public final class ExceptionUtils {

    private final static String IO_NODE = "io exceptions";

    private ExceptionUtils() {
        throw new UnsupportedOperationException();
    }

    @Nullable
    public static String toString(final IOException e) {
        if (Language.keyExists(IO_NODE + "." + e.getClass().getSimpleName())) {
            return Language.format(IO_NODE + "." + e.getClass().getSimpleName(), e.getLocalizedMessage());
        }
        if (Skript.testing())
            e.printStackTrace();
        return e.getLocalizedMessage();
    }

}
