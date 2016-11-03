# atmosphere-agent
The agent is responsible for establishing and maintaining the connection with specific devices. It acts as a middleman between the server and the mobile devices.

## Project setup
In order to be able to run the `atmosphere-agent` project you need to:

* clone and publish the following projects to Maven locally (follow the links for more information):
  * [atmosphere-server-agent-lib](https://github.com/MusalaSoft/atmosphere-server-agent-lib)
  * [atmosphere-agent-device-lib](https://github.com/MusalaSoft/atmosphere-agent-device-lib)
  * [atmosphere-ime](https://github.com/MusalaSoft/atmosphere-ime)
  * [atmosphere-service](https://github.com/MusalaSoft/atmosphere-service)
  * [atmosphere-uiautomator-bridge](https://github.com/MusalaSoft/atmosphere-uiautomator-bridge)
* clone the [atmosphere-agent](https://github.com/MusalaSoft/atmosphere-agent) project

Once you have performed the steps mentioned above, in the `atmosphere-agent` project root directory open a terminal/command prompt and run the following commands:

* `./gradlew clean build` on Linux/macOS
* `gradlew clean build` on Windows

to build the project using the Gradle wrapper;

* `./gradlew run` on Linux/macOS
* `gradlew run` on Windows

to start the Agent. You will see something of the kind:
```
...
com.musala.atmosphere.agent.DeviceManager.<init>(DeviceManager.java:148) 05 Jul 2016 14:32:56 - Device manager created successfully.
com.musala.atmosphere.agent.AgentManager.<init>(AgentManager.java:104) 05 Jul 2016 14:32:56 - AgentManager created successfully.
...
com.musala.atmosphere.agent.devicewrapper.util.ondevicecomponent.DeviceRequestSender.request(DeviceRequestSender.java:148) 05 Jul 2016 14:33:08 - java.net.SocketException: Connection reset
com.musala.atmosphere.agent.DeviceManager.createWrapperForDevice(DeviceManager.java:342) 05 Jul 2016 14:33:15 - Created wrapper for device with bindingId = 015d4bdf293c1a11
```
Now you need to run `connect <yourServerIPAddress> <serverPort>`. By default the server port is `1980`. If the server is running on your local machine the command should look like this: `connect 127.0.0.1 1980`, or you may skip the IP address altogether: `connect 1980`. The agent and the server are connected successfully when you see something of the kind:

```
connect 127.0.0.1 1980
com.musala.atmosphere.agent.AgentManager.connectToServer(AgentManager.java:223) 05 Jul 2016 14:56:46 - Connection request sent to Server with address (127.0.0.1:1980)
>> com.musala.atmosphere.agent.AgentManager.registerServer(AgentManager.java:197) 05 Jul 2016 14:56:47 - Server with IP (10.0.10.76:1980) registered.
```
