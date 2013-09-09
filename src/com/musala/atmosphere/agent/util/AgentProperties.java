package com.musala.atmosphere.agent.util;

/**
 * Enumeration class containing all possible agent properties.
 * 
 * @author valyo.yolovski
 * 
 */
public enum AgentProperties
{
	ADB_PATH("adb.path"), ADB_CONNECTION_TIMEOUT("adb.connection.timeout"), AGENT_RMI_PORT("agent.rmi.port"), EMULATOR_CREATION_WAIT_TIMEOUT(
			"emulator.creation.wait.timeout"), ANDROID_TOOL_PATH("android.tool.path"), ANDROID_SDK_TOOLS_PATH(
			"android.sdk.tools.path"), ANDROID_TOOL_WORKDIR_PATH("android.tool.workdir.path"), ANDROID_TOOL_CLASS(
			"android.tool.class"), EMULATOR_EXECUTABLE_PATH("emulator.executable.path"), COMMAND_EXECUTION_TIMEOUT(
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
