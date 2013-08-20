package com.musala.atmosphere.agent.command;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.musala.atmosphere.agent.Agent;

/**
 * Return the time in which the agent has been started and the uptime interval since then.
 * 
 * @author nikola.taushanov
 * 
 */
public class UptimeCommand extends NoParamsAgentCommand
{
	public UptimeCommand(Agent agent)
	{
		super(agent);
	}

	@Override
	protected void executeCommand(String[] params)
	{
		Date agentStartDate = agent.getStartDate();
		if (agentStartDate != null)
		{
			SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String formattedTime = dateFormatter.format(agentStartDate);
			agent.writeLineToConsole("Agent was ran on " + formattedTime);

			String formattedTimeInterval = getTimeInterval(agentStartDate);
			agent.writeLineToConsole("Uptime: " + formattedTimeInterval);
		}
	}

	private String getTimeInterval(Date since)
	{
		Date currentTime = new Date();
		long timeInterval = currentTime.getTime() - since.getTime();

		Calendar cal = Calendar.getInstance();
		cal.set(0, 0, 0, 0, 0);
		cal.setTimeInMillis(cal.getTimeInMillis() + timeInterval);

		SimpleDateFormat timeIntervalFormatter = new SimpleDateFormat("HH:mm:ss");
		String formattedTimeInterval = timeIntervalFormatter.format(cal.getTime());
		return formattedTimeInterval;
	}
}
