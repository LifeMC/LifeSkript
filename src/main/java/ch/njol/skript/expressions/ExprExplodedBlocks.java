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

package ch.njol.skript.expressions;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.EmptyArrays;
import ch.njol.util.Kleenean;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.eclipse.jdt.annotation.Nullable;

import java.util.List;

@Name("Exploded Blocks")
@Description("Get all the blocks that were destroyed in an explode event")
@Examples({"on explode:",
        "\tloop exploded blocks:",
        "\t\tadd loop-block to {exploded::blocks::*}"})
@Events("explode")
@Since("2.2.18")
public final class ExprExplodedBlocks extends SimpleExpression<Block> {

    static {
        Skript.registerExpression(ExprExplodedBlocks.class, Block.class, ExpressionType.COMBINED, ExprExplodedBlocks::new, "[the] exploded blocks");
    }

    @Override
    public final boolean init(final Expression<?>[] expressions, final int matchedPattern, final Kleenean isDelayed, final SkriptParser.ParseResult parseResult) {
        if (!ScriptLoader.isCurrentEvent(EntityExplodeEvent.class)) {
            Skript.error("Exploded blocks can only be retrieved from an explode event.");
            return false;
        }
        return true;
    }

    @Override
    protected final Block[] get(final Event e) {
        final List<Block> blockList = ((EntityExplodeEvent) e).blockList();
        return blockList.toArray(EmptyArrays.EMPTY_BLOCK_ARRAY);
    }

    @Override
    public final boolean isSingle() {
        return false;
    }

    @Override
    public final Class<Block> getReturnType() {
        return Block.class;
    }

    @Override
    public final String toString(@Nullable final Event e, final boolean d) {
        return "exploded blocks";
    }

}
