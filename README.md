# Autotests Adb Server
## What is it?
When you are writing ui-tests you need do some actions with device sometimes. 
You may to execute a big part of those actions by [ADB](https://developer.android.com/studio/command-line/adb).
But Espresso doesn't contain a mechanism allowing Adb commands fulfilling although Appium does. <br> 
Why? In Espresso your app and tests are in device. In Appium your tests are in Desktop or Server. 
So in Espresso you can't call adb because you can't call adb from inside the device. <br>
That's why we have created **Autotests Adb Server** to compensate for the Espresso disadvantage. <br>
<br>
The main idea of the tool is so similar with idea of Appium. 
We need an ability to include in our tests some external thing from where we would send adb-commands to a device. 
This external thing can be Desktop or Server. <br>
That's why the tool is consist of two parts: **Desktop** and **Device**. <br>
**Desktop** - is the server listening commands from devices to execute adb-commands. <br>
**Device** - is the client sending commands to the server from your tests.

## Usage

#### Server/Desktop
You need to start the **Desktop** on your host (desktop/server) **before** tests' start to execute. <br>
To start the **Desktop** please copy built library from */artifacts/desktop.jar* to convenient for you place. <br>
The next is to execute a simple command in your host's cmd:
```
java -jar desktop.jar
```
Also, you can set additional options as it's shown in the example below:
```
java -jar desktop.jar emulators=emulator-5554,emulator-5556 adbServerPort=5041
```
where: <br>
emulators - you set a list of emulators that can be captured by desktop.jar <br>
adbServerPort - you set the adb server port number (the default value is 5037)

#### Device
1) To include built library */artifacts/device.jar* in your project (later it will be substituted by jcenter).
2) To give permissions for an access to the Internet 
```gradle
<uses-permission android:name="android.permission.INTERNET" />
``` 
3) To use the class:
```kotlin
object AdbTerminal {

    fun connect() { }

    fun disconnect() { }

    /**
     * Please first of all call [connect] method to establish a connection
     */
    fun executeAdb(command: String): CommandResult { }

    /**
     * Please first of all call [connect] method to establish a connection
     */
    fun executeCmd(command: String): CommandResult { }

}
```
As you are seeing you need to establish connection by ```connect``` method calling before to execute adb or simple cmd command. <br>
For a reminder: adb-command is also cmd command but it starts with ```adb``` key word. <br>
After the session please close the connection by ```disconnect``` method. <br>
```executeAdb``` and ```executeCmd``` are synchronous methods to not reorder a line of commands because if commands were completed in incorrect order it may to lead inconsistent state of the app and the device. <br>
Also these methods don't throw any exception. All possible results are mapping into ```CommandResult```. <br>
All methods of ```AdbTerminal``` may be call from any thread. <br>
So, please observe **example** module.

### Logs
Let's consider what the developer looks in the logs on the host and the device. <br>

#### The host logs
*desktop.jar* is running and waiting devices. But no one device exists in the current environment.
```
INFO:_____tag=MAIN______________________________________message => arguments: emulators=[], adbServerPort=null
INFO:_____tag=Desktop___________________________________method=startDevicesObserving___________________message => start
```

There has been new emulator in the current environment - ```emulator-5554```. <br>
The main moment is port forwarding. <br>
Next step is a WatchdogThread's start that is responsible to establish socket connection between the client (the host forwarded to device port) and the server (the device).
```
INFO:_____tag=Desktop_________________________________method=startDevicesObserving___________________message => New device has been found: emulator-5554. Initialize connection to it...
INFO:_____tag=DesktopDeviceSocketConnectionForwardImplmethod=getDesktopSocketLoad____________________message => calculated desktop client port=11866
INFO:_____tag=DesktopDeviceSocketConnectionForwardImplmethod=forwardPorts(fromPort=11866, toPort=8500)_message => started
INFO:_____tag=CommandExecutorImpl_____________________method=execute_________________________________message => adbCommand=adb -s emulator-5554 forward tcp:11866 tcp:8500
INFO:_____tag=DesktopDeviceSocketConnectionForwardImplmethod=forwardPorts(fromPort=11866, toPort=8500)_message => result=CommandResult(status=SUCCESS, description=exitCode=0, message=11866
)
INFO:_____tag=DesktopDeviceSocketConnectionForwardImplmethod=getDesktopSocketLoad____________________message => desktop client port=11866 is forwarding with device server port=8500
INFO:_____tag=DeviceMirror____________________________method=startConnectionToDevice_________________message => connect to device=emulator-5554 start
INFO:_____tag=DeviceMirror.WatchdogThread_____________method=run_____________________________________message => WatchdogThread is started from Desktop to Device=emulator-5554
```

