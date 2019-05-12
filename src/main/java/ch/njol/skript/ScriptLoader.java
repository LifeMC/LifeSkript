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

package ch.njol.skript;

import ch.njol.skript.aliases.Aliases;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.command.CommandEvent;
import ch.njol.skript.command.Commands;
import ch.njol.skript.command.ScriptCommand;
import ch.njol.skript.config.*;
import ch.njol.skript.effects.Delay;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.function.Function;
import ch.njol.skript.lang.function.FunctionEvent;
import ch.njol.skript.lang.function.Functions;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.Message;
import ch.njol.skript.localization.PluralizingArgsMessage;
import ch.njol.skript.log.*;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.Converters;
import ch.njol.skript.util.Date;
import ch.njol.skript.util.ExceptionUtils;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import ch.njol.util.NonNullPair;
import ch.njol.util.StringUtils;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

/**
 * @author Peter Güttinger
 */
public final class ScriptLoader {
    public static final List<TriggerSection> currentSections = new ArrayList<>();
    public static final List<Loop> currentLoops = new ArrayList<>();
    static final HashMap<String, String> currentOptions = new HashMap<>();
    private static final Message m_no_errors = new Message("skript.no errors"),
            m_no_scripts = new Message("skript.no scripts");
    private static final PluralizingArgsMessage m_scripts_loaded = new PluralizingArgsMessage("skript.scripts loaded");
    private static final Map<String, ItemType> currentAliases = new HashMap<>();
    /**
     * must be synchronized
     */
    private static final ScriptInfo loadedScripts = new ScriptInfo();
    /**
     * Filter for enabled scripts & folders.
     */
    @SuppressWarnings("null")
	private static final FileFilter scriptFilter = f -> f != null && (f.isDirectory() && SkriptConfig.allowScriptsFromSubFolders.value() || StringUtils.endsWithIgnoreCase(f.getName().trim(), ".sk".trim()) && !StringUtils.startsWithIgnoreCase(f.getName().trim(), "-".trim()));
    @Nullable
    public static Config currentScript;
    public static Kleenean hasDelayBefore = Kleenean.FALSE;
    /**
     * use {@link #setCurrentEvent(String, Class...)}
     */
    @Nullable
    private static String currentEventName;
    /**
     * use {@link #setCurrentEvent(String, Class...)}
     */
    @Nullable
    private static Class<? extends Event>[] currentEvents;
    private static String indentation = "";

    private ScriptLoader() {
    }

    @Nullable
    public static final String getCurrentEventName() {
        return currentEventName;
    }

    /**
     * Call {@link #deleteCurrentEvent()} after parsing
     *
     * @param name The name of the current event.
     * @param events The current events.
     */
    @SafeVarargs
    public static final void setCurrentEvent(final String name, final @Nullable Class<? extends Event>... events) {
        currentEventName = name;
        currentEvents = events;
        hasDelayBefore = Kleenean.FALSE;
    }

    public static final void deleteCurrentEvent() {
        currentEventName = null;
        currentEvents = null;
        hasDelayBefore = Kleenean.FALSE;
    }

//	private static final class SerializedScript {
//		public SerializedScript() {}
//
//		public final List<Trigger> triggers = new ArrayList<Trigger>();
//		public final List<ScriptCommand> commands = new ArrayList<ScriptCommand>();
//	}

    public static final Map<String, ItemType> getScriptAliases() {
        return currentAliases;
    }

    static final ScriptInfo loadScripts() {
        final File scriptsFolder = new File(Skript.getInstance().getDataFolder(), Skript.SCRIPTSFOLDER + File.separator);
        if (!scriptsFolder.isDirectory())
            //noinspection ResultOfMethodCallIgnored
            scriptsFolder.mkdirs();

        final Date start = new Date();

        final ScriptInfo i;

        final ErrorDescLogHandler h = SkriptLogger.startLogHandler(new ErrorDescLogHandler(null, null, m_no_errors.toString()));
        try {
            Language.setUseLocal(false);

            i = loadScripts(scriptsFolder);

            synchronized (loadedScripts) {
                loadedScripts.add(i);
            }
        } finally {
            Language.setUseLocal(true);
            h.stop();
        }

        if (i.files == 0)
            Skript.warning(m_no_scripts.toString());
        if (Skript.logNormal() && i.files > 0)
            Skript.info(m_scripts_loaded.toString(i.files, i.triggers, i.commands, start.difference(new Date())));

        SkriptEventHandler.registerBukkitEvents();
        Functions.postCheck(); // Check that all functions which are called exist.
        return i;
    }

