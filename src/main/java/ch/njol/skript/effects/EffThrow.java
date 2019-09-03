/*
 *
 *     This file is part of Skript.
 *
 *    Skript is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    Skript is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with Skript. If not, see <https://www.gnu.org/licenses/>.
 *
 *
 *   Copyright 2011-2019 Peter Güttinger and contributors
 *
 */

package ch.njol.skript.effects;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("Custom Warning / Error")
@Description("Throws a custom warning / error.")
@Examples({"on load:", "\tset {id} to random uuid", "\tif {id} is not set:", "\t\tthrow new error \"Failed to set ID, please reload!\"", "\t\tstop # Throw does not stops execution, you must add stop!"})
@Since("2.2-Fixes-V10c, 2.2.14 (throwing java errors), 2.2.16 (finalized java errors)")
public final class EffThrow extends Effect {
    static {
        Skript.registerEffect(EffThrow.class, "throw[ a] [new] (0¦warning|1¦error|2¦java error) %string%");
    }

    private boolean error;
    private boolean java;

    @Nullable
    private String script;

    private int line;

    @SuppressWarnings("null")
    private Expression<String> detail;

    @Override
    @SuppressWarnings("null")
    public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final SkriptParser.ParseResult parseResult) {
        error = parseResult.mark > 0;
        java = parseResult.mark > 1;
        detail = (Expression<String>) exprs[0];
        if (ScriptLoader.currentScript != null)
            script = ScriptLoader.currentScript.getFileName();
        if (SkriptLogger.getNode() != null)
            line = SkriptLogger.getNode().getLine();
        return true;
    }

    private String getTypeName() {
        return error ? java ? "java error" : "error" : "warning";
    }

    @Override
    @SuppressWarnings("null")
    protected void execute(final Event e) {
        if (error) {
            if (java) {
                // Bad things happening - throw it to caller!
                throw new ScriptError(script, line, detail.getSingle(e));
            }
            Skript.error(detail.getSingle(e));
        } else {
            Skript.warning(detail.getSingle(e));
        }
    }

    @Override
    public String toString(final @Nullable Event e, final boolean debug) {
        return "throw new " + getTypeName() + " because " + detail.toString(e, debug);
    }

    public static final class ScriptError extends RuntimeException {

        /**
         * serialVersionUID
         */
        private static final long serialVersionUID = 1255223120346309260L;

        @Nullable
        private String script;

        @Nullable
        private String detail;

        private int line;

        public ScriptError(final @Nullable String script, final int line, final @Nullable String detail) {
            this(detail);

            this.script = script;
            this.detail = detail;

            this.line = line;
        }

        /**
         *
         */
        public ScriptError() {
            super();
        }

        /**
         * @param message
         * @param cause
         * @param enableSuppression
         * @param writableStackTrace
         */
        public ScriptError(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }

        /**
         * @param message
         * @param cause
         */
        public ScriptError(final String message, final Throwable cause) {
            super(message, cause);
        }

        /**
         * @param message
         */
        public ScriptError(final @Nullable String message) {
            super(message);
        }

        /**
         * @param cause
         */
        public ScriptError(final Throwable cause) {
            super(cause);
        }

        @Nullable
        public String getScript() {
            return script;
        }

        /**
         * Same as {@link ScriptError#getLocalizedMessage()}
         * and {@link ScriptError#getMessage()}
         *
         * @return The details, or the cause of the error.
         */
        @Nullable
        public String getDetail() {
            return detail;
        }

        public int getLine() {
            return line;
        }

    }
}
