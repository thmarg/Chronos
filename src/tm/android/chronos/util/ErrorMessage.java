/*
 * Message
 *
 * Copyright (c) 2015 Thierry Margenstern under MIT license
 * http://opensource.org/licenses/MIT
 */

package tm.android.chronos.util;

/**
 * A simple class encaplsulate an Exception and a localized message<br>
 *     So far in the jdk Exception.getLocalizedMessage return an english message.
 */
public class ErrorMessage {
	public Exception exception;
	public String localiszedMessage;

	public ErrorMessage(){}

}