    /**
     * All loaded script files.
     */
    @SuppressWarnings("null")
    static final Set<File> loadedFiles = Collections.synchronizedSet(new HashSet<>());

    @SuppressWarnings("null") // Collections methods don't return nulls, ever
    public static final Collection<File> getLoadedFiles() {
        return Collections.unmodifiableCollection(loadedFiles);
    }

    /**
     * Loads enabled scripts from the specified directory and it's subdirectories.
     *
     * @param directory The directory to load scripts from
     * @return Info on the loaded scripts
     */
    public static final ScriptInfo loadScripts(final File directory) {
        final ScriptInfo i = new ScriptInfo();
        final boolean wasLocal = Language.setUseLocal(false);
        try {
            final File[] files = directory.listFiles(scriptFilter);
            assert files != null;
            Arrays.sort(files);
            for (final File f : files) {
                if (f.isDirectory()) {
                    i.add(loadScripts(f));
                } else {
                    i.add(loadScript(f));
                }
            }
        } finally {
            if (wasLocal)
                Language.setUseLocal(true);
        }
        Functions.postCheck(); // Check that all functions which are called exist.
        return i;
    }

    /**
     * Loads the specified scripts.
     *
     * @param files The script files to load
     * @return Info on the loaded scripts
     */
    public static final ScriptInfo loadScripts(final File[] files) {
        Arrays.sort(files);
        final ScriptInfo i = new ScriptInfo();
        final boolean wasLocal = Language.setUseLocal(false);
        try {
            for (final File f : files) {
                assert f != null : Arrays.toString(files);
                i.add(loadScript(f));
            }
        } finally {
            if (wasLocal)
                Language.setUseLocal(true);
        }

        synchronized (loadedScripts) {
            loadedScripts.add(i);
        }

        SkriptEventHandler.registerBukkitEvents();
        Functions.postCheck(); // Check that all functions which are called exist.
        return i;
    }

