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

package com.musala.atmosphere.agent.entity;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Base64;

import org.apache.log4j.Logger;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.SyncException;
import com.android.ddmlib.TimeoutException;
import com.musala.atmosphere.agent.devicewrapper.util.ShellCommandExecutor;
import com.musala.atmosphere.commons.exceptions.CommandFailedException;

/**
 * Entity responsible for operations related with images.
 *
 * @author yavor.stankov
 *
 */
public class ImageEntity {
    private static final Logger LOGGER = Logger.getLogger(ImageEntity.class.getCanonicalName());

    private static final String SCREENSHOT_REMOTE_FILE_NAME = "/data/local/tmp/remote_screen.png";

    private static final String SCREENSHOT_LOCAL_FILE_NAME = "local_screen.png";

    private static final String SCREENSHOT_COMMAND = "screencap -p " + SCREENSHOT_REMOTE_FILE_NAME;

    private ShellCommandExecutor shellCommandExecutor;

    private IDevice wrappedDevice;

    /**
     * Constructs a new {@link ImageEntity} object by a given {@link ShellCommandExecutor shell command executor}
     * and a {@link IDevice wrapped ddmlib device}.
     *
     * @param shellCommandExecutor
     *        - a shell command executor to execute shell commands with
     *
     * @param wrappedDevice
     *        - a wrapped ddmlib device
     */
    ImageEntity(ShellCommandExecutor shellCommandExecutor, IDevice wrappedDevice) {
        this.shellCommandExecutor = shellCommandExecutor;
        this.wrappedDevice = wrappedDevice;
    }

    /**
     * Returns a JPEG compressed display screenshot.
     *
     * @return Image as base64 encoded {@link String}
     * @throws CommandFailedException
     *         In case of an error in the execution
     */
    public String getScreenshot() throws CommandFailedException {
        shellCommandExecutor.execute(SCREENSHOT_COMMAND);

        FileInputStream fileReader = null;

        try {
            wrappedDevice.pullFile(SCREENSHOT_REMOTE_FILE_NAME, SCREENSHOT_LOCAL_FILE_NAME);

            File localScreenshotFile = new File(SCREENSHOT_LOCAL_FILE_NAME);
            fileReader = new FileInputStream(localScreenshotFile);

            final long sizeOfScreenshotFile = localScreenshotFile.length();
            byte[] screenshotData = new byte[(int) sizeOfScreenshotFile];
            fileReader.read(screenshotData);
            fileReader.close();

            String screenshotBase64String = Base64.getEncoder().encodeToString(screenshotData);

            return screenshotBase64String;
        } catch (IOException | AdbCommandRejectedException | TimeoutException | SyncException e) {
            LOGGER.error("Screenshot fetching failed.", e);
            throw new CommandFailedException("Screenshot fetching failed.", e);
        }
    }
}
