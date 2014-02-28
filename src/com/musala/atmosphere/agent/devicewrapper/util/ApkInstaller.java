package com.musala.atmosphere.agent.devicewrapper.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.log4j.Logger;

import com.android.ddmlib.IDevice;
import com.android.ddmlib.InstallException;
import com.musala.atmosphere.commons.exceptions.CommandFailedException;

/**
 * Class responsible for APK file transfer and installation on a device.
 * 
 * @author georgi.gaydarov
 * 
 */
public class ApkInstaller {
    private static final Logger LOGGER = Logger.getLogger(ApkInstaller.class.getCanonicalName());

    private static final String TEMP_APK_FILE_SUFFIX = ".apk";

    private File tempApkFile;

    private OutputStream tempApkFileOutputStream;

    private final IDevice device;

    /**
     * Creates an APK installer instance for a specified {@link IDevice}.
     * 
     * @param forDevice
     *        - the device that this instance will install to.
     */
    public ApkInstaller(IDevice forDevice) {
        device = forDevice;
    }

    /**
     * Creates a new .apk file that will be installed on the current device. Use {@link #appendToAPK(byte[], int)
     * appendToAPK(byte[])} and {@link #buildAndInstallAPK() buildAndInstallAPK()} to transfer the file. If another file
     * is being transfered, it will be discarded.
     * 
     * @throws CommandFailedException
     */
    public void initAPKInstall() throws CommandFailedException {
        String tempApkFilePrefix = device.getSerialNumber();
        // replaces everything that is not a letter,
        // number or an underscore with an underscore
        tempApkFilePrefix = tempApkFilePrefix.replaceAll("\\W+", "_");

        try {
            // discard any leftovers from previous APK installation
            discardAPK();
            // create new temporary files and resources
            tempApkFile = File.createTempFile(tempApkFilePrefix, TEMP_APK_FILE_SUFFIX);
            tempApkFileOutputStream = new BufferedOutputStream(new FileOutputStream(tempApkFile));
        } catch (IOException e) {
            throw new CommandFailedException("Initiating APK transfer and installation environment failed.", e);
        }
    }

    /**
     * Appends bytes to the .apk file that is currently being built. Use {@link #buildAndInstallAPK()} to install the
     * transfered .apk file or {@link #discardAPK()} to discard all transfered data.
     * 
     * @param bytes
     *        - Byte array to append to the .apk file that is being built.
     * @param length
     *        - The number of actual bytes to write.
     * @throws CommandFailedException
     */
    public void appendToAPK(byte[] bytes, int length) throws CommandFailedException {
        if (tempApkFile == null || tempApkFileOutputStream == null) {
            throw new IllegalStateException("Temp .apk file should be created (by calling initAPKInstall()) before any calls to appendToAPK() and buildAndInstallAPK().");
        }
        try {
            tempApkFileOutputStream.write(bytes, 0, length);
        } catch (IOException e) {
            throw new CommandFailedException("Failed to append byte array to .apk file.", e);
        }
    }

    /**
     * Builds the transfered .apk file, uploads and then installs it on the current device.
     * 
     * @throws CommandFailedException
     */
    public void buildAndInstallAPK() throws CommandFailedException {
        if (tempApkFile == null || tempApkFileOutputStream == null) {
            throw new IllegalStateException("Temp .apk file should be created (by calling initAPKInstall()) before any calls to appendToAPK() and buildAndInstallAPK().");
        }

        try {
            tempApkFileOutputStream.flush();
            tempApkFileOutputStream.close();
            tempApkFileOutputStream = null;
            String absolutePathToApk = tempApkFile.getAbsolutePath();

            String installResult = device.installPackage(absolutePathToApk, true /* force reinstall */);
            discardAPK();

            if (installResult != null) {
                LOGGER.error("PacketManager installation returned error code '" + installResult + "'.");
                throw new CommandFailedException("PacketManager installation returned error code '" + installResult
                        + "'.");
            }
        } catch (InstallException | IOException e) {
            LOGGER.error("Installing apk failed.", e);
            throw new CommandFailedException("Installing .apk file failed.", e);
        }
    }

    /**
     * Discards all transfered data.
     */
    public void discardAPK() {
        if (tempApkFileOutputStream != null) {
            try {
                tempApkFileOutputStream.close();
            } catch (IOException e) {
                // closing failed, it was never open. nothing to do here.
            }
            tempApkFileOutputStream = null;
        }

        if (tempApkFile != null) {
            if (tempApkFile.exists()) {
                tempApkFile.delete();
            }
            tempApkFile = null;
        }
    }
}