Further, you can observer a lot of attempts to establish connection between the client and the server. <br>
It's right behavior. Don't worry.
```
INFO:_____tag=DeviceMirror.WatchdogThread_____________method=run_____________________________________message => Try to connect to Device=emulator-5554...
INFO:_____tag=ConnectionServerImplBySocket____________method=tryConnect______________________________message => start
INFO:_____tag=ConnectionMaker_________________________method=connect_________________________________message => start
INFO:_____tag=ConnectionMaker_________________________method=connect_________________________________message => current state=DISCONNECTED
INFO:_____tag=DesktopDeviceSocketConnectionForwardImplmethod=getDesktopSocketLoad____________________message => started with ip=127.0.0.1, port=11866
INFO:_____tag=DesktopDeviceSocketConnectionForwardImplmethod=getDesktopSocketLoad____________________message => completed with ip=127.0.0.1, port=11866
INFO:_____tag=ConnectionMaker_________________________method=connect_________________________________message => updated state=CONNECTED
INFO:_____tag=ConnectionServerImplBySocket____________method=tryConnect______________________________message => start handleMessages
INFO:_____tag=SocketMessagesTransferring______________method=startListening__________________________message => start
ERROR:____tag=SocketMessagesTransferring______________method=startListening__________________________message => java.io.EOFException
INFO:_____tag=ConnectionServerImplBySocket____________method=tryDisconnect___________________________message => start
INFO:_____tag=ConnectionMaker_________________________method=disconnect______________________________message => start
INFO:_____tag=ConnectionMaker_________________________method=disconnect______________________________message => current state=CONNECTED
INFO:_____tag=ConnectionMaker_________________________method=disconnect______________________________message => updated state=DISCONNECTING
INFO:_____tag=ConnectionMaker_________________________method=disconnect______________________________message => updated state=DISCONNECTED
INFO:_____tag=ConnectionServerImplBySocket____________method=tryDisconnect___________________________message => attempt completed
INFO:_____tag=ConnectionServerImplBySocket____________method=tryConnect______________________________message => attempt completed
INFO:_____tag=DeviceMirror.WatchdogThread_____________method=run_____________________________________message => Try to connect to Device=emulator-5554...
INFO:_____tag=ConnectionServerImplBySocket____________method=tryConnect______________________________message => start
INFO:_____tag=ConnectionMaker_________________________method=connect_________________________________message => start
INFO:_____tag=ConnectionMaker_________________________method=connect_________________________________message => current state=DISCONNECTED
INFO:_____tag=DesktopDeviceSocketConnectionForwardImplmethod=getDesktopSocketLoad____________________message => started with ip=127.0.0.1, port=11866
INFO:_____tag=DesktopDeviceSocketConnectionForwardImplmethod=getDesktopSocketLoad____________________message => completed with ip=127.0.0.1, port=11866
INFO:_____tag=ConnectionMaker_________________________method=connect_________________________________message => updated state=CONNECTED
INFO:_____tag=ConnectionServerImplBySocket____________method=tryConnect______________________________message => start handleMessages
INFO:_____tag=SocketMessagesTransferring______________method=startListening__________________________message => start
ERROR:____tag=SocketMessagesTransferring______________method=startListening__________________________message => java.io.EOFException
INFO:_____tag=ConnectionServerImplBySocket____________method=tryDisconnect___________________________message => start
INFO:_____tag=ConnectionMaker_________________________method=disconnect______________________________message => start
INFO:_____tag=ConnectionMaker_________________________method=disconnect______________________________message => current state=CONNECTED
INFO:_____tag=ConnectionMaker_________________________method=disconnect______________________________message => updated state=DISCONNECTING
INFO:_____tag=ConnectionMaker_________________________method=disconnect______________________________message => updated state=DISCONNECTED
INFO:_____tag=ConnectionServerImplBySocket____________method=tryDisconnect___________________________message => attempt completed
INFO:_____tag=ConnectionServerImplBySocket____________method=tryConnect______________________________message => attempt completed
```

When a developer runs the application containing *Device* part of AdbServer and starts this part then the connection is successfully established.
```
INFO:_____tag=DeviceMirror.WatchdogThread_____________method=run_____________________________________message => Try to connect to Device=emulator-5554...
INFO:_____tag=ConnectionServerImplBySocket____________method=tryConnect______________________________message => start
INFO:_____tag=ConnectionMaker_________________________method=connect_________________________________message => start
INFO:_____tag=ConnectionMaker_________________________method=connect_________________________________message => current state=DISCONNECTED
INFO:_____tag=DesktopDeviceSocketConnectionForwardImplmethod=getDesktopSocketLoad____________________message => started with ip=127.0.0.1, port=11866
INFO:_____tag=DesktopDeviceSocketConnectionForwardImplmethod=getDesktopSocketLoad____________________message => completed with ip=127.0.0.1, port=11866
INFO:_____tag=ConnectionMaker_________________________method=connect_________________________________message => updated state=CONNECTED
INFO:_____tag=ConnectionServerImplBySocket____________method=tryConnect______________________________message => start handleMessages
INFO:_____tag=SocketMessagesTransferring______________method=startListening__________________________message => start
INFO:_____tag=SocketMessagesTransferring______________method=startListening__________________________message => IO Streams were created
INFO:_____tag=ConnectionServerImplBySocket____________method=tryConnect______________________________message => attempt completed
INFO:_____tag=SocketMessagesTransferring.MessagesListeningThread_method=run_____________________________________message => start to work
```

