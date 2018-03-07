# Yarward日志生成模块

根据项目中遇到的需求，多进程都要写日志，且不同进程的将日志写到一起，进行统一管理。 *因此本次日志生成模块，支持多应用共写同一日志文件，日志生成已改为跨进程安全的。**但请注意，若多应用同写一个文件，请在配置时，配置相同的日志文件根目录和文件目录生成器，且文件名缓存使用统一缓存***

## 功能介绍
日志生成模块主要为      
1. 为应用程序提供写日志的接口，以及相关配置接口。        
2. 本模块可以将日志输入到文件和控制台。
3. 对于日志的输出，提过日志过滤，对于日志过滤部分，默认提供标签过滤器，日志级别过滤器，内容过滤器。程序结构符合开闭原则，可自定义过滤器。
4. 对于日志文件的输入文件，默认提供了一个路径生成器，该生成将多APP的日志，输出至同一文件。程序结构符合开闭原则，可自定义路径生成器。
5. 提供日志文件的清理接口
6. 写日志接口重写了android SDK API中的日志操作方法。（想使用本模块日志生成，仅需要更换包名即可）
7. 日志输出格式，默认日志输出格式为“Log console”日志输出格式。程序结构符合闭合原则，可定义。

## 快速使用
本日志生成模块，对外统一提供Log操作类。

**需要增加权限**
```
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"></uses-permission>
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"></uses-permission>
```
**注册Service**
```
   <service android:name="com.yarward.lib.log.ClearLogFilesService"
            android:enabled="true"></service>
```

* Step1：使用前需要进行必要配置，例如Context，建议在Application中配置
    ``` 
    Log.init(sApplication);
    ```
    以及其他的配置，例如是否写入文件，是否按日志级别写入不同文件夹，设置过滤器等。具体设置可根据实际需求参考下文。
* Step2: 写日志。       
    对外提供接口于android SDK API中相同。可以使用如下接口来写不同级别的日志。
    ```
    public static void d(String tag, String msg)
    public static void d(String msg)
    public static void d(String tag, String msg, Throwable thr)
    public static void d(String msg, Throwable thr)
    ```

    ```
    public static void e(String tag, String msg)
    public static void e(String tag, String msg)
    public static void e(String msg)
    public static void e(String tag, String msg, Throwable thr)
    public static void e(String msg, Throwable thr)
    ```
    ```
    public static void i(String tag, String msg)
    public static void i(String msg)
    public static void i(String tag, String msg, Throwable thr)
    public static void i(String msg, Throwable thr)
    ```
    ```
    public static void v(String tag, String msg)
    public static void v(String msg)
    public static void v(String tag, String msg, Throwable thr)
    public static void v(String msg, Throwable thr)
    ```
    ```
    public static void w(Throwable thr)
    public static void w(String tag, String msg)
    public static void w(String msg)
    public static void w(String tag, String msg, Throwable thr)
    public static void w(String msg, Throwable thr)
    ```
    ```
    public static void wtf(Throwable thr)
    public static void wtf(String tag, String msg)
    public static void wtf(String msg)
    public static void wtf(String tag, String msg, Throwable thr)
    public static void wtf(String msg, Throwable thr)
    ```
    ```
    public static boolean isLoggable(String tag, int level) 
    public static int println(int priority, String tag, String msg) 
    public static String getStackTraceString(Throwable tr)
    ```


## 其他类


| 类                 | 说明           |
| ----------------- | ------------ |
| FilePathGenerator | 日志文件路径生成器    |
| LogFilter         | 日志文件过滤器      |
| LogFormatter      | 输入文件的日志的输出格式 |


​    
## 其他可配置项

