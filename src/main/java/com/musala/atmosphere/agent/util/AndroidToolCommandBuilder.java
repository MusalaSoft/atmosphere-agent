package com.musala.atmosphere.agent.util;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.musala.atmosphere.commons.exceptions.CommandFailedException;
import com.musala.atmosphere.commons.sa.EmulatorParameters;
import com.musala.atmosphere.commons.util.Pair;

/**
 * A builder of commands for the android tool.
 *
 * @author yordan.petrov
 *
 */
public class AndroidToolCommandBuilder {
    private final static Logger LOGGER = Logger.getLogger(AndroidToolCommandBuilder.class.getCanonicalName());

    private static final String CREATE_AVD_COMMAND = "create avd ";

    private static final String LIST_TARGETS_COMMAND = "list targets";

    private static final String DELETE_AVD_COMMAND_FORMAT = "delete avd -n %s";

    /**
     * Pattern matching an Android target section returned from the "list targets" android tool command. The IDs of the
     * target as well as the available ABIs are matched in the first three groups.
     */
    private static final String TARGET_LIST_PATTERN = "id:\\s(\\d+)\\sor\\s\"(.*?)\".*?ABIs\\s:\\s([\\w\\s,-]*?)(?=(\\n----------)|(\\n$))";

    // TODO: consider adding this as an emulator property.
    private static final String FALLBACK_TARGET_ID = "1";

    private static final AbiType ABI_TYPE = AgentPropertiesLoader.getEmulatorAbiType();

    private static enum CreateAvdCommandParameter {
        // TODO: Add all parameters.
        SDCARD("-c %s ", "Path to a shared SD card image, or size of a new sdcard for the new AVD.", false),
        NAME("-n %s ", "Name of the new AVD.", true),
        SNAPSHOT("-a %s ", "Place a snapshots file in the AVD, to enable persistence.", false),
        PATH("-p %s ", "Directory where the new AVD will be created.", false),
        FORCE("-f ", "Forces creation (overwrites an existing AVD).", false),
        SKIN("-s %dx%d ", "Skin for the new AVD.", false),
        TARGET("-t %s ", "Target ID of the new AVD.", true),
        ABI("-b %s ", "The ABI to use for the AVD. The default is to auto-select the ABI if the platform has only one ABI for its system images.", false);

        private String format;

        private String description;

        private boolean isRequired;

        private CreateAvdCommandParameter(String format, String description, boolean isRequired) {
            this.format = format;
            this.description = description;
            this.isRequired = isRequired;
        }

        public String getFormat() {
            return format;
        }

        public String getDescription() {
            return description;
        }

        public boolean isRequired() {
            return isRequired;
        }
    }

    private String avdName;

    private EmulatorParameters deviceParameters;

    private SdkToolCommandSender sdkToolCommandSender;

    public AndroidToolCommandBuilder(String avdName, EmulatorParameters deviceParameters) {
        this.avdName = avdName;
        this.deviceParameters = deviceParameters;
        this.sdkToolCommandSender = new SdkToolCommandSender();
    }

    /**
     * Checks whether a given Android target has an system image for the given {@link AbiType}.
     *
     * @param targetId
     *        - the identifier of the Android target.
     * @param requiredAbi
     *        - the {@link AbiType} to check for.
     * @return true if system image for the {@link AbiType} is found, false if such is not found.
     * @throws CommandFailedException
     *         when fails to execute the command
     */
    private boolean hasAbi(String targetId, AbiType requiredAbi) throws CommandFailedException {
        String[] availableTargetAbis = getAvailableTargetAbis(targetId);
        for (String availableAbi : availableTargetAbis) {
            if (requiredAbi.toString().equals(availableAbi)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a list of the available {@link AbiType} system images for the given target.
     *
     * @param targetId
     *        - the identifier of the Android target.
     * @return a list of the available {@link AbiType} system images for the given target.
     * @throws CommandFailedException
     *         when fails to get an available target abis
     */
    private String[] getAvailableTargetAbis(String targetId) throws CommandFailedException {
        String[] availableTargetAbis = null;
        try {
            String result = sdkToolCommandSender.sendCommandToAndroidTool(LIST_TARGETS_COMMAND, "");
            Pattern pattern = Pattern.compile(TARGET_LIST_PATTERN, Pattern.DOTALL);
            Matcher matcher = pattern.matcher(result);
            while (matcher.find()) {
                String foundTargetIdIndex = matcher.group(1);
                String foundTargetIdName = matcher.group(2);
                if (targetId.equals(foundTargetIdIndex) || targetId.equals(foundTargetIdName)) {
                    String foundTargetAbisString = matcher.group(3);
                    availableTargetAbis = foundTargetAbisString.split(", ");
                    break;
                }
            }
        } catch (IOException e) {
            String message = "Getting target list failed.";
            LOGGER.fatal(message, e);
            throw new CommandFailedException(message);
        }
        return availableTargetAbis;
    }

    /**
     * Returns a command for creating an Android Virtual Device profile.
     *
     * @return a command for creating an Android Virtual Device profile.
     * @throws CommandFailedException
     *         thrown when the command fails
     *
     */
    public String getCreateAvdCommand() throws CommandFailedException {
        String target = deviceParameters.getTarget();
        if (target == null) {
            target = FALLBACK_TARGET_ID;
        }
        if (!hasAbi(target, ABI_TYPE)) {
            return null;
        }
        String targetParameter = String.format(CreateAvdCommandParameter.TARGET.getFormat(), target);
        String nameParameter = String.format(CreateAvdCommandParameter.NAME.getFormat(), avdName);
        String abiParameter = String.format(CreateAvdCommandParameter.ABI.getFormat(), ABI_TYPE);
        String forceParameter = CreateAvdCommandParameter.FORCE.getFormat();
        Pair<Integer, Integer> emulatorResolution = deviceParameters.getResolution();

        StringBuilder createAvdCommandBuilder = new StringBuilder();
        createAvdCommandBuilder.append(CREATE_AVD_COMMAND);
        createAvdCommandBuilder.append(nameParameter);
        createAvdCommandBuilder.append(targetParameter);
        createAvdCommandBuilder.append(abiParameter);
        if (emulatorResolution != null) {
            String resolutionParameter = String.format(CreateAvdCommandParameter.SKIN.getFormat(),
                                                       emulatorResolution.getKey(),
                                                       emulatorResolution.getValue());
            createAvdCommandBuilder.append(resolutionParameter);
        }
        createAvdCommandBuilder.append(forceParameter);
        String createAvdCommand = createAvdCommandBuilder.toString();
        return createAvdCommand;
    }

    /**
     * Returns a command for deleting an Android Virtual Device profile.
     *
     * @return a command for deleting an Android Virtual Device profile.
     */
    public String getDeleteAvdCommand() {
        return String.format(DELETE_AVD_COMMAND_FORMAT, avdName);
    }
}