    @SuppressWarnings({"unchecked", "null"})
    public static final ScriptInfo loadScript(final File f) {
        if (f == null) {
            assert false;
            return new ScriptInfo();
        }
//		File cache = null;
//		if (SkriptConfig.enableScriptCaching.value()) {
//			cache = new File(f.getParentFile(), "cache" + File.separator + f.getName() + "c");
//			if (cache.exists()) {
//				final RetainingLogHandler log = SkriptLogger.startRetainingLog();
//				ObjectInputStream in = null;
//				try {
//					in = new ObjectInputStream(new FileInputStream(cache));
//					final long lastModified = in.readLong();
//					if (lastModified == f.lastModified()) {
//						final SerializedScript script = (SerializedScript) in.readObject();
//						triggersLoop: for (final Trigger t : script.triggers) {
//							if (t.getEvent() instanceof SelfRegisteringSkriptEvent) {
//								((SelfRegisteringSkriptEvent) t.getEvent()).register(t);
//								SkriptEventHandler.addSelfRegisteringTrigger(t);
//							} else {
//								for (final SkriptEventInfo<?> e : Skript.getEvents()) {
//									if (e.c == t.getEvent().getClass()) {
//										SkriptEventHandler.addTrigger(e.events, t);
//										continue triggersLoop;
//									}
//								}
//								throw new EmptyStackException();
//							}
//						}
//						for (final ScriptCommand c : script.commands) {
//							Commands.registerCommand(c);
//						}
//						log.printLog();
//						return new ScriptInfo(1, script.triggers.size(), script.commands.size());
//					} else {
//						cache.delete();
//					}
//				} catch (final Exception e) {
//					if (Skript.testing()) {
//						System.err.println("[debug] Error loading cached script '" + f.getName() + "':");
//						e.printStackTrace();
//					}
//					unloadScript(f);
//					if (in != null) {
//						try {
//							in.close();
//						} catch (final IOException e1) {}
//					}
//					cache.delete();
//				} finally {
//					log.stop();
//					if (in != null) {
//						try {
//							in.close();
//						} catch (final IOException e) {}
//					}
//				}
//			}
//		}
        try {

            @Nullable
            Date startDate = null;

            if (Skript.logHigh())
                startDate = new Date();

            final Config config = new Config(f, true, false, ":");

            if (SkriptConfig.keepConfigsLoaded.value())
                SkriptConfig.configs.add(config);

            int numTriggers = 0;
            int numCommands = 0;
            int numFunctions = 0;

            currentAliases.clear();
            currentOptions.clear();
            currentScript = config;

//			final SerializedScript script = new SerializedScript();

            final CountingLogHandler numErrors = SkriptLogger.startLogHandler(new CountingLogHandler(SkriptLogger.SEVERE));

            try {
                for (final Node cnode : config.getMainNode()) {
                    if (!(cnode instanceof SectionNode)) {
                        Skript.error("invalid line - all code has to be put into triggers");
                        continue;
                    }

                    final SectionNode node = (SectionNode) cnode;
                    String event = node.getKey();
                    if (event == null)
                        continue;

                    if ("aliases".equalsIgnoreCase(event)) {
                        node.convertToEntries(0, "=");
                        for (final Node n : node) {
                            if (!(n instanceof EntryNode)) {
                                Skript.error("invalid line in aliases section");
                                continue;
                            }
                            final ItemType t = Aliases.parseAlias(((EntryNode) n).getValue());
                            if (t == null)
                                continue;
                            currentAliases.put(n.getKey().toLowerCase(Locale.ENGLISH), t);
                        }
                        continue;
                    } else if ("options".equalsIgnoreCase(event)) {
                        node.convertToEntries(0);
                        for (final Node n : node) {
                            if (!(n instanceof EntryNode)) {
                                Skript.error("invalid line in options");
                                continue;
                            }
                            currentOptions.put(n.getKey(), ((EntryNode) n).getValue());
                        }
                        continue;
                    } else if ("variables".equalsIgnoreCase(event)) {
                        node.convertToEntries(0, "=");
                        for (final Node n : node) {
                            if (!(n instanceof EntryNode)) {
                                Skript.error("Invalid line in variables section");
                                continue;
                            }
                            @SuppressWarnings("null")
							String name = n.getKey().toLowerCase(Locale.ENGLISH);
                            if (name.startsWith("{") && name.endsWith("}"))
                                name = name.substring(1, name.length() - 1);
                            final String var = name;
                            name = StringUtils.replaceAll(name, "%(.+)?%", m -> {
                                if (m.group(1).contains("{") || m.group(1).contains("}") || m.group(1).contains("%")) {
                                    Skript.error("'" + var + "' is not a valid name for a default variable");
                                    return null;
                                }
                                final ClassInfo<?> ci = Classes.getClassInfoFromUserInput(m.group(1));
                                if (ci == null) {
                                    Skript.error("Can't understand the type '" + m.group(1) + "'");
                                    return null;
                                }
                                return "<" + ci.getCodeName() + ">";
                            });
                            if (name == null) {
                                continue;
                            } else if (name.contains("%")) {
                                Skript.error("Invalid use of percent signs in variable name");
                                continue;
                            }
                            // Variables feature is for setting default variables,
                            // Not a shortcut for setting variables at startup.
                            // So if variable already exists, do nothing and continue.
                            if (Variables.getVariable(name, null, false) != null)
                                continue;
                            Object o;
                            final ParseLogHandler log = SkriptLogger.startParseLogHandler();
                            try {
                                o = Classes.parseSimple(((EntryNode) n).getValue(), Object.class, ParseContext.SCRIPT);
                                if (o == null) {
                                    log.printError("Can't understand the value '" + ((EntryNode) n).getValue() + "'");
                                    continue;
                                }
                                log.printLog();
                            } finally {
                                log.stop();
                            }
                            final ClassInfo<?> ci = Classes.getSuperClassInfo(o.getClass());
                            if (ci == null || ci.getSerializer() == null) {
                                Skript.error("Can't save '" + ((EntryNode) n).getValue() + "' in a variable");
                                continue;
                            } else if (ci.getSerializeAs() != null) {
                                final ClassInfo<?> as = Classes.getExactClassInfo(ci.getSerializeAs());
                                if (as == null) {
                                    assert false : ci;
                                    continue;
                                }
                                o = Converters.convert(o, as.getC());
                                if (o == null) {
                                    Skript.error("Can't save '" + ((EntryNode) n).getValue() + "' in a variable");
                                    continue;
                                }
                            }
                            Variables.setVariable(name, o, null, false);
                        }
                        continue;
                    }

                    if (!SkriptParser.validateLine(event))
                        continue;

                    if (event.toLowerCase(Locale.ENGLISH).startsWith("command ")) {

                        setCurrentEvent("command", CommandEvent.class);

                        final ScriptCommand c = Commands.loadCommand(node);
                        if (c != null) {
                            numCommands++;
//							script.commands.add(c);
                        }

                        deleteCurrentEvent();

                        continue;
                    } else if (event.toLowerCase(Locale.ENGLISH).startsWith("function ") || event.startsWith("func ") || event.startsWith("fun ")) { // Allow kotlin and javascript style function prefixes

                        setCurrentEvent("function", FunctionEvent.class);

                        // Allows to define functions with kotlin style:
                        // fun myFunction():
                        //     broadcast "Yey!"
                        if (!event.toLowerCase(Locale.ENGLISH).startsWith("function ")) {
                            if (event.startsWith("func ")) {
                                node.setKey(event.replaceFirst("func", "function"));
                            } else {
                                node.setKey(event.replaceFirst("fun", "function"));
                            }
                        }

                        final Function<?> func = Functions.loadFunction(node);
                        if (func != null) {
                            numFunctions++;
                        }

                        deleteCurrentEvent();

                        continue;
                    }

                    if (Skript.logVeryHigh() && !Skript.debug())
                        Skript.info("loading trigger '" + event + "'");

                    if (StringUtils.startsWithIgnoreCase(event, "on "))
                        event = event.substring("on ".length());

                    event = replaceOptions(event);

                    final NonNullPair<SkriptEventInfo<?>, SkriptEvent> parsedEvent = SkriptParser.parseEvent(event, "can't understand this event: '" + node.getKey() + "'");
                    if (parsedEvent == null)
                        continue;

                    if (Skript.debug() || node.debug())
                        Skript.debug(event + " (" + parsedEvent.getSecond().toString(null, true) + "):");

                    setCurrentEvent(parsedEvent.getFirst().getName().toLowerCase(Locale.ENGLISH), parsedEvent.getFirst().events);
                    final Trigger trigger;
                    try {
                        trigger = new Trigger(config.getFile(), event, parsedEvent.getSecond(), loadItems(node));
                        trigger.setLineNumber(node.getLine());
                        trigger.setDebugLabel(config.getFileName() + ": line " + node.getLine());
                    } finally {
                        deleteCurrentEvent();
                    }

                    if (parsedEvent.getSecond() instanceof SelfRegisteringSkriptEvent) {
                        ((SelfRegisteringSkriptEvent) parsedEvent.getSecond()).register(trigger);
                        SkriptEventHandler.addSelfRegisteringTrigger(trigger);
                    } else {
                        SkriptEventHandler.addTrigger(parsedEvent.getFirst().events, trigger);
                    }

//					script.triggers.add(trigger);

                    numTriggers++;
                }

                if (Skript.logHigh() && startDate != null) {
                    final long loadTime = TimeUnit.MILLISECONDS.toSeconds(startDate.difference(new Date()).getMilliSeconds());
                    Skript.info("Loaded " + numTriggers + " trigger" + (numTriggers == 1 ? "" : "s") + ", " + numCommands + " command" + (numCommands == 1 ? "" : "s") + " and " + numFunctions + " function" + (numFunctions == 1 ? "" : "s") + " from '" + config.getFileName() + "' in " + loadTime + " seconds.");
                }

                currentScript = null;

            } finally {
                numErrors.stop();
            }

//			if (SkriptConfig.enableScriptCaching.value() && cache != null) {
//				if (numErrors.getCount() > 0) {
//					ObjectOutputStream out = null;
//					try {
//						cache.getParentFile().mkdirs();
//						out = new ObjectOutputStream(new FileOutputStream(cache));
//						out.writeLong(f.lastModified());
//						out.writeObject(script);
//					} catch (final NotSerializableException e) {
//						Skript.exception(e, "Cannot cache " + f.getName());
//						if (out != null)
//							out.close();
//						cache.delete();
//					} catch (final IOException e) {
//						Skript.warning("Cannot cache " + f.getName() + ": " + e.getLocalizedMessage());
//						if (out != null)
//							out.close();
//						cache.delete();
//					} finally {
//						if (out != null)
//							out.close();
//					}
//				}
//			}

            loadedFiles.add(f);
            return new ScriptInfo(1, numTriggers, numCommands, numFunctions);
        } catch (final IOException e) {
            Skript.error("Could not load " + f.getName() + ": " + ExceptionUtils.toString(e));
        } catch (final Throwable tw) {
            Skript.exception(tw, "Could not load " + f.getName());
        } finally {
            SkriptLogger.setNode(null);
        }
        if (Skript.testing() || Skript.debug())
            Skript.warning("Returning empty script info after loading \"" + f.getName() + "\"");
        return new ScriptInfo();
    }