And an example of one command execution:
```
INFO:_____tag=SocketMessagesTransferring.MessagesListeningThread_method=peekNextMessage_________________________message => with message=TaskMessage(command=AdbCommand(body=shell input text 1))
INFO:_____tag=ConnectionServerImplBySocket____________method=handleMessages__________________________message => received taskMessage=TaskMessage(command=AdbCommand(body=shell input text 1))
INFO:_____tag=CommandExecutorImpl_____________________method=execute_________________________________message => adbCommand=adb -s emulator-5554 shell input text 1
INFO:_____tag=ConnectionServerImplBySocket____________method=handleMessages.backgroundExecutor_______message => result of taskMessage=TaskMessage(command=AdbCommand(body=shell input text 1)) => result=CommandResult(status=SUCCESS, description=exitCode=0, message=)
INFO:_____tag=SocketMessagesTransferring______________method=sendMessage_____________________________message => where sendModel=ResultMessage(command=AdbCommand(body=shell input text 1), data=CommandResult(status=SUCCESS, description=exitCode=0, message=))
```

#### The device logs
The device with *Device* part is running and waiting host with working AdbServer (*Desktop* part). But no one host exists in the current environment.
```
INFO:_____tag=Device__________________________________method=start___________________________________message => start
INFO:_____tag=Device.WatchdogThread___________________method=run_____________________________________message => WatchdogThread starts from Device to Desktop
INFO:_____tag=Device.WatchdogThread___________________method=run_____________________________________message => Try to connect to Desktop...
INFO:_____tag=ConnectionClientImplBySocket____________method=tryConnect______________________________message => start
INFO:_____tag=ConnectionMaker_________________________method=connect_________________________________message => start
INFO:_____tag=ConnectionMaker_________________________method=connect_________________________________message => current state=DISCONNECTED
INFO:_____tag=DesktopDeviceSocketConnectionForwardImplmethod=getDeviceSocketLoad_____________________message => started
```

There has been appropriate host.
```
INFO:_____tag=DesktopDeviceSocketConnectionForwardImplmethod=getDeviceSocketLoad_____________________message => completed
INFO:_____tag=ConnectionMaker_________________________method=connect_________________________________message => updated state=CONNECTED
INFO:_____tag=ConnectionClientImplBySocket____________method=tryConnect______________________________message => start handleMessages
INFO:_____tag=SocketMessagesTransferring______________method=startListening__________________________message => start
INFO:_____tag=SocketMessagesTransferring______________method=startListening__________________________message => IO Streams were created
INFO:_____tag=ConnectionClientImplBySocket____________method=tryConnect______________________________message => attempt completed
INFO:_____tag=SocketMessagesTransferring.MessagesListeningThread_method=run_____________________________________message => start to work
```

And an example of one command execution:
```
INFO:_____tag=Device__________________________________method=execute_________________________________message => Start to execute the command=AdbCommand(body=shell input text 1)
INFO:_____tag=ConnectionClientImplBySocket____________method=executeAdbCommand_______________________message => started command=AdbCommand(body=shell input text 1)
INFO:_____tag=SocketMessagesTransferring______________method=sendMessage_____________________________message => where sendModel=TaskMessage(command=AdbCommand(body=shell input text 1))
INFO:_____tag=SocketMessagesTransferring.MessagesListeningThread_method=peekNextMessage_________________________message => with message=ResultMessage(command=AdbCommand(body=shell input text 1), data=CommandResult(status=SUCCESS, description=exitCode=0, message=))
INFO:_____tag=ConnectionClientImplBySocket____________method=handleMessages__________________________message => received resultMessage=ResultMessage(command=AdbCommand(body=shell input text 1), data=CommandResult(status=SUCCESS, description=exitCode=0, message=))
INFO:_____tag=ConnectionClientImplBySocket____________method=executeAdbCommand_______________________message => command=AdbCommand(body=shell input text 1) completed with commandResult=CommandResult(status=SUCCESS, description=exitCode=0, message=)
INFO:_____tag=Device__________________________________method=execute_________________________________message => The result of command=AdbCommand(body=shell input text 1) => CommandResult(status=SUCCESS, description=exitCode=0, message=)
```

## Integration

To use AdbServer device library, include the `jcenter` repository to your root `build.gradle` file (if it does not exist already):

```
allprojects {
    repositories {
        jcenter()
    }
}
```

And then add dependency to your module `build.gradle`:

```
implementation 'com.kaspersky.android-components:adbserver-device:1.0.0'
```

## Support
Russian support in telegram - t.me/kaspresso

## Contribution Policy
AdbServer is an open source project, and depends on its users to improve it. We are more than happy to find you interested in taking the project forward. <br>
Kindly refer to the [Contribution Guidelines](https://github.com/KasperskyLab/AdbServer/blob/master/CONTRIBUTING.md) for detailed information.

## License
AdbServer is open source and available under the [Apache License, Version 2.0](https://github.com/KasperskyLab/AdbServer/blob/master/LICENSE).
