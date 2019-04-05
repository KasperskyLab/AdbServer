#**Autotests Host Server**
### **Для чего нужен данный инструмент**
Часто при написании UI-тестов теруебтся выполнять ряд действий над устройством,  которые невозможно выполнить средствами Espresso. Многие сценарии можно выполнить посредством исполнения команд ADB(https://developer.android.com/studio/command-line/adb)

В Espresso нет механизма для исполнения таких команд, поэтому был разработан инструмент для** подключения к хост-машине** и исполнения adb-команд на ней.

### **Использование**
######На хост-машине
необходимо запустить клиент **до выполнения тестов**. Именно этот клиент будет исполнять adb-команды. Он представляет из себя jar-файл  **HostConnectionClient.jar**
Перед стартом тестов необходимо стартовать его, выполнив команду

	java -jar HostConnectionClient.jar

######На девайсе
1) Выдать пермишены на доступ к интернету:

	<uses-permission android:name="android.permission.INTERNET" />
2) Воспользоваться классом

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
------------
- Методы :

		fun start() {...}
		fun stop() {...}
служат для инициализации/остановки подключения к хост-машине.
Можно вызывать из любого потока. Вызывать нужно до вызова *performXXX()*
методов
------------
- Метод:

			@Throws(
					AdbException::class,
					IOException::class
			)
			fun executeAdbCommand(adbCommand: String): String {...}
служит для выполнения adb-команды(при этом префикс"adb" в команде уже не требуется).
Возможные исключения:
*IOException* - в случае ошибки подключения
*AdbException* - в случае заверешния adb команды с ненулевым кодом возврата

------------
- Метод:

				@Throws(
						CmdException::class,
						IOException::class
				)
				fun executeCmdCommand(cmdCommand: String): String {...}
служит для выполнения сmd-команды(командная строка хост-машины)
Возможные исключения:
*IOException* - в случае ошибки подключения
*CmdException* - в случае заверешния cmd команды с ненулевым кодом возврата