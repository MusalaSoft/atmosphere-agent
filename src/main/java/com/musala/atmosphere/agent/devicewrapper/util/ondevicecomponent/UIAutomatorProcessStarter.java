package com.musala.atmosphere.agent.devicewrapper.util.ondevicecomponent;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.musala.atmosphere.agent.devicewrapper.util.BackgroundShellCommandExecutor;
import com.musala.atmosphere.agent.devicewrapper.util.FileTransferService;
import com.musala.atmosphere.agent.devicewrapper.util.ShellCommandExecutor;
import com.musala.atmosphere.agent.util.OnDeviceComponent;
import com.musala.atmosphere.commons.exceptions.CommandFailedException;

/**
 * Utility class responsible for starting UIAutomator worker processes.
 *
 * @author georgi.gaydarov
 *
 */
public class UIAutomatorProcessStarter {
    private static final Logger LOGGER = Logger.getLogger(UIAutomatorProcessStarter.class.getCanonicalName());

    @Deprecated
    private static final String LOCAL_REQUEST_FILE_NAME_FORMAT = "request%d.ser";

    private static final String[] EXTERNAL_LIBRARY_NAMES = {};

    private Map<String, String> attachmentsKeys;

    @Deprecated
    private int attachedObjectsId = 0;

    private Map<String, String> params;

    public UIAutomatorProcessStarter() {
        params = new HashMap<>();
        attachmentsKeys = new HashMap<>();
    }

    public void addParameter(String key, String value) {
        params.put(key, value);
    }

    @Deprecated
    public void attachObject(String key, Serializable object) {
        String fileName = String.format(LOCAL_REQUEST_FILE_NAME_FORMAT, attachedObjectsId);

        try {
            OutputStream buffer = new BufferedOutputStream(new FileOutputStream(fileName));
            ObjectOutput output = new ObjectOutputStream(buffer);
            output.writeObject(object);
            output.close();
        } catch (IOException e) {
            LOGGER.error("Could not serialize object.", e);
            return;
        }

        attachmentsKeys.put(fileName, key);
        addParameter(key, fileName);
        attachedObjectsId++;
    }

    public void removeParameter(String key) {
        String valueToRemove = params.get(key);
        params.remove(key);
        if (attachmentsKeys.containsKey(valueToRemove)) {
            attachmentsKeys.remove(valueToRemove);
        }
    }

    /**
     * Starts a UIAutomator process with the previously specified parameters.
     *
     * @param executor
     *        - the device {@link ShellCommandExecutor} instance to be used for UIAutomator starting
     * @param service
     *        - the device {@link FileTransferService} instance to be used for file uploading
     * @param commandExecutorTimeout
     *        - timeout, that will be used when executing shell commands on the device
     * @return the execution console response
     * @throws CommandFailedException
     *         thrown when failed to run
     */
    @Deprecated
    public String run(ShellCommandExecutor executor, FileTransferService service, int commandExecutorTimeout)
        throws CommandFailedException {
        uploadRequiredFiles(service);
        String cmdLine = buildCommand();
        return executor.execute(cmdLine, commandExecutorTimeout);
    }

    /**
     * Starts a UIAutomator process with the previously specified parameters.
     *
     * @param executor
     *        - the device {@link ShellCommandExecutor} instance to be used for UIAutomator starting
     * @param service
     *        - the device {@link FileTransferService} instance to be used for file uploading
     * @return the execution console response
     * @throws CommandFailedException
     *         thrown when failed to run
     */
    @Deprecated
    public String run(ShellCommandExecutor executor, FileTransferService service) throws CommandFailedException {
        uploadRequiredFiles(service);
        String cmdLine = buildCommand();
        return executor.execute(cmdLine);
    }

    /**
     * Starts a UIAutomator process with the previously specified parameters.
     *
     * @param executor
     *        - the device {@link ShellCommandExecutor} instance to be used for UIAutomator starting
     * @throws CommandFailedException
     *         thrown when the execution fails
     */
    public void runInBackground(BackgroundShellCommandExecutor executor) throws CommandFailedException {
        String command = buildCommand();
        executor.executeInBackground(command);
    }

    @Deprecated
    private void uploadRequiredFiles(FileTransferService service) throws CommandFailedException {
        for (String fileName : attachmentsKeys.keySet()) {
            String remoteFullPath = service.pushFile(fileName);
            String fileParameterKey = attachmentsKeys.get(fileName);
            // replace the file name with full remote path
            addParameter(fileParameterKey, remoteFullPath);
        }
    }

    private String buildCommand() {
        StringBuilder builder = new StringBuilder();
        builder.append("uiautomator runtest ");
        builder.append(OnDeviceComponent.UI_AUTOMATOR_BRIDGE.getFileName());
        for (String lib : EXTERNAL_LIBRARY_NAMES) {
            builder.append(" ");
            builder.append(lib);
        }
        builder.append(" -c ");
        builder.append(OnDeviceComponent.UI_AUTOMATOR_BRIDGE.getPackageName());

        for (Map.Entry<String, String> entry : params.entrySet()) {
            builder.append(" -e ");
            builder.append(entry.getKey());
            builder.append(" ");
            builder.append(entry.getValue());
        }

        return builder.toString();
    }
}