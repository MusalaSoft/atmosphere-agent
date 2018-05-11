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

package com.musala.atmosphere.agent.util.settings;

/**
 * Secure system settings, containing system preferences that applications can read but are not allowed to write. These
 * are for preferences that the user must explicitly modify through the system UI or specialized APIs for those values,
 * not modified directly by applications. We can get or change these settings using the 'content' command utility
 * provided by Android's shell.
 *
 * @see <a href="http://developer.android.com/reference/android/provider/Settings.Secure.html">developer.android.com</a>
 *
 * @author nikola.taushanov
 *
 */
public enum AndroidSecureSettings implements IAndroidSettings {
    // TODO: Implement someday when required.
    /**
     * None value.
     */
    NONE("");

    private String settingName;

    private static final String CONTENT_URI = "content://settings/secure";

    private AndroidSecureSettings(String settingName) {
        this.settingName = settingName;
    }

    /**
     * @return the name of the enum constant
     */
    @Override
    public String toString() {
        return settingName;
    }

    /**
     * @return the uniform resource identifier
     */
    @Override
    public String getContentUri() {
        return CONTENT_URI;
    }
}