| 配置接口                                     | 说明                                       | 备注                                       |
| ---------------------------------------- | ---------------------------------------- | ---------------------------------------- |
| public static void init(Context mcontext) | 初始化                                      |                                          |
| public static boolean isLogInit()        | 是否已经初始化                                  |                                          |
| public static void setLogFileRootDirectory(String logFileDirectory) | 设置日志生成的根目录                               | 注意：如果程序中设置了自定义的文件路径生成器，则日志文件将写入到文件路径生成器中的根目录 |
| public static void setEnabled(boolean enabled) | 设置日志生成模块是否可用，默认可用                        |                                          |
| public static boolean isEnabled()        | 查看日志生成模块是否可用                             |                                          |
| public static void setLog2ConsoleEnabled(boolean enabled) | 设置日志是否可以输出到控制台，默认可以                      |                                          |
| public static boolean isLog2ConsoleEnabled() | 查看日志是否可以输出到控制台                           |                                          |
| public static void setLog2FileEnabled(boolean enabled) | 设置日志是否输出到文件中，默认可以                        |                                          |
| public static boolean isLog2FileEnabled() | 查看日志是否可以输出到文件中                           |                                          |
| public static void setLog2LevelFileEnabled | 设置日志是否按级别单独写入到文件中，默认为false               | 如果设置为true，则再日志文件的根目录下，自动生成all_level，error_level，debug_level，info_level等文件夹存放不同级别的日志文件；否则日志文件的根目录下，只有all_level一文件夹 |
| public static String getLogFileRootDirectory() | 获取日志文件的根目录                               |                                          |
| public static String getGlobalTag()      | 获取全局日志标签                                 |                                          |
| public static void setGlobalTag(String tag) | 设置全局日志标签                                 |                                          |
| public static void setExecutor(ExecutorService executor) | 设置写日志的异步执行对象                             |                                          |
| public static ExecutorService getExecutor() | 获取写日志的异步执行对象                             |                                          |
| public static void setFilePathGenerator_ALL(FilePathGenerator generator) | 设置用于所有级别日志输出到指定文件的文件路径生成器                |                                          |
| public static FilePathGenerator getFilePathGenerator_ALL() | 获取用于所有级别日志输出到指定文件的文件路径生成器                |                                          |
| public static void setFilePathGenerator_INFO(FilePathGenerator generator) | 设置用于INFO级别日志输出到指定文件的文件路径生成器              |                                          |
| public static FilePathGenerator getFilePathGenerator_INFO() | 获取用于INFO级别日志输出到指定文件的文件路径生成器              |                                          |
| public static void setFilePathGenerator_DEBUG(FilePathGenerator generator) | 设置用于DEBUG级别日志输出到指定文件的文件路径生成器             |                                          |
| public static FilePathGenerator getFilePathGenerator_DEBUG() | 获取用于DEBUG级别日志输出到指定文件的文件路径生成器             |                                          |
| public static void setFilePathGenerator_ERROR(FilePathGenerator generator) | 设置用于ERROR级别日志输出到指定文件的文件路径生成器             |                                          |
| public static FilePathGenerator getFilePathGenerator_ERROR() | 获取用于ERROR级别日志输出到指定文件的文件路径生成器             |                                          |
| public static void setLogFormatter(LogFormatter formatter) | 设置日志格式化对象                                |                                          |
| public static boolean addLogFilter(LogFilter filter) | 添加输出日志过滤器                                |                                          |
| public static List<LogFilter> getLogFilters() | 获取日志过滤器列表                                |                                          |
| public static void removeLogFilter(LogFilter filter) | 删除日志过滤器                                  |                                          |
| public static void clearLogFilters()     | 清空日志过滤器                                  |                                          |
| public static void setIsDeleteFileWhenStorageLack(boolean isDeleteFileWhenStorageLack) | 设置当磁盘容量不够时，写日志时，是否进行日志删除的操作，删除规则见下文自动删除规则 |                                          |
|  public static void setIsProcessersLog2SameFile(boolean isProcessersLog2SameFile)| 当使用默认路径生成器时，设置进该属性为true，多进程日志文件名使用统一缓存 |                                          |



##### 自动删除的规则：
默认配置：日志文件总量设置为3G，日志文件保存最长天数60天，单个日志文件的最大容量为10M。     

删除条件为：        
1. 日志文件的总量超过3G，或者日志文件最早生成的日志保存天数超过了60天，或者sdcard的空闲容量小于当个日志文件最大容量10M。      


删除文件：      
1. 会删除保存了60天的所有日志文件。        
2. 按照日志文件的日期来由早到晚的删除，直到(日志文件总容量小于我们设置的最大日志容量减去一个文件的最大容量)，且存储剩余容量大于10M。

## 日志文件的生成
如果使用时不定义FilePathGenerator，使用默认的，且设置日志需要根据日志级别写入不同的日志文件。那么在日志文件的根目录下则会有四个不同的日志文件夹。       
- *all_level* : 写入所有Level日志的日志文件存放的文件夹
- *debug_level*:只写入Debug Level日志的日志文件存放的文件夹
- *info_level*:只写入Info Level日志的日志文件存放的文件夹
- *error_level*:只写入Error Level日志的日志文件存放的文件夹

 Note：默认的FilePathGernerator有以下几个特， 