    /**
     * Unloads enabled scripts from the specified directory and its subdirectories.
     *
     * @param folder The folder to unload scripts from
     * @return Info on the unloaded scripts
     */
    static final ScriptInfo unloadScripts(final File folder) {
        final ScriptInfo r = unloadScripts_(folder);
        Functions.validateFunctions();
        return r;
    }

    private static final ScriptInfo unloadScripts_(final File folder) {
        final ScriptInfo info = new ScriptInfo();
        final File[] files = folder.listFiles(scriptFilter);
        assert files != null;
        for (final File f : files) {
            if (f.isDirectory()) {
                info.add(unloadScripts_(f));
            } else if (f.getName().endsWith(".sk")) {
                info.add(unloadScript_(f));
            }
        }
        return info;
    }

    /**
     * Unloads the specified script.
     *
     * @param script The script file to unload
     * @return Info on the unloaded script
     */
    static final ScriptInfo unloadScript(final File script) {
        final ScriptInfo r = unloadScript_(script);
        Functions.validateFunctions();
        return r;
    }

    private static final ScriptInfo unloadScript_(final File script) {
        final ScriptInfo info = SkriptEventHandler.removeTriggers(script);
        synchronized (loadedScripts) {
            loadedScripts.subtract(info);
        }
        loadedFiles.remove(script);
        return info;
    }

