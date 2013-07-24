package com.musala.atmosphere.agent;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.apache.log4j.Logger;

public class AgentConsole
{
	private static final String LINE_SEPARATOR = System.getProperty("line.separator");

	// TODO extract to config file
	private static final String LINE_PREFFIX = ">> ";

	// TODO extract to config file
	private static final int MAX_NUMBER_OF_FLUSH_TRIES = 50;

	private static final Logger LOGGER = Logger.getLogger(AgentConsole.class.getCanonicalName());

	private BufferedReader consoleReader = null;

	private BufferedWriter consoleWriter = null;

	/**
	 * Creates console for the agent. If Agent code is ran through some IDE ( for example Eclipse ) we must use
	 * <b>System.in</b> and <b>System.out</b> to manage the agent using text commands ( because System.console() is null
	 * when JVM is started automatically, for example - when it's launched from an IDE ); if the agent is ran through
	 * command prompt, then System.console() should be used.
	 */
	public AgentConsole()
	{
		consoleReader = (BufferedReader) new BufferedReader(new InputStreamReader(System.in));
		consoleWriter = (BufferedWriter) new BufferedWriter(new OutputStreamWriter(System.out));
	}

	/**
	 * Reads single line of text from the console. A line is considered to be terminated by any one of a line feed
	 * ('\n'), a carriage return ('\r'), or a carriage return followed immediately by a linefeed.
	 * 
	 * 
	 * @return - A String containing the contents of the line, not including any line-termination characters, or null if
	 *         the end of the stream has been reached
	 * @throws IOException
	 *         - if the agent is started through IDE, it is possible for an IOException to be thrown while reading from
	 *         the System.in
	 */
	public String readLine() throws IOException
	{
		System.out.print(LINE_PREFFIX);
		String line = consoleReader.readLine();
		return line;
	}

	/**
	 * Prints message to the agent console. No new line will be added to the end of the message.
	 * 
	 * @param message
	 *        - message or command to be written on the agent console.
	 * @throws IOException
	 *         - possible IOException could be thrown when Agent was started through some IDE and an internal error
	 *         occurs when trying to write to the System.out
	 */
	public void write(String message) throws IOException
	{
		System.out.print(LINE_PREFFIX);
		consoleWriter.write(message);
		int numberOfFailedFlushes = 0;

		for (int numberOfFlushTries = 0; numberOfFlushTries < MAX_NUMBER_OF_FLUSH_TRIES; numberOfFlushTries++)
		{
			try
			{
				consoleWriter.flush();
				break;
			}
			catch (Exception e)
			{
				numberOfFailedFlushes++;
			}
		}

		if (numberOfFailedFlushes == MAX_NUMBER_OF_FLUSH_TRIES)
		{
			LOGGER.error("Could not flush message: '" + message + "' to console.");
		}
	}

	/**
	 * Prints line of text to the console. The line consists of the given text, contatenated with the character for new
	 * line.
	 * 
	 * @param message
	 *        - message or command to be written on the agent console.
	 * @throws IOException
	 *         - possible IOException could be thrown when trying to write to the System.out
	 */
	public void writeLine(String message) throws IOException
	{
		System.out.print(LINE_PREFFIX);
		consoleWriter.write(message + LINE_SEPARATOR);
		int numberOfFailedFlushes = 0;

		for (int numberOfFlushTries = 0; numberOfFlushTries < MAX_NUMBER_OF_FLUSH_TRIES; numberOfFlushTries++)
		{
			try
			{
				consoleWriter.flush();
				break;
			}
			catch (Exception e)
			{
				numberOfFailedFlushes++;
			}
		}

		if (numberOfFailedFlushes == MAX_NUMBER_OF_FLUSH_TRIES)
		{
			LOGGER.error("Could not flush message: '" + message + "' to console.");
		}
	}
}