1.多进程共同写入同一日志文件内。  **NOTE:多应用时，注意配置日志文件的生成路径要一致。即 public static void setLogFileRootDirectory(String logFileDirectory) 或者设置的FilePathGenerator**    

2.每个日志文件的大小上限为10M，日志文件按照日期生成，日志文件的命名格式为yhlog-[yymmdd]-[index].log；如果当天日志超过10M，则依次生成另外的当天的日志文件index递增。     

3.设备重启，不会影响日志写入的日志文件。    

4.如果设备在运行中修改了日期，日志则会写入新日期的日志文件中。 


## 日志文件的删除

### 方式1-在需要删除的时候调用接口删除

系统提供删除日志文件的接口。
* 删除特定日期之前的已生成的所有日志文件
```
        /**
     * 删除“特定日期”之前的日志文件
     * @param year 
     * @param month
     * @param day
     */
	public static void deleteLogFiles(int year,int month,int day)
```

使用方法：
```
Log.deleteLogFiles(2012,12,23);
```

### 方式2-注册广播
该中方式，日志文件的删除规则由本模块确定，用户只需注册广播ClearLogFilesBroadcastReceiver，并在相应删除条件下发送广播，或者使用系统广播即可。        

该种方式的删除规则如下定义，      
默认配置：日志文件总量设置为3G，日志文件保存最长天数60天，单个日志文件的最大容量为10M。     

删除条件为：        
1. 日志文件的总量超过3G，或者日志文件最早生成的日志保存天数超过了60天，或者sdcard的空闲容量小于当个日志文件最大容量10M。      


删除文件：      
1. 会删除保存了60天的所有日志文件。        
2. 按照日志文件的日期来由早到晚的删除，直到(日志文件总容量小于我们设置的最大日志容量减去一个文件的最大容量)，且存储剩余容量大于10M。


## 日志文件的格式
本模块提供三个日志格式：
* ` ConsoleFormatter ` ： 格式如控制台日志格式
* ` ConsoleFormatterWithPackage` ：再控制台格式的基础上，增加了包名的展示。适用于多应用写同一文件
* ` DefaultFormatter `： 最简洁的日志格式

默认使用的ConsoleFormatter，可以通过 ` public static void setLogFormatter(LogFormatter formatter)`进行设置。



# 多应用同写日志使用范例

示例中创建了两个Module，分别再两个module的Application类中做Log的相关配置。**注意:如果写同一文件路径设置一定要一致**


Module1的Application类，代码：
```



/**
 * Created by MichelleHong on 2016/11/29 0029.
 *
 * @des
 */

public class TestAppApplication extends Application {


    private TestAppApplication sApplication;
    private String logFileRootDirectory = Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator+"testlog";

    @Override
    public void onCreate() {
        super.onCreate();
        sApplication = this;
        //初始化上下文
        Log.init(sApplication);
        //设置日志文件的根目录
        Log.setLogFileRootDirectory(logFileRootDirectory);
        //使用公共缓存
        Log.setIsProcessersLog2SameFile(true);
        //设置是否按日志级别写入不同文件
        Log.setLog2LevelFileEnabled(true);
        //设置日志输出格式
        Log.setLogFormatter(new LogFormatter.ConsoleFormatterWithPackage(sApplication));
        //设置全局TAG
        Log.setGlobalTag("TESTAPP1");
    }
}
```


Module2的Application类，代码如下：
```


/**
 * Created by MichelleHong on 2016/11/29 0029.
 *
 * @des
 */

public class TestAppApplication extends Application {

    private TestAppApplication sApplication;
    private String logFileRootDirectory = Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator+"testlog";

    @Override
    public void onCreate() {
        super.onCreate();
        sApplication = this;
        Log.init(sApplication);
        Log.setLogFileRootDirectory(logFileRootDirectory);
        Log.setIsProcessersLog2SameFile(true);
        Log.setLog2LevelFileEnabled(true);
        Log.setLogFormatter(new LogFormatter.ConsoleFormatterWithPackage(sApplication));
        Log.setGlobalTag("TESTAPP2");
    }
}
```


写日志时，根据实际需求调用相应接口即可。

