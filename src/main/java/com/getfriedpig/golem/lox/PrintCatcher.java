package com.getfriedpig.golem.lox;

import java.util.ArrayList;

public final class PrintCatcher {
	static final ArrayList<String> log = new ArrayList<>();
	public static void Print(String string) {
		log.add(string);
	}
	
	public static ArrayList<String> GetLog() {
		return log;
	}

	public static void ClearLog() { log.clear(); }
}
