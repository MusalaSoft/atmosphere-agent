package com.musala.atmosphere.agent.devicewrapper.util;

/**
 * Contains the BATTERY_CHANGED intent constructor.
 * 
 * @author valyo.yolovski
 * 
 */
public class BatteryChangedIntentData
{
	private Integer level;

	private Integer state;

	private Integer launchFlags; // 0x600000010 - dafult launch flag

	private Integer icon_small; // 1730328 - default value

	private Integer scale; // 100 - default scale is 100

	private String technology; // "Li-ion" - default value

	private Integer voltage; // 4178 - most common value

	private Integer invalid_charger; // 0 - default is charger is valid

	private Integer plugged; // 2 - if plugged to usb; 1 - to ACD; 0 - not plugged

	private Integer health; // 2 - default value

	private Integer temperature; // 280 - most common value

	public String present; // "true" - default value for battery presence

	public String getPresent()
	{
		return present;
	}

	public void setPresent(String present)
	{
		this.present = present;
	}

	public Integer getLaunchFlags()
	{
		return launchFlags;
	}

	public void setLaunchFlags(int launchFlags)
	{
		this.launchFlags = launchFlags;
	}

	public Integer getScale()
	{
		return scale;
	}

	public void setScale(int scale)
	{
		this.scale = scale;
	}

	public String getTechnology()
	{
		return technology;
	}

	public void setTechnology(String technology)
	{
		this.technology = technology;
	}

	public Integer getVoltage()
	{
		return voltage;
	}

	public void setVoltage(int voltage)
	{
		this.voltage = voltage;
	}

	public Integer getInvalid_charger()
	{
		return invalid_charger;
	}

	public void setInvalid_charger(int invalid_charger)
	{
		this.invalid_charger = invalid_charger;
	}

	public Integer getPlugged()
	{
		return plugged;
	}

	public void setPlugged(int plugged)
	{
		this.plugged = plugged;
	}

	public Integer getHealth()
	{
		return health;
	}

	public void setHealth(int health)
	{
		this.health = health;
	}

	public Integer getTemperature()
	{
		return temperature;
	}

	public void setTemperature(int temperature)
	{
		this.temperature = temperature;
	}

	public Integer getLevel()
	{
		return level;
	}

	public void setLevel(int level)
	{
		this.level = level;
	}

	public Integer getState()
	{
		return state;
	}

	public void setState(int state)
	{
		this.state = state;
	}

	public Integer getIcon_small()
	{
		return icon_small;
	}

	public void setIcon_small(int icon_small)
	{
		this.icon_small = icon_small;
	}
}