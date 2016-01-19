package com.att.aro.console.printstreamutils;

import java.io.PrintStream;

import org.apache.log4j.Level;

public class OutSave {
	private final PrintStream out;
	private final Level level;

	public OutSave(PrintStream out, Level level) {
		this.out = out;
		this.level = level;
	}

	public PrintStream getOut() {
		return out;
	}
	public Level getLevel() {
		return level;
	}


	@Override
	public String toString() {
		return "OutSave [out=" + out + ", level=" + level + "]";
	}
}
