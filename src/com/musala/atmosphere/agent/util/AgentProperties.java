package com.musala.atmosphere.agent.util;

public enum AgentProperties
{ // add more if you need
	PATH_TO_ADB("PATH_TO_ADB"), ADBRIDGE_TIMEOUT("ADBRIDGE_TIMEOUT"), AGENT_RMI_PORT("AGENT_RMI_PORT"), EMULATOR_CREATION_WAIT(
			"EMULATOR_CREATION_WAIT"), EMULATOR_CREATION_WAIT_TIMEOUT("EMULATOR_CREATION_WAIT_TIMEOUT"), ANDROID_TOOL_PATH(
			"ANDROID_TOOL_PATH"), ANDROID_TOOLSDIR_PATH("ANDROID_TOOLSDIR_PATH"), ANDROID_WORKDIR_PATH(
			"ANDROID_WORKDIR_PATH"), ANDROIDTOOL_CLASS("ANDROIDTOOL_CLASS"), EMULATOR_EXECUTABLE("EMULATOR_EXECUTABLE");

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
