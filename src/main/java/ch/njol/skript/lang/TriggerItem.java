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

package ch.njol.skript.lang;

import ch.njol.skript.Skript;
import ch.njol.util.StringUtils;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import java.io.File;

/**
 * Represents a trigger item, i.e. a trigger section, a condition or an effect.
 *
 * @author Peter Güttinger
 * @see TriggerSection
 * @see Trigger
 * @see Statement
 */
public abstract class TriggerItem implements Debuggable {

    /**
     * how much to indent each level
     */
    private static final String indent = "  ";
    @Nullable
    protected TriggerSection parent;
    @Nullable
    private TriggerItem next;
    @Nullable
    private String indentation;

    protected TriggerItem() {
    }

    protected TriggerItem(final TriggerSection parent) {
        this.parent = parent;
    }

    /**
     * @param start
     * @param e
     * @return false if an exception occurred
     */
    public static final boolean walk(final TriggerItem start, final Event e) {
        assert start != null && e != null;
        TriggerItem i = start;
        try {
            while (i != null)
                i = i.walk(e);
            return true;
        } catch (final StackOverflowError err) {
            if (Skript.debug())
                Skript.exception(err);
            final Trigger t = start.getTrigger();
            final File sc = t == null ? null : t.getScript();
            Skript.adminBroadcast("<red>The script '<gold>" + (sc == null ? "<unknown>" : sc.getName()) + "<red>' infinitely (or excessively) repeated itself!");
            return false;
        } catch (final Throwable tw) {
            Skript.exception(tw, i, "Error when executing trigger in event " + e.getClass().getCanonicalName());
            return false;
        }
    }

    /**
     * Executes this item and returns the next item to run.
     * <p>
     * Overriding classes must call {@link #debug(Event, boolean)}. If this method is overridden, {@link #run(Event)} is not used anymore and can be ignored.
     *
     * @param e
     * @return The next item to run or null to stop execution
     */
    @Nullable
    protected TriggerItem walk(final Event e) {
        if (run(e)) {
            debug(e, true);
            return next;
        }
        debug(e, false);
        final TriggerSection parent = this.parent;
        return parent == null ? null : parent.getNext();
    }

    /**
     * Executes this item.
     *
     * @param e
     * @return True if the next item should be run, or false for the item following this item's parent.
     */
    protected abstract boolean run(Event e);

    public final String getIndentation() {
        String ind = indentation;
        if (ind == null) {
            int level = 0;
            TriggerItem i = this;
            while ((i = i.parent) != null)
                level++;
            indentation = ind = StringUtils.multiply(indent, level);
        }
        return ind;
    }

    protected final void debug(final Event e, final boolean run) {
        if (!Skript.debug())
            return;
        Skript.debug(getIndentation() + (run ? "" : "-") + toString(e, true));
    }

    @Override
    public final String toString() {
        return toString(null, false);
    }

    @Nullable
    public final TriggerSection getParent() {
        return parent;
    }

    public TriggerItem setParent(@Nullable final TriggerSection parent) {
        this.parent = parent;
        return this;
    }

    /**
     * @return The trigger this item belongs to, or null if this is a stand-alone item (e.g. the effect of an effect command)
     */
    @Nullable
    public final Trigger getTrigger() {
        TriggerItem i = this;
        while (i != null && !(i instanceof Trigger))
            i = i.parent;
//		if (i == null)
//			throw new IllegalStateException("TriggerItem without a Trigger detected!");
        return (Trigger) i;
    }

    @Nullable
    public final TriggerItem getNext() {
        return next;
    }

    public TriggerItem setNext(@Nullable final TriggerItem next) {
        this.next = next;
        return this;
    }

}
