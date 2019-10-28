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

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.LitConsole;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("Print")
@Description({"Prints a text or list of texts to the server console.", "This only a shortcut for explicitly sending the given text(s) to the console using the send effect, and should be used instead as it is more idiomatic and easier to write."})
@Examples("print \"hello world!\"")
@Since("2.2.18")
public final class EffPrint extends Effect {

    static {
        Skript.registerEffect(EffPrint.class, EffPrint::new, "(print|echo) %strings%[ to[ the ][ server ]console]");
    }

    private final Effect sender = new EffMessage();
    private final Expression<ConsoleCommandSender> console = new LitConsole();

    @SuppressWarnings("null")
    private Expression<String> strings;

    @Override
    protected final void execute(final Event e) {
        sender.run(e);
    }

    @Override
    public final String toString(@Nullable final Event e, final boolean debug) {
        return "print " + strings.toString(e, debug);
    }

    @Override
    public final boolean init(final Expression<?>[] expressions, final int matchedPattern, final Kleenean isDelayed, final SkriptParser.ParseResult parseResult) {
        strings = (Expression<String>) expressions[0];
        if (!console.init(expressions, matchedPattern, isDelayed, parseResult))
            return false;
        return sender.init(new Expression<?>[] {strings, console}, matchedPattern, isDelayed, parseResult);
    }

}
