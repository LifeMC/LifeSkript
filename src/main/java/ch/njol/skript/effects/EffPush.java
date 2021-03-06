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
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Direction;
import ch.njol.skript.util.PropertyManager;
import ch.njol.util.Kleenean;
import fr.neatmonster.nocheatplus.hooks.NCPExemptionManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
@Name("Push")
@Description("Push entities around.")
@Examples({"push the player upwards", "push the victim downwards at speed 0.5"})
@Since("1.4.6")
public final class EffPush extends Effect {
    public static final boolean hasNoCheatPlus = Bukkit.getPluginManager().getPlugin("NoCheatPlus") != null
            && Skript.classExists("fr.neatmonster.nocheatplus.hooks.NCPExemptionManager") && !PropertyManager.getBoolean("skript.disableNcpHook");

    public static boolean hookNotified;

    static {
        Skript.registerEffect(EffPush.class, "(push|thrust) %entities% %direction% [(at|with) (speed|velocity|force) %-number%]");
    }

    @SuppressWarnings("null")
    private Expression<Entity> entities;
    @SuppressWarnings("null")
    private Expression<Direction> direction;
    @Nullable
    private Expression<Number> speed;

    @SuppressWarnings({"unchecked", "null"})
    @Override
    public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
        entities = (Expression<Entity>) exprs[0];
        direction = (Expression<Direction>) exprs[1];
        speed = (Expression<Number>) exprs[2];
        return true;
    }

    @Override
    protected void execute(final Event e) {
        final Direction d = direction.getSingle(e);
        if (d == null)
            return;
        final Number v = speed != null ? speed.getSingle(e) : null;
        if (speed != null && v == null)
            return;
        final Entity[] ents = entities.getArray(e);
        for (final Entity en : ents) {
            assert en != null;
            final Vector mod = d.getDirection(en);
            if (v != null)
                mod.normalize().multiply(v.doubleValue());
            final boolean flag = en instanceof Player && hasNoCheatPlus;
            if (flag)
                NCPExemptionManager.exemptPermanently((Player) en);
            en.setVelocity(en.getVelocity().add(mod));
            if (flag)
                NCPExemptionManager.unexempt((Player) en);
        }
    }

    @Override
    public String toString(@Nullable final Event e, final boolean debug) {
        return "push " + entities.toString(e, debug) + ' ' + direction.toString(e, debug) + (speed != null ? " at speed " + speed.toString(e, debug) : "");
    }

}
