package com.musala.atmosphere.agent.util;

/**
 * Enumeration class containing all possible agent properties.
 * 
 * @author valyo.yolovski
 * 
 */
public enum AgentProperties
{ // add more if you need
	PATH_TO_ADB("path.to.adb"), ADBRIDGE_TIMEOUT("adbridge.timeout"), AGENT_RMI_PORT("agent.rmi.port"), EMULATOR_CREATION_WAIT(
			"emulator.creation.wait"), EMULATOR_CREATION_WAIT_TIMEOUT("emulator.creation.wait.timeout"), ANDROID_TOOL_PATH(
			"android.tool.path"), ANDROID_TOOLSDIR_PATH("android.toolsdir.path"), ANDROID_WORKDIR_PATH(
			"android.workdir.path"), ANDROIDTOOL_CLASS("androidtool.class"), EMULATOR_EXECUTABLE("emulator.executable"), RMI_MINIMAL_PORT_VALUE(
			"rmi.minimal.port.value"), RMI_MAXIMAL_PORT_VALUE("rmi.maximal.port.value"), COMMAND_EXECUTION_TIMEOUT(
			"command.execution.timeout");

	private String value;

	private AgentProperties(String value)
	{
		this.value = value;
	}

	@Override
	public String toString()
	{
		return value;
	}
};
