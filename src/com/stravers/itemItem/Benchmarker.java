package com.stravers.itemItem;

public class Benchmarker {

	private static final String ELAPSED_TIME = "elapsed time in millis: ";
	private static final String ELAPSED_NANO_TIME = "\nelapsed time in nanos: ";
	private static final String RECORD_START = "starting benchmarking at ";
	private static final String RECORD_END = "stopping benchmarking at ";
	private static final String IN_MILLIS = " in milliseconds.";
	
	private long startTime;
	private long endTime;
	private long startNanoTime;
	private long endNanoTime;
	
	public Benchmarker() {
		startTime = 0;
		endTime = 0;
		startNanoTime = 0;
		endNanoTime = 0;
	}
	
	public void startTimer() {
		startTime = System.currentTimeMillis();
		startNanoTime = System.nanoTime();
		Utilities.printMessage(RECORD_START + startTime + IN_MILLIS);
	}
	
	public void stopTimer() {
		endTime = System.currentTimeMillis();
		endNanoTime = System.nanoTime();
		Utilities.printMessage(RECORD_END + endTime + IN_MILLIS);
	}
	
	public String getElapsedTime() {
		long elapsedTime = endTime - startTime;
		long elapsedNanoTime = endNanoTime - startNanoTime;
		String result = ELAPSED_TIME + elapsedTime + ELAPSED_NANO_TIME + elapsedNanoTime;
		return result;
	}
}
