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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

@SuppressWarnings("static-method")
final class ReleaseChannelTest {

    @Test
    final void testGeneral() {
        assertSame(ReleaseChannel.STABLE, ReleaseChannel.mostStable());
        assertSame(ReleaseChannel.NIGHTLY, ReleaseChannel.mostUnstable());

        assertSame(ReleaseChannel.PREVIEW, ReleaseChannel.getByExactName("PREVIEW"));
    }

    @Test
    final void testUpDown() {
        assertSame(ReleaseChannel.STABLE, ReleaseChannel.PREVIEW.up());
        assertSame(ReleaseChannel.PREVIEW, ReleaseChannel.STABLE.down());

        assertSame(ReleaseChannel.STABLE, ReleaseChannel.STABLE.up());
        assertSame(ReleaseChannel.NIGHTLY, ReleaseChannel.NIGHTLY.down());
    }

    @Test
    final void testGetBy() {
        assertSame(ReleaseChannel.DEV, ReleaseChannel.getByStartsWith("dev"));
        assertSame(ReleaseChannel.DEV, ReleaseChannel.getByEndsWith("2.2-dev"));

        assertSame(ReleaseChannel.PREVIEW, ReleaseChannel.getByContains("2.3-preview-test.1"));

        assertSame(ReleaseChannel.DEV, ReleaseChannel.getByPrefix("dev-1"));
        assertSame(ReleaseChannel.PREVIEW, ReleaseChannel.getBySuffix("2.2.16-pre1"));
    }

    @Test
    final void testParse() {
        assertSame(ReleaseChannel.STABLE, ReleaseChannel.parse("2.2.18"));
        assertSame(ReleaseChannel.BETA, ReleaseChannel.parse("2.2.16-beta1"));

        assertSame(ReleaseChannel.PREVIEW, ReleaseChannel.parse("2.2.16-pre1"));
        assertSame(ReleaseChannel.DEV, ReleaseChannel.parse("2.2-dev"));

        assertSame(ReleaseChannel.ALPHA, ReleaseChannel.parse("2.2.20-alpha1"));
    }

}
