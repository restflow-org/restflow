package org.restflow.util;

public abstract class Contract {

	private static boolean showStackTrace;
	private static boolean throwExceptions;
	
	static {
		synchronized(Contract.class) {
			showStackTrace = true;
			throwExceptions = false;
		}
	}
	
	public static void setShowStackTrace(boolean value) {
		synchronized(Contract.class) {
			showStackTrace = value;
		}
	}

	public static void setThrowExceptions(boolean value) {
		synchronized(Contract.class) {
			throwExceptions = value;
		}
	}

	
	public static void requires(boolean condition, String message) {
		if (condition == false) {
			
			synchronized(Contract.class) {
			
				if (throwExceptions) {
					throw new IllegalStateException(message);
				}
			}
		}
	}
	
	public static void requires(boolean condition) {
		if (condition == false) {
			
			synchronized(Contract.class) {
			
				if (throwExceptions) {	
					throw new IllegalStateException();
				}
			}
		}
	}
	
	public static void disallows(boolean condition, String message) {
		if (condition == true) {

			synchronized(Contract.class) {
			
				if (throwExceptions) {
					throw new IllegalStateException(message);
				}
			}
		}
	}

	public static void disallows(boolean condition) {
		if (condition == true) {
			
			synchronized(Contract.class) {
			
				if (throwExceptions) {
					throw new IllegalStateException();
				}
			}
		}
	}

}
