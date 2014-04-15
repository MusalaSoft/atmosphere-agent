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

import com.musala.atmosphere.agent.devicewrapper.util.FileTransferService;
import com.musala.atmosphere.agent.devicewrapper.util.ShellCommandExecutor;
import com.musala.atmosphere.commons.exceptions.CommandFailedException;

/**
 * Utility class responsible for starting UIAutomator worker processes.
 * 
 * @author georgi.gaydarov
 * 
 */
public class UIAutomatorProcessStarter {
    private static final Logger LOGGER = Logger.getLogger(UIAutomatorProcessStarter.class.getCanonicalName());

    private static final String LOCAL_REQUEST_FILE_NAME_FORMAT = "request%d.ser";

    private static final String ENTRY_POINT_CLASS = "com.musala.atmosphere.uiautomator.ActionDispatcher";

    private static final String AUTOMATOR_JAR_NAME = "AtmosphereUIAutomatorBridge.jar";

    private static final String[] EXTERNAL_LIBRARY_NAMES = {"AtmosphereUIAutomatorBridgeLibs.jar"};

    private Map<String, String> attachmentsKeys;

    private int attachedObjectsId = 0;

    private Map<String, String> params;

    public UIAutomatorProcessStarter() {
        params = new HashMap<String, String>();
        attachmentsKeys = new HashMap<String, String>();
    }

    public void addParameter(String key, String value) {
        params.put(key, value);
    }

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
     *        - the device {@link ShellCommandExecutor} instance to be used for UIAutomator starting.
     * @param service
     *        - the device {@link FileTransferService} instance to be used for file uploading.
     * @return the execution console response.
     * @throws CommandFailedException
     */
    public String run(ShellCommandExecutor executor, FileTransferService service) throws CommandFailedException {
        uploadRequiredFiles(service);
        String cmdLine = buildCommand();
        return executor.execute(cmdLine);
    }

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
        builder.append(AUTOMATOR_JAR_NAME);
        for (String lib : EXTERNAL_LIBRARY_NAMES) {
            builder.append(" ");
            builder.append(lib);
        }
        builder.append(" -c ");
        builder.append(ENTRY_POINT_CLASS);

        for (Map.Entry<String, String> entry : params.entrySet()) {
            builder.append(" -e ");
            builder.append(entry.getKey());
            builder.append(" ");
            builder.append(entry.getValue());
        }

        return builder.toString();
    }
}