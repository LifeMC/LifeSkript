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
 *   Copyright (C) 2011 Peter Güttinger and contributors
 *
 */

package ch.njol.skript.update;

import ch.njol.skript.util.Version;
import ch.njol.util.LineSeparators;
import org.junit.jupiter.api.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("static-method")
final class AbstractUpdaterTest {

    private static final Matcher V_2_2_18 = Pattern.compile("2.2.18", Pattern.LITERAL).matcher("");
    private static final Pattern UNIX_NEW_LINE = Pattern.compile(LineSeparators.UNIX, Pattern.LITERAL);

    @Test
    final void testReplaceVersion() {
        final String downloadUrl = "https://github.com/LifeMC/LifeSkript/releases/download/2.2.18/Skript.jar";
        final String exprDownloadUrl = V_2_2_18.reset(downloadUrl).replaceAll(Matcher.quoteReplacement("%version%"));

        assertTrue(exprDownloadUrl.contains("%version%"), exprDownloadUrl);

        assertEquals(downloadUrl,
                AbstractUpdater.replaceVersionInDownloadUrl(() -> new Version("2.2.18"), exprDownloadUrl));
    }

    @Test
    final void testGetVersion() {
        assertEquals(new Version(2, 2, 18), AbstractUpdater.getVersion("version: 2.2.18"));
        assertEquals(new Version(2, 2, 18), AbstractUpdater.getVersion("version=2.2.18"));

        assertEquals(new Version(2, 2, 18), AbstractUpdater.getVersion("2.2.18"));

        final String samplePluginYml = "main: ch.njol.skript.Skript" + LineSeparators.UNIX +
                LineSeparators.UNIX +
                "version: 2.2.18" + LineSeparators.UNIX +
                LineSeparators.UNIX +
                "commands:";

        assertEquals(new Version(2, 2, 18), AbstractUpdater.getVersion(samplePluginYml));

        assertEquals(new Version(2, 2, 18), AbstractUpdater.getVersion(samplePluginYml.replace(LineSeparators.UNIX, LineSeparators.MAC)));
        assertEquals(new Version(2, 2, 18), AbstractUpdater.getVersion(UNIX_NEW_LINE.matcher(samplePluginYml).replaceAll(Matcher.quoteReplacement(LineSeparators.DOS))));
    }

}
