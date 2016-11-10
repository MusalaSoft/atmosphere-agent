package com.musala.atmosphere.agent.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * Sends commands to some of the available Android SDK tools.
 *
 * @author yordan.petrov
 *
 */
public class SdkToolCommandSender {
    private final static Logger LOGGER = Logger.getLogger(SdkToolCommandSender.class.getCanonicalName());

    private static final String PATH_TO_SDK_TOOLS = AgentPropertiesLoader.getAndroidSdkToolsDirPath();

    private static final String EMULATOR_EXECUTABLE = AgentPropertiesLoader.getEmulatorExecutable();

    private static final String ANDROID_EXECUTABLE = AgentPropertiesLoader.getAndroidExecutable();

    private static final String PATH_TO_EXECUTABLE_FORMAT = "%s%s%s ";

    private static final String PATH_TO_EMULTOR_EXECUTABLE = String.format(PATH_TO_EXECUTABLE_FORMAT,
                                                                           PATH_TO_SDK_TOOLS,
                                                                           File.separator,
                                                                           EMULATOR_EXECUTABLE);

    private static final String PATH_TO_ANDROID_EXECUTABLE = String.format(PATH_TO_EXECUTABLE_FORMAT,
                                                                           PATH_TO_SDK_TOOLS,
                                                                           File.separator,
                                                                           ANDROID_EXECUTABLE);

    /**
     * Sends a command to the android tool.
     *
     * @param command
     *        Command to be sent.
     * @param commandInput
     *        Input that should be sent to the android tool.
     * @return STDOUT and STDERR output from the android tool.
     * @throws IOException
     *         thrown when an I/O error occurs.
     */
    public String sendCommandToAndroidTool(String command, String commandInput) throws IOException {
        StringBuilder androidToolCommandBuilder = new StringBuilder();
        androidToolCommandBuilder.append(PATH_TO_ANDROID_EXECUTABLE);
        androidToolCommandBuilder.append(command);

        String builtCommand = androidToolCommandBuilder.toString();

        String returnValue = sendCommandViaRuntime(builtCommand, commandInput, "android-tool");
        return returnValue;
    }

    /**
     * Sends a command to the emulator executable.
     *
     * @param parameters
     *        Parameters to be passed to the executable.
     * @param commandInput
     *        Input to be sent to the emulator.
     * @return STDOUT and STDERR of the emulator executable.
     * @throws IOException
     *         thrown when an I/O error occurs.
     */
    public String sendCommandToEmulatorTool(List<String> parameters, String commandInput) throws IOException {
        List<String> command = new LinkedList<>();
        command.add(PATH_TO_EMULTOR_EXECUTABLE);
        command.addAll(parameters);

        String returnValue = sendCommandViaProcessBuilder(command, commandInput, "emulator.exe tool");
        return returnValue;
    }

    /**
     * Sends a command to the emulator executable and returns the starter process.
     *
     * @param parameters
     *        Parameters to be passed to the executable.
     * @param commandInput
     *        Input to be sent to the emulator.
     * @return {@link Process Process} - started process
     * @throws IOException
     *         thrown when an I/O error occurs.
     */
    public Process sendCommandToEmulatorToolAndReturn(List<String> parameters, String commandInput) throws IOException {
        List<String> command = new LinkedList<>();
        command.add(PATH_TO_EMULTOR_EXECUTABLE);
        command.addAll(parameters);

        Process returnProcess = sendCommandViaProcessBuilderAndReturn(command, commandInput, "emulator.exe tool");
        return returnProcess;
    }

