// This file is part of the ATMOSPHERE mobile testing framework.
// Copyright (C) 2016 MusalaSoft
//
// ATMOSPHERE is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// ATMOSPHERE is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with ATMOSPHERE.  If not, see <http://www.gnu.org/licenses/>.

package com.musala.atmosphere.agent.devicewrapper.util;

import com.musala.atmosphere.commons.util.IntentBuilder;
import com.musala.atmosphere.commons.util.IntentBuilder.IntentAction;

/**
 * Contains the BATTERY_CHANGED intent constructor.
 * 
 * @author valyo.yolovski
 * 
 */
public class BatteryChangedIntentData {
    private Integer level;

    private Integer state;

    private Integer launchFlags; // 0x600000010 - dafult launch flag

    private Integer iconSmall; // 1730328 - default value

    private Integer scale; // 100 - default scale is 100

    private String technology; // "Li-ion" - default value

    private Integer voltage; // 4178 - most common value

    private Integer invalidCharger; // 0 - default is charger is valid

    private Integer plugged; // 2 - if plugged to usb; 1 - to ACD; 0 - not plugged

    private Integer health; // 2 - default value

    private Integer temperature; // 280 - most common value

    public Boolean present; // "true" - default value for battery presence

    public Boolean getPresent() {
        return present;
    }

    public void setPresent(Boolean present) {
        this.present = present;
    }

    public Integer getLaunchFlags() {
        return launchFlags;
    }

    public void setLaunchFlags(int launchFlags) {
        this.launchFlags = launchFlags;
    }

    public Integer getScale() {
        return scale;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }

    public String getTechnology() {
        return technology;
    }

    public void setTechnology(String technology) {
        this.technology = technology;
    }

    public Integer getVoltage() {
        return voltage;
    }

    public void setVoltage(int voltage) {
        this.voltage = voltage;
    }

    public Integer getInvalidCharger() {
        return invalidCharger;
    }

    public void setInvalidCharger(int invalidCharger) {
        this.invalidCharger = invalidCharger;
    }

    public Integer getPlugged() {
        return plugged;
    }

    public void setPlugged(int plugged) {
        this.plugged = plugged;
    }

    public Integer getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public Integer getTemperature() {
        return temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public Integer getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public Integer getIconSmall() {
        return iconSmall;
    }

    public void setIconSmall(int iconSmall) {
        this.iconSmall = iconSmall;
    }

    public String buildIntentQuery() {
        IntentBuilder intentBuilder = new IntentBuilder(IntentAction.BATTERY_CHANGED);

        if (getLaunchFlags() != null) {
            intentBuilder.setFlags(getLaunchFlags());
        }
        if (getIconSmall() != null) {
            intentBuilder.putExtraInteger("icon-small", getIconSmall());
        }
        if (getPresent() != null) {
            intentBuilder.putExtraBoolean("present", getPresent());
        }
        if (getScale() != null) {
            intentBuilder.putExtraInteger("scale", getScale());
        }
        if (getLevel() != null) {
            intentBuilder.putExtraInteger("level", getLevel());
        }
        if (getTechnology() != null) {
            intentBuilder.putExtraString("technology", getTechnology());
        }
        if (getState() != null) {
            intentBuilder.putExtraInteger("status", getState());
        }
        if (getVoltage() != null) {
            intentBuilder.putExtraInteger("voltage", getVoltage());
        }
        if (getInvalidCharger() != null) {
            intentBuilder.putExtraInteger("invalid_charger", getInvalidCharger());
        }
        if (getPlugged() != null) {
            intentBuilder.putExtraInteger("plugged", getPlugged());
        }
        if (getHealth() != null) {
            intentBuilder.putExtraInteger("health", getHealth());
        }
        if (getTemperature() != null) {
            intentBuilder.putExtraInteger("temperature", getTemperature());
        }
        String query = intentBuilder.buildIntentCommand();
        return query;
    }
}