    /**
     * Replaces options in a string. May return null, but only if the input is null.
     *
     * @param s The string to replace options.
     * @return The replaced string. May return null, but only if the input is null.
     */
    public static final String replaceOptions(final String s) {
        final String r = StringUtils.replaceAll(s, "\\{@(.+?)\\}", m -> {
            final String option = currentOptions.get(m.group(1));
            if (option == null) {
                Skript.error("undefined option " + m.group());
                return m.group();
            }
            return Matcher.quoteReplacement(option);
        });
        assert r != null;
        return r;
    }

    @SuppressWarnings({"unchecked", "null"})
    public static final List<TriggerItem> loadItems(final SectionNode node) {

        if (Skript.debug())
            indentation += "    ";

        final List<TriggerItem> items = new ArrayList<>();

        Kleenean hadDelayBeforeLastIf = Kleenean.FALSE;

        for (final Node n : node) {
            SkriptLogger.setNode(n);
            if (n instanceof SimpleNode) {
                final SimpleNode e = (SimpleNode) n;
                @SuppressWarnings("null")
				final String s = replaceOptions(e.getKey());
                if (!SkriptParser.validateLine(s))
                    continue;
                final Statement stmt = Statement.parse(s, "Can't understand this condition/effect: " + s);
                if (stmt == null)
                    continue;
                if (Skript.debug() || n.debug())
                    Skript.debug(indentation + stmt.toString(null, true));
                items.add(stmt);
                if (stmt instanceof Delay)
                    hasDelayBefore = Kleenean.TRUE;
            } else if (n instanceof SectionNode) {
                @SuppressWarnings("null")
				String name = replaceOptions(n.getKey());
                if (!SkriptParser.validateLine(name))
                    continue;

                if (StringUtils.startsWithIgnoreCase(name, "loop ")) {
                    final String l = name.substring("loop ".length());
                    final RetainingLogHandler h = SkriptLogger.startRetainingLog();
                    Expression<?> loopedExpr;
                    try {
                        loopedExpr = new SkriptParser(l).parseExpression(Object.class);
                        if (loopedExpr != null)
                            loopedExpr = loopedExpr.getConvertedExpression(Object.class);
                        if (loopedExpr == null) {
                            h.printErrors("Can't understand this loop: '" + name + "'");
                            continue;
                        }
                        h.printLog();
                    } finally {
                        h.stop();
                    }
                    if (loopedExpr.isSingle()) {
                        Skript.error("Can't loop " + loopedExpr + " because it's only a single value");
                        continue;
                    }
                    if (Skript.debug() || n.debug())
                        Skript.debug(indentation + "loop " + loopedExpr.toString(null, true) + ":");
                    final Kleenean hadDelayBefore = hasDelayBefore;
                    items.add(new Loop(loopedExpr, (SectionNode) n));
                    if (hadDelayBefore != Kleenean.TRUE && hasDelayBefore != Kleenean.FALSE)
                        hasDelayBefore = Kleenean.UNKNOWN;
                } else if (StringUtils.startsWithIgnoreCase(name, "while ")) {
                    final String l = name.substring("while ".length());
                    final Condition c = Condition.parse(l, "Can't understand this condition: " + l);
                    if (c == null)
                        continue;
                    if (Skript.debug() || n.debug())
                        Skript.debug(indentation + "while " + c.toString(null, true) + ":");
                    final Kleenean hadDelayBefore = hasDelayBefore;
                    items.add(new While(c, (SectionNode) n));
                    if (hadDelayBefore != Kleenean.TRUE && hasDelayBefore != Kleenean.FALSE)
                        hasDelayBefore = Kleenean.UNKNOWN;
                } else if ("else".equalsIgnoreCase(name)) {
                    if (items.isEmpty() || !(items.get(items.size() - 1) instanceof Conditional) || ((Conditional) items.get(items.size() - 1)).hasElseClause()) {
                        Skript.error("'else' has to be placed just after an 'if' or 'else if' section");
                        continue;
                    }
                    if (Skript.debug() || n.debug())
                        Skript.debug(indentation + "else:");
                    final Kleenean hadDelayAfterLastIf = hasDelayBefore;
                    hasDelayBefore = hadDelayBeforeLastIf;
                    ((Conditional) items.get(items.size() - 1)).loadElseClause((SectionNode) n);
                    hasDelayBefore = hadDelayBeforeLastIf.or(hadDelayAfterLastIf.and(hasDelayBefore));
                } else if (StringUtils.startsWithIgnoreCase(name, "else if ")) {
                    if (items.isEmpty() || !(items.get(items.size() - 1) instanceof Conditional) || ((Conditional) items.get(items.size() - 1)).hasElseClause()) {
                        Skript.error("'else if' has to be placed just after another 'if' or 'else if' section");
                        continue;
                    }
                    name = name.substring("else if ".length());
                    final Condition cond = Condition.parse(name, "can't understand this condition: '" + name + "'");
                    if (cond == null)
                        continue;
                    if (Skript.debug() || n.debug())
                        Skript.debug(indentation + "else if " + cond.toString(null, true));
                    final Kleenean hadDelayAfterLastIf = hasDelayBefore;
                    hasDelayBefore = hadDelayBeforeLastIf;
                    ((Conditional) items.get(items.size() - 1)).loadElseIf(cond, (SectionNode) n);
                    hasDelayBefore = hadDelayBeforeLastIf.or(hadDelayAfterLastIf.and(hasDelayBefore.and(Kleenean.UNKNOWN)));
                } else {
                    if (StringUtils.startsWithIgnoreCase(name, "if "))
                        name = name.substring(3);
                    final Condition cond = Condition.parse(name, "can't understand this condition: '" + name + "'");
                    if (cond == null)
                        continue;
                    if (Skript.debug() || n.debug())
                        Skript.debug(indentation + cond.toString(null, true) + ":");
                    final Kleenean hadDelayBefore = hasDelayBefore;
                    hadDelayBeforeLastIf = hadDelayBefore;
                    items.add(new Conditional(cond, (SectionNode) n));
                    hasDelayBefore = hadDelayBefore.or(hasDelayBefore.and(Kleenean.UNKNOWN));
                }
            }
        }

        for (int i = 0; i < items.size() - 1; i++)
            items.get(i).setNext(items.get(i + 1));

        SkriptLogger.setNode(node);

        if (Skript.debug())
            indentation = indentation.substring(0, indentation.length() - 4);

        return items;
    }

