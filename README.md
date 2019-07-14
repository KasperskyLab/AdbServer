# **Autotests Adb Server**
### **What is it?**
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

### **Usage**
#### Server/Desktop
You need to start the **Desktop** on your host (desktop/server) **before** tests' start to execute. <br>
To start the **Desktop** please copy built library from */artifacts/desktop.jar* to convenient for you place. <br>
The next is to execute a simple command in your host's cmd:
```
java -jar desktop.jar
```

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