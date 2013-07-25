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

	private static final String LINE_PREFFIX = ">> ";

	private static final Logger LOGGER = Logger.getLogger(AgentConsole.class.getCanonicalName());

	private BufferedReader consoleReader;

	private BufferedWriter consoleWriter;

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
	 */
	public void write(String message)
	{
		try
		{
			consoleWriter.write(message);
			consoleWriter.flush();

		}
		catch (IOException e)
		{
			LOGGER.error("Error while printing information on the console.", e);
		}
	}

	/**
	 * Prints line of text to the console. The line consists of the given text, contatenated with the character for new
	 * line.
	 * 
	 * @param message
	 *        - message or command to be written on the agent console.
	 */
	public void writeLine(String message)
	{
		write(message + LINE_SEPARATOR);
	}
}
