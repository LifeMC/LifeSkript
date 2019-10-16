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

package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Checker;
import org.bukkit.Material;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Sheep;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Locale;

/**
 * @author Peter Güttinger
 */
public final class EvtEntityBlockChange extends SkriptEvent {
    static {
        Skript.registerEvent("Enderman/Sheep", EvtEntityBlockChange.class, EntityChangeBlockEvent.class, ChangeEvent.patterns).description("Called when an enderman places or picks up a block, or a sheep eats grass respectively.").examples("").since("");
    }

    @SuppressWarnings("null")
    private ChangeEvent event;

    @SuppressWarnings("null")
    @Override
    public boolean init(final Literal<?>[] args, final int matchedPattern, final ParseResult parser) {
        event = ChangeEvent.values()[matchedPattern];
        return true;
    }

    @Override
    public boolean check(final Event e) {
        if (!(e instanceof EntityChangeBlockEvent))
            return false;
        return event.checker.check((EntityChangeBlockEvent) e);
    }

    @Override
    public String toString(@Nullable final Event e, final boolean debug) {
        return event.name().replace('_', ' ').toLowerCase(Locale.ENGLISH);
    }

    private enum ChangeEvent {
        ENDERMAN_PLACE("enderman place", e -> e.getEntity() instanceof Enderman && e.getTo() != Material.AIR), ENDERMAN_PICKUP("enderman pickup", e -> e.getEntity() instanceof Enderman && e.getTo() == Material.AIR), SHEEP_EAT("sheep eat", e -> e.getEntity() instanceof Sheep);
        // TODO silverfishes

        static final ChangeEvent[] values;
        static final String[] patterns;

        static {
            values = ChangeEvent.values();
            assert values.length > 0;

            patterns = new String[values.length];
            for (int i = 0; i < patterns.length; i++) {
                patterns[i] = values[i].pattern;
            }
        }

        final Checker<EntityChangeBlockEvent> checker;
        private final String pattern;

        ChangeEvent(final String pattern, final Checker<EntityChangeBlockEvent> c) {
            this.pattern = pattern;
            checker = c;
        }
    }

}
