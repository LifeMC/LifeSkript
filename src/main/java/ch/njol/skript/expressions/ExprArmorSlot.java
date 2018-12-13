/*
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * 
 * Copyright 2011-2014 Peter Güttinger
 * 
 */

package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.EquipmentSlot;
import ch.njol.skript.util.EquipmentSlot.EquipSlot;
import ch.njol.skript.util.Slot;
import ch.njol.util.Kleenean;

import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;

import java.util.Locale;

import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
@Name("Armour Slot")
@Description("A part of a player's armour, i.e. the boots, leggings, chestplate or helmet.")
@Examples({"set chestplate of the player to a diamond chestplate", "helmet of player is neither a helmet nor air # player is wearing a block, e.g. from another plugin"})
@Since("1.0")
public class ExprArmorSlot extends SimplePropertyExpression<LivingEntity, Slot> {
	static {
		register(ExprArmorSlot.class, Slot.class, "(0¦boot[s]|0¦shoe[s]|1¦leg[ging][s]|2¦chestplate[s]|3¦helm[et][s]) [slot]", "livingentities");
	}
	
	@SuppressWarnings("null")
	private EquipSlot slot;
	
	private final static EquipSlot[] slots = {EquipSlot.BOOTS, EquipSlot.LEGGINGS, EquipSlot.CHESTPLATE, EquipSlot.HELMET};
	
	@SuppressWarnings("null")
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		super.init(exprs, matchedPattern, isDelayed, parseResult);
		slot = slots[parseResult.mark];
		return true;
	}
	
	@Override
	@Nullable
	public Slot convert(final LivingEntity e) {
		final EntityEquipment eq = e.getEquipment();
		if (eq == null)
			return null;
		return new EquipmentSlot(eq, slot);
	}
	
	@Override
	protected String getPropertyName() {
		return "" + slot.name().toLowerCase(Locale.ENGLISH);
	}
	
	@Override
	public Class<Slot> getReturnType() {
		return Slot.class;
	}
	
}
