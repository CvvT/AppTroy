# AppTroy
=========
An Online Analysis System for Packed Android Malware.
AppTroy is a generic and fine-grained system for malware analysis, which is convenient for analysts to unpack, deobfuscate and monitor system API. With the functionalities it provides, even an inexperienced analyst is able to better understand the internal logic of the code, because our system does not require knowledge of the packer.

## Build
--------
AppTroy is compatible with different versions and even ROMS, but you have to modify one thing in the source code to make it work on your Android system. It's recommended that the target Android system should be <= 4.4.2 because yet I haven't tested it with ART runtime environment. 

You need to adapt the `offset` in /jni/dump.cpp file at line 13(`#define OFFSET 796;`). The value is the offset from gDvm to its member variable useDexFiles, which can be accessed through reverse engineering(Note: the `dvmInternalNativeShutdownv()` method may be the one you should inspect.). Of course, all these manual work can be done in script. The source code is in example/ directory but I find it hard to use :(

## Install
----------
Make sure you have read everything above.

```
	git clone https://github.com/CvvT/AppTroy.git
	cd AppTroy
	ndk-build
	import this project into Android Studio and build it

	install Xposed framework
	install AppTroy

	adb push /libs/armeabi/libapptroy.so /system/libs/
	adb push /libs/armeabi/libluajava.so /system/libs/

	remember enable the AppTroy module in Xposed installer application and reboot the system
```
[Xposed Framework](http://www.repo.xposed.info/module/de.robv.android.xposed.installer)

## Usage
--------
Simply using Log.d() to output all information(`adb logcat -s cc` to see).
If you want to change the log behavior, the source code is in file /com/cc/apptroy/util/Logger.java

### API monitor
Monitor most of sensitive API.
### Unpacking
1. start the target application
2. show DexFiles

	`am broadcast -a com.cc.dumpapk --es package your/package/name --es cmd '{"action":"show"}'`
3. select one DexFile: Check the output
4. Dump DexFile

	`am broadcast -a com.cc.dumpapk --es package your/package/name --es cmd '{"action":"dump", "cookie": the_value_selected_from_previous_step}'`

	When it's done, all smali files will be stored in /data/data/package/of/target/app/files/smali/
5. Update Class/Method: when you failed to get the real content of one class or method

	`am broadcast -a com.cc.dumpapk --es package your/package/name --es cmd '{"action":"update", "filepath":"/path/to/the/config/file"}'`

	Here is a sample of config file.

```
update.json template
{
	"class": ["com.cc.test.MainActivity", ],
	"method": [
		{"clsName": "", "mthName": "", "signature": ""},
	]
}
```
Alternative command:

`am broadcast -a com.cc.dumpapk --es package your/package/name --es cmd '{"action":"update_cls", "clsName":"class type descriptor"}`

`am broadcast -a com.cc.dumpapk --es package your/package/name --es cmd '{"action":"update_mth", "clsName":"type descriptor", "mthName":"method name", "signature":"method signature"}'`

### Lua script (See [AndroLua](https://github.com/mkottman/AndroLua))
`am broadcast -a com.cc.dumpapk --es package your/package/name --es cmd '{"action":"invoke", "filepath":"/path/to/the/lua/script"}'`

OR

`am broadcast -a com.cc.dumpapk --es package your/package/name --es cmd '{"action":"update", "lua":"content of lua script"}'`

Five convenient functions:

+ *luajava.newInstance(className, ...)*: This function creates a new Java object, and returns a Lua object that is a reference to the actual Java object. You can access this object with the regular syntax used to access object oriented functions in Lua objects. The first parameter is the name of the class to be instantiated. The other parameters are passed to the Java Class constructor.

+ *bind(className)*: This function retrieves a Java class corresponding to className. The returned object can be used to access static fields and methods of the corresponding class.

+ *luajava.new(javaClass)*: This function receives a java.lang.Class and returns a new instance of this class. {\itshape New} works just like newInstance, but the first argument is an instance of the class.

+ *luajava.createProxy(interfaceNames, luaObject)*: We can also, instead of creating a Java object to be manipulated by Lua, create a Lua object that will be manipulated by Java. We can do that in LuaJava by creating a proxy to that object. This is done by the {\itshape createProxy} function. The function returns a java Object reference that can be used as an implementation of the given interface. CreateProxy receives a string that contain the names of the interfaces to be implemented, separated by a comma(,), and a lua object that is the interface implementation.

+ *log(string)*: The function receives a string and records it in the log.

## Samples
## Question
If you have any trouble, you can contact me via email(weitengchencc@gmail.com).
