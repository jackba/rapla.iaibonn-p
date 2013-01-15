package org.rapla.plugin.wobexport;

public class StyledWobEntry {
	private boolean error;
	private String line;
	
	StyledWobEntry(final String content, final boolean error) {
		this.error = error;
		this.line = content;
	}

	public boolean isError() {
		return error;
	}

	public String getLine() {
		return line;
	}
}
