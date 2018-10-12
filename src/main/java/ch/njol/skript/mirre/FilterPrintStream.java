package ch.njol.skript.mirre;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import ch.njol.skript.command.Commands;

public class FilterPrintStream extends PrintStream {

	
	public FilterPrintStream(final OutputStream out, final boolean autoFlush, final String encoding) throws UnsupportedEncodingException {
		super(out, autoFlush, encoding);
	}

	public FilterPrintStream(final OutputStream out, final boolean autoFlush) {
		super(out, autoFlush);
	}

	public FilterPrintStream(final OutputStream out) {
		super(out);
	}
	
	@Override
	public synchronized void println(@SuppressWarnings("null") final String string){
		if(Commands.suppressUnknownCommandMessage && string.contains("Unknown command. Type")){
			Commands.suppressUnknownCommandMessage = false;
			return;
		}
		super.println(string);
	}

}
