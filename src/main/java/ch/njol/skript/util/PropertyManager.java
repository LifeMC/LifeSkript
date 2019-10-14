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

package ch.njol.skript.util;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.SkriptParser;

/**
 * A utility class for options controlled by the JVM arguments.
 *
 * Supports default values, minimum maximum ranges and gives warnings
 * when an invalid input is found instead of just silently returning the default value.
 *
 * This prevents typos, etc. This the main reason we don't use direct
 * methods like {@link Integer#getInteger(String, int)}, the another reason is that
 * method returns a boxed Integer, which is not a primitive.
 */
public final class PropertyManager {

    private PropertyManager() {
        throw new UnsupportedOperationException("Static class");
    }

    /**
     * Internal method
     */
    private static final void invalidProperty(final String propertyName,
                                              final String propertyValue,
                                              final String defaultValue) {
        Skript.warning("Invalid property value \"" + propertyValue + "\" for property \"" + propertyName + "\". Using default value \"" + defaultValue + "\" instead.");
    }

    /**
     * Defaults to false
     */
    public static final boolean getBoolean(final String propertyName) {
        return getBoolean(propertyName, false);
    }

    public static final boolean getBoolean(final String propertyName,
                                           final boolean defaultValue) {
        final String propertyValue = System.getProperty(propertyName);
        if (propertyValue != null) {
            final Runnable invalidPropertyValue = () -> invalidProperty(propertyName, propertyValue, Boolean.toString(defaultValue));
            final boolean isTrue = "true".equalsIgnoreCase(propertyValue);

            if (isTrue || "false".equalsIgnoreCase(propertyValue))
                return isTrue;

            invalidPropertyValue.run();
        }
        return defaultValue;
    }

    public static final int getInt(final String propertyName,
                                       final int defaultValue) {
        return getInt(propertyName, defaultValue, -1);
    }

    public static final int getInt(final String propertyName,
                                   final int defaultValue,
                                   final int minimumValue) {
        return getInt(propertyName, defaultValue, minimumValue, -1);
    }

    public static final int getInt(final String propertyName,
                                   final int defaultValue,
                                   final int minimumValue,
                                   final int maximumValue) {
        final String propertyValue = System.getProperty(propertyName);
        if (propertyValue != null) {
            final Runnable invalidPropertyValue = () -> invalidProperty(propertyName, propertyValue, Integer.toString(defaultValue));
            if (SkriptParser.isInteger(propertyValue)) {
                final int value;

                try {
                    value = Integer.parseInt(propertyValue);
                } catch (final NumberFormatException e) {
                    invalidPropertyValue.run();
                    return defaultValue;
                }

                if (minimumValue != -1 && value < minimumValue) {
                    Skript.warning("Property value \"" + propertyValue + "\" is lower than the minimum allowed value for property \"" + propertyName + "\". Using minimum value \"" + minimumValue + "\" instead.");
                    return minimumValue;
                }

                if (maximumValue != -1 && value > maximumValue) {
                    Skript.warning("Property value \"" + propertyValue + "\" exceeds maximum allowed range for property \"" + propertyName + "\". Using maximum value \"" + maximumValue + "\" instead.");
                    return maximumValue;
                }

                return value;
            }
            invalidPropertyValue.run();
        }
        return defaultValue;
    }

}
