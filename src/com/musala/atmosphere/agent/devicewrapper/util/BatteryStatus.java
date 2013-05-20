package com.musala.atmosphere.agent.devicewrapper.util;

/**
 * Represents the power states of a device.
 * 
 * @author georgi.gaydarov
 * 
 */
public enum BatteryStatus
{
	UNKNOWN("unknown"), CHARGING("charging"), DISCHARGING("discharging"), NOT_CHARGING("not-charging"), FULL("full");

	private String value;

	private BatteryStatus(String state)
	{
		this.value = state;
	}

	@Override
	public String toString()
	{
		return value;
	}
}
