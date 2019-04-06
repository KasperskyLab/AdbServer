# **Autotests Host Server**
### **Для чего нужен данный инструмент**
Часто при написании UI-тестов теруебтся выполнять ряд действий над устройством, которые невозможно выполнить средствами Espresso. Многие сценарии можно выполнить посредством исполнения команд [ADB](https://developer.android.com/studio/command-line/adb).
В Espresso нет механизма для исполнения таких команд, поэтому был разработан инструмент для **подключения к хост-машине** и исполнения adb-команд на ней.

### **Использование**
#### На хост-машине
Необходимо запустить клиент **до выполнения тестов**. Именно этот клиент будет исполнять adb-команды. Он представляет из себя jar-файл  **HostConnectionClient.jar**.
Перед стартом тестов необходимо стартовать его, выполнив команду
```
java -jar HostConnectionClient.jar
```
Данный джарник в уже собранном виде можно достать в проекте [Android.Autotests.SupportFiles](https://hqrndtfs.avp.ru/tfs/DefaultCollection/MobileProducts/_git/Android.Autotests.SupportFiles), 
а конкретнее, например в проекте KISA, лежит [здесь](https://hqrndtfs.avp.ru/tfs/DefaultCollection/MobileProducts/_git/Android.Autotests.SupportFiles?path=%2FHostConnectionClient.jar&version=GBkisa).
#### На девайсе
1) Выдать пермишены на доступ к интернету:
```java
<uses-permission android:name="android.permission.INTERNET" />
``` 	
2) Воспользоваться классом
```kotlin
object HostConnection {

    fun start() {...}

    fun stop() {...}

    @Throws(
        AdbException::class,
        IOException::class
    )
    fun executeAdbCommand(adbCommand: String): String {...}

    @Throws(
        CmdException::class,
        IOException::class
    )
    fun executeCmdCommand(cmdCommand: String): String {...}
    
}
```	
##### Описание методов класса HostConnection:

```kotlin
fun start() {...}
fun stop() {...}
```
Cлужат для инициализации/остановки подключения к хост-машине. <br/>
Можно вызывать из любого потока. Вызывать нужно до вызова *executeXXX()* методов. <br/>
<br/><br/>
```kotlin
@Throws(
    AdbException::class,
    IOException::class
)
fun executeAdbCommand(adbCommand: String): String {...}
```
Cлужит для выполнения adb-команды (при этом префикс"adb" в команде уже не требуется). <br/>
Возможные исключения: <br/>
*IOException* - в случае ошибки подключения <br/>
*AdbException* - в случае заверешния adb команды с ненулевым кодом возврата <br/>
<br/><br/>
```kotlin
@Throws(
    CmdException::class,
    IOException::class
)
fun executeCmdCommand(cmdCommand: String): String {...}
```
Cлужит для выполнения сmd-команды (командная строка хост-машины) <br/>
Возможные исключения: <br/>
*IOException* - в случае ошибки подключения <br/>
*CmdException* - в случае заверешния cmd команды с ненулевым кодом возврата <br/>
