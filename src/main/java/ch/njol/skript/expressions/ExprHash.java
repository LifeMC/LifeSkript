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

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author Peter Güttinger
 */
@Name("Hash")
@Description({"Hashes the given text using the MD5 or SHA-256 algorithms. This is useful for storing passwords or IP addresses without having to store them literally.", "Please note that an hashed text is irreversible, i.e. you won't be able to get the original text back (which is the point of storing passwords like this). Brute-force attacks can still be performed on hashes though, which can easily crack short, common or insecure passwords."})
@Examples({"command /setpass <text>:", "	trigger:", "		set {password.%player%} text-argument hashed with SHA-256", "command /login <text>:", "	trigger:", "		{password.%player%} is text-argument hashed with SHA-256:", "			message \"login successful.\"", "		else:", "			message \"wrong password!\""})
@Since("2.0, 2.2.17 (SHA-256 algorithm)")
public final class ExprHash extends PropertyExpression<String, String> {

    @Nullable
    private static final MessageDigest md5;

    @Nullable
    private static final MessageDigest sha256;

    @Nullable
    private MessageDigest algorithm;

    static {
        Skript.registerExpression(ExprHash.class, String.class, ExpressionType.PROPERTY,
                "[md5]( |-)hash(ed|[( |-|)code] of) %strings%",
                "%strings% hash[ed] with (0¦MD5|1¦SHA-256)");
    }

    static {
        try {
            md5 = MessageDigest.getInstance("MD5");
            sha256 = MessageDigest.getInstance("SHA-256");
        } catch (final NoSuchAlgorithmException e) {
            throw new InternalError("JVM does not adhere to Java specifications");
        }
    }

    private static final String toHex(final byte[] b) {
        final char[] r = new char[2 * b.length];
        for (int i = 0; i < b.length; i++) {
            r[2 * i] = Character.forDigit((b[i] & 0xF0) >> 4, 16);
            r[2 * i + 1] = Character.forDigit(b[i] & 0x0F, 16);
        }
        return new String(r);
    }

    @SuppressWarnings({"unchecked", "null"})
    @Override
    public final boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
        if (matchedPattern == 0)
            algorithm = md5;
        else if (matchedPattern == 1) {
            if (parseResult.mark == 0)
                algorithm = md5;
            else if (parseResult.mark == 1)
                algorithm = sha256;
            else
                assert false : parseResult.mark;
        } else
            assert false : matchedPattern;
        if (algorithm == null) {
            Skript.error("The Java Virtual Machine running on this server does not support the requested algorithm, thus you cannot use the 'hash' expression.");
            return false;
        }
        setExpr((Expression<String>) exprs[0]);
        return true;
    }

    @SuppressWarnings("null")
    @Override
    protected final String[] get(final Event e, final String[] source) {
        assert algorithm != null;
        final String[] r = new String[source.length];
        for (int i = 0; i < r.length; i++)
            r[i] = toHex(algorithm.digest(source[i].getBytes(StandardCharsets.UTF_8)));
        return r;
    }

    @Override
    public final String toString(@Nullable final Event e, final boolean debug) {
        return "hash of " + getExpr();
    }

    @Override
    public final Class<String> getReturnType() {
        return String.class;
    }

}