    /**
     * Executes a command on the system via the {@link ProcessBuilder ProcessBuilder} class and waits for it to finish
     * executing.
     *
     * @param command
     *        Command name, followed by command arguments.
     * @param commandInputInput
     *        that should be sent to the executed command.
     * @param commandDescription
     *        Description of the executed command, used in error handling.
     * @return The STDOUT and STDERR of the executed command.
     * @throws IOException
     */
    private String sendCommandViaProcessBuilder(List<String> command, String commandInput, String commandDescription)
        throws IOException {
        Process process = null;
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true); // redirect STDERR to STDOUT
            process = processBuilder.start();
            BufferedWriter inputBuffer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
            inputBuffer.write(commandInput);
            inputBuffer.flush();
            process.waitFor();
        } catch (InterruptedException e) {
            LOGGER.warn("Process execution wait was interrupted.", e);
        } catch (IOException e) {
            LOGGER.fatal("Running " + commandDescription + " resulted in an IOException.", e);
            throw e;
        }

        if (process.exitValue() != 0) {
            LOGGER.fatal(commandDescription + " return code is nonzero for the command line '" + command + "'.");
        }

        StringBuilder responseBuilder = new StringBuilder();
        try {
            String readLine = "";

            BufferedReader responseBuffer = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while (true) {
                readLine = responseBuffer.readLine();
                if (readLine == null) {
                    break;
                }
                responseBuilder.append(readLine);
                responseBuilder.append('\n');
            }
        } catch (IOException e) {
            LOGGER.fatal("Reading the shell response of " + commandDescription + " in an IOException.", e);
            throw e;
        }

        return responseBuilder.toString();
    }

    /**
     * Executes a command via the {@link ProcessBuilder ProcessBuilder} class and returns without waiting for it to
     * finish executing.
     *
     * @param command
     *        Command name, followed by command arguments.
     * @param commandInputInput
     *        that should be sent to the executed command.
     * @param commandDescription
     *        Description of the executed command, used in error handling.
     * @throws IOException
     */
    private Process sendCommandViaProcessBuilderAndReturn(List<String> command,
                                                          String commandInput,
                                                          String commandDescription)
        throws IOException {
        Process process = null;
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true); // redirect STDERR to STDOUT
            process = processBuilder.start();
            BufferedWriter inputBuffer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
            inputBuffer.write(commandInput);
            inputBuffer.flush();
        } catch (IOException e) {
            LOGGER.fatal("Running " + commandDescription + " resulted in an IOException.", e);
            throw e;
        }
        return process;
    }

    /**
     * Executes a command on the system via the {@link Runtime Runtime} .exec() method.
     *
     * @param command
     *        Command to be executed (as if being passed to the system shell).
     * @param commandInput
     *        Input that should be sent to the executed command.
     * @param commandDescription
     *        Description of the executed command, used in error handling.
     * @return The STDOUT and STDERR of the executed command.
     * @throws IOException
     */
    private String sendCommandViaRuntime(String command, String commandInput, String commandDescription)
        throws IOException {
        Process process = null;
        try {
            Runtime runtime = Runtime.getRuntime();
            process = runtime.exec(command);
            BufferedWriter inputBuffer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
            inputBuffer.write(commandInput);
            inputBuffer.flush();
            process.waitFor();
        } catch (InterruptedException e) {
            LOGGER.warn("Process execution wait was interrupted.", e);
        } catch (IOException e) {
            LOGGER.warn("Running " + commandDescription + " resulted in an IOException.", e);
            throw e;
        }

        if (process.exitValue() != 0) {
            LOGGER.fatal(commandDescription + " return code is nonzero for the command line '" + command + "'.");
        }

        StringBuilder responseBuilder = new StringBuilder();
        try {
            String readLine = "";

            BufferedReader responseBuffer = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while (true) {
                readLine = responseBuffer.readLine();
                if (readLine == null) {
                    break;
                }
                responseBuilder.append(readLine);
                responseBuilder.append('\n');
            }
            responseBuffer = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            while (true) {
                readLine = responseBuffer.readLine();
                if (readLine == null) {
                    break;
                }
                responseBuilder.append(readLine);
                responseBuilder.append('\n');
            }
        } catch (IOException e) {
            LOGGER.warn("Reading the shell response of " + commandDescription + " in an IOException.", e);
            throw e;
        }

        return responseBuilder.toString();
    }

}
