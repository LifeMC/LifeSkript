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

package ch.njol.skript.events;

import org.bukkit.event.block.BlockCanBuildEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.CreeperPowerEvent;
import org.bukkit.event.entity.EntityBreakDoorEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PigZapEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkPopulateEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.event.world.SpawnChangeEvent;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.event.world.WorldUnloadEvent;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.util.SimpleEvent;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("unchecked")
public class SimpleEvents {
	static {
		Skript.registerEvent("Can Build Check", SimpleEvent.class, BlockCanBuildEvent.class, "[block] can build check")
				.description("Called when a player rightclicks on a block while holding a block or a placeable item. You can either cancel the event to prevent the block from being built, or uncancel it to allow it.",
						"Please note that the <a href='../expressions/#ExprDurability'>data value</a> of the block to be placed is not available in this event, only its <a href='../expressions/#ExprIdOf'>ID</a>.")
				.examples("")
				.since("1.0 (basic), 2.0 ([un]cancellable)");
		Skript.registerEvent("Block Damage", SimpleEvent.class, BlockDamageEvent.class, "block damage")
				.description("Called when a player starts to break a block. You can usually just use the leftclick event for this.")
				.examples("")
				.since("1.0");
		Skript.registerEvent("Flow", SimpleEvent.class, BlockFromToEvent.class, "[block] flow[ing]", "block mov(e|ing)")
				.description("Called when a blocks flows or teleports to another block. This not only applies to water and lava, but teleporting dragon eggs as well.")
				.examples("")
				.since("1.0");
		Skript.registerEvent("Ignition", SimpleEvent.class, BlockIgniteEvent.class, "[block] ignit(e|ion)")
				.description("Called when a block starts burning, i.e. a fire block is placed next to it and this block is flammable.",
						"The <a href='#burn'>burn event</a> will be called when the block is about do be destroyed by the fire.")
				.examples("")
				.since("1.0");
		Skript.registerEvent("Physics", SimpleEvent.class, BlockPhysicsEvent.class, "[block] physics")
				.description("Called when a physics check is done on a block. By cancelling this event you can prevent some things from happening, " +
						"e.g. sand falling, dirt turning into grass, torches dropping if their supporting block is destroyed, etc." +
						"Please note that using this event might cause quite some lag since it gets called extremely often.")
				.examples("# prevents sand from falling",
						"on block physics:",
						"	block is sand",
						"	cancel event")
				.since("1.4.6");
		Skript.registerEvent("Piston Extend", SimpleEvent.class, BlockPistonExtendEvent.class, "piston extend[ing]")
				.description("Called when a piston is about to extend.")
				.examples("")
				.since("1.0");
		Skript.registerEvent("Piston Retract", SimpleEvent.class, BlockPistonRetractEvent.class, "piston retract[ing]")
				.description("Called when a piston is about to retract.")
				.examples("")
				.since("1.0");
		Skript.registerEvent("Redstone", SimpleEvent.class, BlockRedstoneEvent.class, "redstone [current] [chang(e|ing)]")
				.description("Called when the redstone current of a block changes. This event is of not much use yet.")
				.examples("")
				.since("1.0");
		Skript.registerEvent("Spread", SimpleEvent.class, BlockSpreadEvent.class, "spread[ing]")
				.description("Called when a new block <a href='#form'>forms</a> as a result of a block that can spread, e.g. water or mushrooms.")
				.examples("")
				.since("1.0");
		Skript.registerEvent("Chunk Load", SimpleEvent.class, ChunkLoadEvent.class, "chunk load[ing]")
				.description("Called when a chunk loads. The chunk might or might not contain mobs when it's loaded.")
				.examples("")
				.since("1.0");
		Skript.registerEvent("Chunk Generate", SimpleEvent.class, ChunkPopulateEvent.class, "chunk (generat|populat)(e|ing)")
				.description("Called after a new chunk was generated.")
				.examples("")
				.since("1.0");
		Skript.registerEvent("Chunk Unload", SimpleEvent.class, ChunkUnloadEvent.class, "chunk unload[ing]")
				.description("Called when a chunk is unloaded due to not being near any player. Cancel the event to force the server to keep the chunk loaded " +
						"and thus keep simulating the chunk (e.g. physics, plant growth, minecarts, etc. will keep working and won't freeze).")
				.examples("")
				.since("1.0");
		Skript.registerEvent("Creeper Power", SimpleEvent.class, CreeperPowerEvent.class, "creeper power")
				.description("Called when a creeper is struck by lighting and gets powered. Cancel the event to prevent the creeper from being powered.")
				.examples("")
				.since("1.0");
		Skript.registerEvent("Zombie Break Door", SimpleEvent.class, EntityBreakDoorEvent.class, "zombie break[ing] [a] [wood[en]] door")
				.description("Called when a zombie is done breaking a wooden door. Can be cancelled to prevent the zombie from breaking the door.")
				.examples("")
				.since("");
		Skript.registerEvent("Combust", SimpleEvent.class, EntityCombustEvent.class, "combust[ing]")
				.description("Called when an entity is set on fire, e.g. by fire or lava, a fireball, or by standing in direct sunlight (zombies, skeletons).")
				.examples("")
				.since("1.0");
		Skript.registerEvent("Explode", SimpleEvent.class, EntityExplodeEvent.class, "explo(d(e|ing)|sion)")
				.description("Called when an entity (a primed TNT or a creeper) explodes.")
				.examples("")
				.since("1.0");
//		Skript.registerEvent(SimpleEvent.class, EntityInteractEvent.class, "interact");// = entity interacts with block, e.g. endermen?; player -> PlayerInteractEvent // likely tripwires, pressure plates, etc.
		Skript.registerEvent("Portal Enter", SimpleEvent.class, EntityPortalEnterEvent.class, "portal enter", "entering [a] portal")
				.description("Called when a player enters a nether portal and the swirly animation starts to play.")
				.examples("")
				.since("1.0");
		Skript.registerEvent("Heal", SimpleEvent.class, EntityRegainHealthEvent.class, "heal[ing]")
				.description("Called when an entity is healed, e.g. by eating (players), being fed (pets), or by the effect of a potion of healing (overworld mobs) or harm (nether mobs).")
				.examples("")
				.since("1.0");
		Skript.registerEvent("Tame", SimpleEvent.class, EntityTameEvent.class, "[entity] tam(e|ing)")
				.description("Called when a player tames a wolf or ocelot. Can be cancelled to prevent the entity from being tamed.")
				.examples("")
				.since("1.0");
		Skript.registerEvent("Explosion Prime", SimpleEvent.class, ExplosionPrimeEvent.class, "explosion prime")
				.description("Called when an explosive is primed, i.e. an entity will explode shortly. Creepers can abort the explosion if the player gets too far away, " +
						"while TNT will explode for sure after a short time.")
				.examples("")
				.since("1.0");
		Skript.registerEvent("Hunger Meter Change", SimpleEvent.class, FoodLevelChangeEvent.class, "(food|hunger) (level|met(er|re)|bar) chang(e|ing)")
				.description("Called when the hunger bar of a player changes, i.e. either increases by eating or decreases over time.")
				.examples("")
				.since("1.4.4");
		Skript.registerEvent("Fuel Burn", SimpleEvent.class, FurnaceBurnEvent.class, "fuel burn[ing]")
				.description("Called when a furnace burns an item from its <a href='../expressions/#ExprFurnaceSlot'>fuel slot</a>.")
				.examples("")
				.since("1.0");
		Skript.registerEvent("Smelt", SimpleEvent.class, FurnaceSmeltEvent.class, "[ore] smelt[ing]", "smelt[ing] of ore")//"smelt[ing] of %itemtype%")
		.description("Called when a furnace smelts an item in its <a href='../expressions/#ExprFurnaceSlot'>ore slot</a>.")
				.examples("")
				.since("1.0");
		Skript.registerEvent("Leaves Decay", SimpleEvent.class, LeavesDecayEvent.class, "leaves decay[ing]")
				.description("Called when a leaf block decays due to not being connected to a tree.")
				.examples("")
				.since("1.0");
		Skript.registerEvent("Lightning Strike", SimpleEvent.class, LightningStrikeEvent.class, "lightning [strike]")
				.description("Called when lightning strikes.")
				.examples("")
				.since("1.0");
		Skript.registerEvent("Pig Zap", SimpleEvent.class, PigZapEvent.class, "pig[ ]zap")
				.description("Called when a pig is stroke by lightning and transformed into a zombie pigman. Cancel the event to prevent the transformation.")
				.examples("")
				.since("1.0");
		Skript.registerEvent("Bed Enter", SimpleEvent.class, PlayerBedEnterEvent.class, "bed enter[ing]", "[player] enter[ing] [a] bed")
				.description("Called when a player starts sleeping.")
				.examples("")
				.since("1.0");
		Skript.registerEvent("Bed Leave", SimpleEvent.class, PlayerBedLeaveEvent.class, "bed leav(e|ing)", "[player] leav(e|ing) [a] bed")
				.description("Called when a player leaves a bed.")
				.examples("")
				.since("1.0");
		Skript.registerEvent("Bucket Empty", SimpleEvent.class, PlayerBucketEmptyEvent.class, "bucket empty[ing]", "[player] empty[ing] [a] bucket")//, "emptying bucket [of %itemtype%]", "emptying %itemtype% bucket") -> place of water/lava)
		.description("Called when a player empties a bucket. You can also use the <a href='#place'>place event</a> with a check for water or lava.")
				.examples("")
				.since("1.0");
		Skript.registerEvent("Bucket fill", SimpleEvent.class, PlayerBucketFillEvent.class, "bucket fill[ing]", "[player] fill[ing] [a] bucket")//, "filling bucket [(with|of) %itemtype%]", "filling %itemtype% bucket");)
		.description("Called when a player fills a bucket.")
				.examples("")
				.since("1.0");
		Skript.registerEvent("Throwing of an Egg", SimpleEvent.class, PlayerEggThrowEvent.class, "throw[ing] [of] [an] egg", "[player] egg throw")
				.description("Called when a player throws an egg. You can just use the <a href='#shoot'>shoot event</a> in most cases, " +
						"as this event is intended to support changing the hatched mob and its chance to hatch, but Skript does not yet support that.")
				.examples("")
				.since("1.0");
		// TODO improve - on fish [of %entitydata%] (and/or itemtype), on reel, etc.
		Skript.registerEvent("Fishing", SimpleEvent.class, PlayerFishEvent.class, "[player] fish[ing]")
				.description("Called when a player fishes something. This is not of much use yet.")
				.examples("")
				.since("1.0");
		if (Skript.classExists("org.bukkit.event.player.PlayerItemBreakEvent")) {
			Skript.registerEvent("Item Break", SimpleEvent.class, PlayerItemBreakEvent.class, "[player] tool break[ing]", "[player] break[ing] (a|the|) tool")
					.description("Called when a player breaks his tool because its damage reached the maximum value.",
							"This event cannot be cancelled.")
					.examples("")
					.since("2.1.1");
		}
		Skript.registerEvent("Tool Change", SimpleEvent.class, PlayerItemHeldEvent.class, "[player['s]] (tool|item held|held item) chang(e|ing)")
				.description("Called whenever a player changes his held item by selecting a different slot (e.g. the keys 1-9 or the mouse wheel), <i>not</i> by dropping or replacing the item in the current slot.")
				.examples("")
				.since("1.0");
		Skript.registerEvent("Join", SimpleEvent.class, PlayerJoinEvent.class, "[player] (login|logging in|join[ing])")
				.description("Called when the player joins the server. The player is already in a world when this event is called, so if you want to prevent players from joining you should prefer <a href='#connect'>on connect</a> over this event.")
				.examples("on join:",
						"	message \"Welcome on our awesome server!\"",
						"	broadcast \"%player% just joined the server!\"")
				.since("1.0");
		Skript.registerEvent("Connect", SimpleEvent.class, PlayerLoginEvent.class, "[player] connect[ing]")
				.description("Called when the player connects to the server. This event is called before the player actually joins the server, so if you want to prevent players from joining you should prefer this event over <a href='#join'>on join</a>.")
				.examples("on connect:",
						"	player doesn't have permission \"VIP\"",
						"	number of players is larger than 20," +
								"	kick the player due to \"The last 5 slots are reserved for VIP players.\"")
				.since("2.0");
		Skript.registerEvent("Kick", SimpleEvent.class, PlayerKickEvent.class, "[player] (kick|being kicked)")
				.description("Called when a player is kicked from the server. You can change the <a href='../expressions/#ExprMessage'>kick message</a> or <a href='../effects/#EffCancelEvent'>cancel the event</a> entirely.")
				.examples("")
				.since("1.0");
		// TODO level up/down
		Skript.registerEvent("Level Change", SimpleEvent.class, PlayerLevelChangeEvent.class, "[player] level [change]")
				.description("Called when a player's <a href='../expressions/#ExprLevel'>level</a> changes, e.g. by gathering experience or by enchanting something.")
				.examples("")
				.since("");
		Skript.registerEvent("Portal", SimpleEvent.class, PlayerPortalEvent.class, "[player] portal")
				.description("Called when a player uses a nether or end portal. <a href='../effects/#EffCancelEvent'>Cancel the event</a> to prevent the player from teleporting.")
				.examples("")
				.since("1.0");
		Skript.registerEvent("Quit", SimpleEvent.class, new Class[] {PlayerQuitEvent.class, PlayerKickEvent.class}, "(quit[ting]|disconnect[ing]|log[ ]out|logging out)")
				.description("Called when a player leaves the server. Starting with Skript 2.0 this also includes kicked players.")
				.examples("")
				.since("1.0");
		Skript.registerEvent("Respawn", SimpleEvent.class, PlayerRespawnEvent.class, "[player] respawn[ing]")
				.description("Called when a player respawns. You should prefer this event over the <a href='#death'>death event</a> as the player is technically alive when this event is called.")
				.examples("")
				.since("1.0");
		Skript.registerEvent("Teleport", SimpleEvent.class, PlayerTeleportEvent.class, "[player] teleport[ing]")
				.description("Called whenever a player is teleported, either by a nether/end portal or other means (e.g. by plugins).")
				.examples("")
				.since("1.0");
		Skript.registerEvent("Sneak Toggle", SimpleEvent.class, PlayerToggleSneakEvent.class, "[player] toggl(e|ing) sneak", "[player] sneak toggl(e|ing)")
				.description("Called when a player starts or stops sneaking. Use <a href='../conditions/#CondIsSneaking'>is sneaking</a> to get whether the player was sneaking before the event was called.")
				.examples("# make players that stop sneaking jump",
						"on sneak toggle:",
						"	player was sneaking",
						"	push the player upwards at speed 0.5")
				.since("1.0");
		Skript.registerEvent("Sprint Toggle", SimpleEvent.class, PlayerToggleSprintEvent.class, "[player] toggl(e|ing) sprint", "[player] sprint toggl(e|ing)")
				.description("Called when a player starts or stops sprinting. Use <a href='../conditions/#CondIsSprinting'>is sprinting</a> to get whether the player was sprinting before the event was called.")
				.examples("")
				.since("");
		Skript.registerEvent("Portal Create", SimpleEvent.class, PortalCreateEvent.class, "portal create")
				.description("Called when a portal is created, either by a player or mob lighting an obsidian frame on fire, or by a nether portal creating its teleportation target in the nether/overworld.",
						"Please note that it's not possible to use <a href='../expressions/#ExprEntity'>the player</a> in this event.")
				.examples("")
				.since("1.0");
		Skript.registerEvent("Projectile Hit", SimpleEvent.class, ProjectileHitEvent.class, "projectile hit")
				.description("Called when a projectile hits an entity or a block.",
						"Use the <a href='#damage'>damage event</a> with a <a href='../conditions/#CondIsSet'>check</a> for a <a href='../expressions/#ExprEntity'>projectile</a> " +
								"to be able to use the <a href='../expressions/#ExprAttacked'>entity that got hit</a> in the case when the projectile hit a living entity.",
						"A damage event will even be fired if the damage is 0, e.g. when throwing snowballs at non-nether mobs.")
				.examples("")
				.since("1.0");
		Skript.registerEvent("Shoot", SimpleEvent.class, ProjectileLaunchEvent.class, "[projectile] shoot")
				.description("Called whenever a <a href='../classes/#projectile'>projectile</a> is shot. Use the <a href='../expressions/#ExprShooter'>shooter expression</a> to get who shot the projectile.")
				.examples("")
				.since("1.0");
		Skript.registerEvent("Sign Change", SimpleEvent.class, SignChangeEvent.class, "sign (chang[e]|edit)[ing]", "[player] (chang[e]|edit)[ing] [a] sign")
				.description("As signs are placed empty, this event is called when a player is done editing a sign.")
				.examples("on sign change:",
						"	line 2 is empty",
						"	set line 1 to \"<red>%line 1%\"")
				.since("1.0");
		Skript.registerEvent("Spawn Change", SimpleEvent.class, SpawnChangeEvent.class, "[world] spawn change")
				.description("Called when the spawn point of a world changes.")
				.examples("")
				.since("1.0");
		Skript.registerEvent("Vehicle Create", SimpleEvent.class, VehicleCreateEvent.class, "vehicle create", "creat(e|ing|ion of) [a] vehicle")
				.description("Called when a new vehicle is created, e.g. when a player places a boat or minecart.")
				.examples("")
				.since("1.0");
		Skript.registerEvent("Vehicle Damage", SimpleEvent.class, VehicleDamageEvent.class, "vehicle damage", "damag(e|ing) [a] vehicle")
				.description("Called when a vehicle gets damage. Too much damage will <a href='#vehicle_destroy'>destroy</a> the vehicle.")
				.examples("")
				.since("1.0");
		Skript.registerEvent("Vehicle Destroy", SimpleEvent.class, VehicleDestroyEvent.class, "vehicle destroy", "destr(oy[ing]|uction of) [a] vehicle")
				.description("Called when a vehicle is destroyed. Any <a href='../expressions/#ExprPassenger'>passenger</a> will be ejected and the vehicle might drop some item(s).")
				.examples("")
				.since("1.0");
		Skript.registerEvent("Vehicle Enter", SimpleEvent.class, VehicleEnterEvent.class, "vehicle enter", "enter[ing] [a] vehicle")
				.description("Called when an <a href='../classes/#entity'>entity</a> enters a vehicle, either deliberately (players) or by falling into them (mobs).")
				.examples("")
				.since("1.0");
		Skript.registerEvent("Vehicle Exit", SimpleEvent.class, VehicleExitEvent.class, "vehicle exit", "exit[ing] [a] vehicle")
				.description("Called when an entity exits a vehicle.")
				.examples("")
				.since("1.0");
		Skript.registerEvent("World Init", SimpleEvent.class, WorldInitEvent.class, "world init")
				.description("Called when a world is initialised. As all default worlds are initialised before any scripts are loaded, this event is only called for newly created worlds.",
						"World management plugins might change the behaviour of this event though.")
				.examples("")
				.since("1.0");
		Skript.registerEvent("World Load", SimpleEvent.class, WorldLoadEvent.class, "world load[ing]")
				.description("Called when a world is loaded. As with the world init event, this event will not be called for the server's default world(s).")
				.examples("")
				.since("1.0");
		Skript.registerEvent("World Save", SimpleEvent.class, WorldSaveEvent.class, "world sav(e|ing)")
				.description("Called when a world is saved to disk. Usually all worlds are saved simultaneously, but world management plugins could change this.")
				.examples("")
				.since("1.0");
		Skript.registerEvent("World Unload", SimpleEvent.class, WorldUnloadEvent.class, "world unload[ing]")
				.description("Called when a world is unloaded. This event might never be called if you don't have a world management plugin.")
				.examples("")
				.since("1.0");
		
	}
}
