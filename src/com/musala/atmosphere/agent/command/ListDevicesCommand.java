package com.musala.atmosphere.agent.command;

import java.util.List;

import com.android.ddmlib.IDevice;
import com.musala.atmosphere.agent.Agent;

/**
 * Lists all devices and emulators attached to the agent.
 * 
 * @author nikola.taushanov
 * 
 */
public class ListDevicesCommand extends NoParamsAgentCommand
{

	public ListDevicesCommand(Agent agent)
	{
		super(agent);
	}

	@Override
	protected void executeCommand(String[] params)
	{
		List<IDevice> allAtachedDevices = agent.getAllAttachedDevices();
		if (allAtachedDevices == null || allAtachedDevices.size() == 0)
		{
			agent.writeLineToConsole("No devices attached.");
		}
		else
		{
			agent.writeLineToConsole("List of devices attached : ");
			int counter = 1;
			for (IDevice attachedDevice : allAtachedDevices)
			{
				String consoleMessage = String.format(	"%d. %-20s %s",
														counter,
														attachedDevice.getState(),
														attachedDevice.getName());
				counter++;
				agent.writeLineToConsole(consoleMessage);
			}
		}
	}
}
