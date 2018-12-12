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

package ch.njol.skript.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
public class ConfigReader extends BufferedReader {
	
	@SuppressWarnings("null")
	public final static Charset UTF_8 = Charset.forName("UTF-8");
	
	@Nullable
	private String line;
	private boolean reset;
	private int ln;
	
	private boolean hasNonEmptyLine;
	
	public ConfigReader(final InputStream source) {
		super(new InputStreamReader(source, UTF_8));
	}
	
	@Override
	@Nullable
	public String readLine() throws IOException {
		if (reset) {
			reset = false;
		} else {
			line = stripUTF8BOM(super.readLine());
			ln++;
		}
		return line;
	}
	
	@Nullable
	private String stripUTF8BOM(final @Nullable String line) {
		if (!hasNonEmptyLine && line != null && !line.isEmpty()) {
			hasNonEmptyLine = true;
			if (line.startsWith("\uFEFF")) {
				return line.substring(1);
			}
		}
		return line;
	}
	
	@Override
	public void reset() {
		if (reset)
			throw new IllegalStateException("reset was called twice without a readLine inbetween");
		reset = true;
	}
	
	public int getLineNum() {
		return ln;
	}
	
	@Nullable
	public String getLine() {
		return line;
	}
	
	@Override
	public boolean markSupported() {
		return false;
	}
	
	@Override
	public void mark(final int readAheadLimit) throws IOException {
		throw new UnsupportedOperationException();
	}
	
}