    /**
     * For unit testing
     *
     * @param node The node to load trigger from
     * @return The loaded Trigger
     */
    @SuppressWarnings("null")
	@Nullable
    static final Trigger loadTrigger(final SectionNode node) {
        String event = node.getKey();
        if (event == null) {
            assert false : node;
            return null;
        }
        if (event.toLowerCase(Locale.ENGLISH).startsWith("on "))
            event = event.substring("on ".length());

        final NonNullPair<SkriptEventInfo<?>, SkriptEvent> parsedEvent = SkriptParser.parseEvent(event, "can't understand this event: '" + node.getKey() + "'");
        if (parsedEvent == null) {
            assert false;
            return null;
        }

        setCurrentEvent("unit test", parsedEvent.getFirst().events);
        try {
            return new Trigger(null, event, parsedEvent.getSecond(), loadItems(node));
        } finally {
            deleteCurrentEvent();
        }
    }

    public static final int loadedScripts() {
        synchronized (loadedScripts) {
            return loadedScripts.files;
        }
    }

    public static final int loadedCommands() {
        synchronized (loadedScripts) {
            return loadedScripts.commands;
        }
    }

    public static final int loadedFunctions() {
        synchronized (loadedScripts) {
            return loadedScripts.functions;
        }
    }

    public static final int loadedTriggers() {
        synchronized (loadedScripts) {
            return loadedScripts.triggers;
        }
    }

    public static final boolean isCurrentEvent(final @Nullable Class<? extends Event> event) {
        return CollectionUtils.containsSuperclass(currentEvents, event);
    }

    @SafeVarargs
    public static final boolean isCurrentEvent(final Class<? extends Event>... events) {
        return CollectionUtils.containsAnySuperclass(currentEvents, events);
    }

    /**
     * Use this sparingly; {@link #isCurrentEvent(Class)} or {@link #isCurrentEvent(Class...)} should be used in most cases.
     */
    @Nullable
    public static final Class<? extends Event>[] getCurrentEvents() {
        return currentEvents;
    }

    public static final class ScriptInfo {
        public int files, triggers, commands, functions;

        public ScriptInfo() {
        }

        public ScriptInfo(final int numFiles, final int numTriggers, final int numCommands, final int numFunctions) {
            files = numFiles;
            triggers = numTriggers;
            commands = numCommands;
            functions = numFunctions;
        }

        public void add(final ScriptInfo other) {
            files += other.files;
            triggers += other.triggers;
            commands += other.commands;
            functions += other.functions;
        }

        public void subtract(final ScriptInfo other) {
            files -= other.files;
            triggers -= other.triggers;
            commands -= other.commands;
            functions -= other.functions;
        }
    }

}
