[![Build Status](https://travis-ci.org/MusalaSoft/atmosphere-agent.svg?branch=master)](https://travis-ci.org/MusalaSoft/atmosphere-agent)
[![Download](https://api.bintray.com/packages/musala/atmosphere/atmosphere-agent/images/download.svg)](https://bintray.com/musala/atmosphere/atmosphere-agent/_latestVersion)  

See our site for better context of this readme. [Click here](http://atmosphereframework.com/)

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
07 II 2017 15:37:23 - Created wrapper for device with bindingId = YT9111897C
07 II 2017 15:37:23 - AgentManager created successfully.
07 II 2017 15:37:23 - Agent created on port: 1989
The Agent has started successfully.
>>
```
Now you need to run `connect <yourServerIPAddress> <serverPort>`. By default the server port is `1980`. If the server is running on your local machine the command should look like this: `connect 127.0.0.1 1980`, or you may skip the IP address altogether: `connect 1980`. The agent and the server are connected successfully when you see something of the kind:

```
>> connect 127.0.0.1 1980
07 II 2017 15:37:29 - Connection request sent to Server with address (127.0.0.1:1980)
>> 07 II 2017 15:37:30 - Server with IP (169.254.121.202:1980) registered.
```

## Exit from the Agent
To exit properly from the `Agent` use the `exit` command. Otherwise an instance of the `Agent` may still run.

```
>> exit
14 May 2018 11:24:50 - Closing the AgentManager.
14 May 2018 11:24:51 - Agent stopped successfully.
```
