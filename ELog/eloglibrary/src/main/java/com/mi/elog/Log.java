package com.mi.elog;

import android.content.Context;
import android.text.TextUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * 
 * @描述： 自定义日志类。
 *        1.具体日志功能
 *        	1.a输出日志到控制台
 *        	2.b输出日志到文件
 *        	3.针对wifi分机多App设计，支持多App，写入同一日志文件
 *        2.实现对各种日志功能开闭设置
 * 		    2.a是否使用日志功能
 * 			2.b在开启日志功能的前提下，是否使用输出日志到控制台
 * 			2.c在开启日志功能的前提下，是否使用输出日志到文件
 *          2.d在开启输出日志到文件的前提下，是否将日志级别为debug，info，error的日志单独输出到文件
 *        3.过滤日志输入到控制台及文件
 *        4.日志输出格式暂定默认为eclipse控制台格式
 * @author：Michelle_Hong 
 * @创建时间：2016-11-28上午8:29:10
 * @see
 */
public class Log {
	
	/**
     * Application的全局TAG
     */
    public static String GLOBAL_TAG = "";
    /**
     * 日志系统是否可用
     */
    protected static boolean isEnabled = true;

    /**
     * 是否输出日志到控制台
     */
    protected static boolean isLog2ConsoleEnabled = true;

    /**
     * 是否输出日志到文件
     */
    protected static boolean isLog2FileEnabled = true;

    /**
     * 磁盘容量不够时，写入日志前是否删除旧的日志文件
     */
    private static boolean isDeleteFileWhenStorageLack = true;
    /**
     * 当isDeleteFileWhenStorageLack为true时sdcard可以写入的最少容量
     */
    public static final long  limitFreeStorage = 1024*1024*1;
    
    /**
     * 是否将日志按照日志级别单独写入文件，（需要在日志输入文件可用的前提下可用）
     */
    protected static boolean isLog2LevelFileEnabled = false;

    
    private static FilePathGenerator generator_all = null;

    private static FilePathGenerator generator_debug = null;
 
    private static FilePathGenerator generator_info = null;
  
    private static FilePathGenerator generator_error = null;

    
    private static LogFormatter formatter = null;
   
    private static List<LogFilter> filters = null;

    private static Context context;

    public static void init(Context mcontext){
        context = mcontext;
    }


    public static boolean isLogInit(){
        if(context == null){
            return false;
        }else{
            return true;
        }
    }


    public static void setLogFileRootDirectory(String logFileDirectory){
        if(!TextUtils.isEmpty(logFileDirectory)){
            if(logFileDirectory.endsWith(File.separator)){
                FilePathGenerator.logDirRoot = logFileDirectory.substring(0,logFileDirectory.length()-1);
            }else{
                FilePathGenerator.logDirRoot = logFileDirectory;
            }
        }

    }


    public static String getLogFileRootDirectory(){
       return FilePathGenerator.logDirRoot;
    }

    /**
     * 获取异步任务执行对象ExecutorService
     *
     * @return the ExecutorService
     */
    public static ExecutorService getExecutor() {
        return Log2File.getExecutor();
    }

    /**
     * 设置异步任务执行对象ExecutorService
     *
     * @param executor the ExecutorService
     */
    public static void setExecutor(ExecutorService executor) {
        Log2File.setExecutor(executor);
        ConcurrentTotalFileSizeWLatch.setExecutor(executor);
    }

    /**
     * 释放资源
     */
    public static void releaseLogCompont(){
        ExecutorService executorService = getExecutor();
        if(executorService != null){
            executorService.shutdown();
        }
    }

    /**
     * 获取所有级别日志输出到的文件的路径
     *
     * @return path
     */
    public static String getCurrentPath_ALL() {
        if (generator_all == null) {
            return null;
        }

        return generator_all.getTargetpath();
    }

   

    /**
     * 获取文件生成对象 FilePathGenerator
     *
     * @return the FilePathGenerator
     */
    public static FilePathGenerator getFilePathGenerator_ALL() {
        return generator_all;
    }

    /**
     * 设置用于所有级别日志输出到指定文件的 文件生成对象 FilePathGenerator 
     *
     * @param generator the FilePathGenerator
     */
    public static void setFilePathGenerator_ALL(FilePathGenerator generator) {
        Log.generator_all = generator;
    }


    /**
     * 获取INFO级别日志文件路径生成器
     * @return
     */
    public static FilePathGenerator getFilePathGenerator_INFO() {
        return generator_info;
    }

    /**
     * 用于INFO级别的日志单独输出到指定文件的 文件生成对象
     * @param generator
     */
    public static void setFilePathGenerator_INFO(FilePathGenerator generator){
    	Log.generator_info = generator;
    }


    /**
     * 获取DEBUG级别日志文件路径生成器
     * @return
     */
    public static FilePathGenerator getFilePathGenerator_DEBUG() {
        return generator_debug;
    }
    
    /**
     * 用于DEBUG级别的日志单独输出到指定文件的 文件生成对象
     * @param generator
     */
    public static void setFilePathGenerator_DEBUG(FilePathGenerator generator){
    	Log.generator_debug = generator;
    }

    /**
     * 获取ERROR级别日志文件路径生成器
     * @return
     */
    public static FilePathGenerator getFilePathGenerator_ERROR() {
        return generator_error;
    }


    /**
     * 用于ERROR级别的日志单独输出到指定文件的 文件生成对象
     * @param generator
     */
    public static void setFilePathGenerator_ERROR(FilePathGenerator generator){
    	Log.generator_error = generator;
    }
    
    /**
     * 获取日志格式话对象LogFormatter
     *
     * @return
     */
    public static LogFormatter getLogFormatter() {
        return formatter;
    }

    /**
     * 设置日志格式化对象LogFormatter
     *
     * @param formatter
     */
    public static void setLogFormatter(LogFormatter formatter) {
        Log.formatter = formatter;
    }


    public static boolean isDeleteFileWhenStorageLack() {
        return isDeleteFileWhenStorageLack;
    }

    public static void setIsDeleteFileWhenStorageLack(boolean isDeleteFileWhenStorageLack) {
        Log.isDeleteFileWhenStorageLack = isDeleteFileWhenStorageLack;
    }

    /**
     * 添加输出日志的过滤器
     * 每种日志过滤器添加一个
     * @param filter 需要添加的过滤器对象
     * @return 返回true表示添加成功，如果返回fasle，则出现情况，过滤器为null，或者添加了相同类型的过滤器
     */
    public static boolean addLogFilter(LogFilter filter) {
        boolean ret = true;

        if (filter == null) {
            ret = false;
            return ret;
        }

        if (filters == null) {
            filters = new ArrayList<LogFilter>();
        }

        for (LogFilter f : filters) {
            if (filter.getClass().getName().equals(f.getClass().getName())) {
                ret = false;
                break;
            }
        }

        if (ret) {
            filters.add(filter);
        }

        return ret;
    }

    /**
     * 获取过滤器列表
     *
     * @return
     */
    public static List<LogFilter> getLogFilters() {
        return filters;
    }

    /**
     * 删除过滤器
     * @param filter
     */
    public static void removeLogFilter(LogFilter filter){
        if (filter == null || filters == null || filters.isEmpty()){
            return;
        }

        if (filters.contains(filter)){
            filters.remove(filter);
        }
    }

    /**
     * 删除所有日志过滤器
     *
     */
    public static void clearLogFilters(){
        if (filters == null || filters.isEmpty()){
            return;
        }

        filters.clear();
    }
 

    /**
     * 查看日志是否可用
     */
    public static boolean isEnabled() {
        return isEnabled;
    }

    /**
     * 设置日志可用与否，默认可用
     *
     * @param
     */
    public static void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    /**
     * 日志是否往控制台打印
     */
    public static boolean isLog2ConsoleEnabled() {
        return isLog2ConsoleEnabled;
    }

    /**
     * 日志是否输出到文件
     */
    public static void setLog2ConsoleEnabled(boolean enabled) {
        isLog2ConsoleEnabled = enabled;
    }

    
    public static boolean isLog2FileEnabled() {
        return isLog2FileEnabled;
    }

    /**
     * 设置日志是否输出到文件
     *
     * @param enabled whether to enable the log
     */
    public static void setLog2FileEnabled(boolean enabled) {
        isLog2FileEnabled = enabled;
    }
    
    
    /**
     * 日志是否按级别单独输出到不同的文件目录中
     * @return
     */
    public static boolean isLog2LevelFileEnabled() {
		return isLog2LevelFileEnabled;
	}

	/**
     * 设置是否将日志按照日志级别单独写入文件 （需要在日志输入文件可用的前提下可用）
     * @param isLog2LevelFileEnabled enable whether to enable to log Level file
     */
	public static void setLog2LevelFileEnabled(boolean isLog2LevelFileEnabled) {
		Log.isLog2LevelFileEnabled = isLog2LevelFileEnabled;
	}

	/**
     * （摘自SDK android.util.Log 如下注释）
     * Checks to see whether or not a log for the specified tag is loggable at the specified level.
     * The default level of any tag is set to INFO.
     * This means that any level above and including INFO will be logged.
     * Before you make any calls to a logging method you should check to see if your tag should be logged.
     *
     * @param tag   The tag to check
     * @param level The level to check
     * @return Whether or not that this is allowed to be logged.
     */
    public static boolean isLoggable(String tag, int level) {
        return android.util.Log.isLoggable(tag, level);
    }

    /**
     * （摘自SDK android.util.Log 如下注释）
     * Low-level logging call.
     *
     * @param priority The priority/type of this log message
     * @param tag      Used to identify the source of a log message. It usually identifies the class or activity where the log call occurs.
     * @param msg      The message you would like logged.
     * @return The number of bytes written.
     */
    public static int println(int priority, String tag, String msg) {
        return android.util.Log.println(priority, tag, msg);
    }

    /**
     * Handy function to get a loggable stack trace from a Throwable
     *
     * @param tr An exception to log
     * @return
     */
    public static String getStackTraceString(Throwable tr) {
        return android.util.Log.getStackTraceString(tr);
    }

    

    /**
     * 获取全局日志标签
     */
    public static String getGlobalTag() {
        return GLOBAL_TAG;
    }

    /**
     * 设置全局日志标签
     *
     * @param
     */
    public static void setGlobalTag(String tag) {
        GLOBAL_TAG = tag;
    }

    /**
     * (摘自SDK andrid.util.log注释)
     * Send a Debug log message.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static void d(String tag, String msg) {
        log(LEVEL.DEBUG, tag, msg, null);
    }

    /**
     * Send a DEBUG log message.
     */
    public static void d(String msg) {
        log(LEVEL.DEBUG, null, msg, null);
    }

    /**
     * Send a DEBUG log message and log the exception.
     *
     * @param msg The message you would like logged.
     * @param thr An exception to log
     */
    public static void d(String tag, String msg, Throwable thr) {
        log(LEVEL.DEBUG, tag, msg, thr);
    }

    /**
     * Send a DEBUG log message and log the exception.
     *
     * @param msg The message you would like logged.
     * @param thr An exception to log
     */
    public static void d(String msg, Throwable thr) {
        log(LEVEL.DEBUG, null, msg, thr);
    }

    /**
     * Send a ERROR log message.
     *
     * @param msg The message you would like logged.
     */
    public static void e(String tag, String msg) {
        log(LEVEL.ERROR, tag, msg, null);
    }

    /**
     * Send an ERROR log message.
     *
     * @param msg The message you would like logged.
     */
    public static void e(String msg) {
        log(LEVEL.ERROR, null, msg, null);
    }

    /**
     * Send a ERROR log message and log the exception.
     *
     * @param msg The message you would like logged.
     * @param thr An exception to log
     */
    public static void e(String tag, String msg, Throwable thr) {
        log(LEVEL.ERROR, tag, msg, thr);
    }

    /**
     * Send an ERROR log message and log the exception.
     *
     * @param msg The message you would like logged.
     * @param thr An exception to log
     */
    public static void e(String msg, Throwable thr) {
        log(LEVEL.ERROR, null, msg, thr);
    }

    /**
     * Send a INFO log message.
     *
     * @param msg The message you would like logged.
     */
    public static void i(String tag, String msg) {
        log(LEVEL.INFO, tag, msg, null);
    }

    /**
     * Send an INFO log message.
     *
     * @param msg The message you would like logged.
     */
    public static void i(String msg) {
        log(LEVEL.INFO, null, msg, null);
    }

    /**
     * Send a INFO log message and log the exception.
     *
     * @param msg The message you would like logged.
     * @param thr An exception to log
     */
    public static void i(String tag, String msg, Throwable thr) {
        log(LEVEL.INFO, tag, msg, thr);
    }

    /**
     * Send a INFO log message and log the exception.
     *
     * @param msg The message you would like logged.
     * @param thr An exception to log
     */
    public static void i(String msg, Throwable thr) {
        log(LEVEL.INFO, null, msg, thr);
    }

    /**
     * Send a VERBOSE log message.
     *
     * @param msg The message you would like logged.
     */
    public static void v(String tag, String msg) {
        log(LEVEL.VERBOSE, tag, msg, null);
    }

    /**
     * Send a VERBOSE log message.
     *
     * @param msg The message you would like logged.
     */
    public static void v(String msg) {
        log(LEVEL.VERBOSE, null, msg, null);
    }

    /**
     * Send a VERBOSE log message and log the exception.
     *
     * @param msg The message you would like logged.
     * @param thr An exception to log
     */
    public static void v(String tag, String msg, Throwable thr) {
        log(LEVEL.VERBOSE, tag, msg, thr);
    }

    /**
     * Send a VERBOSE log message and log the exception.
     *
     * @param msg The message you would like logged.
     * @param thr An exception to log
     */
    public static void v(String msg, Throwable thr) {
        log(LEVEL.VERBOSE, null, msg, thr);
    }

    /**
     * Send an empty WARN log message and log the exception.
     *
     * @param thr An exception to log
     */
    public static void w(Throwable thr) {
        log(LEVEL.WARN, null, null, thr);
    }

    /**
     * Send a WARN log message.
     *
     * @param msg The message you would like logged.
     */
    public static void w(String tag, String msg) {
        log(LEVEL.WARN, tag, msg, null);
    }

    /**
     * Send a WARN log message
     *
     * @param msg The message you would like logged.
     */
    public static void w(String msg) {
        log(LEVEL.WARN, null, msg, null);
    }

    /**
     * Send a WARN log message and log the exception.
     *
     * @param msg The message you would like logged.
     * @param thr An exception to log
     */
    public static void w(String tag, String msg, Throwable thr) {
        log(LEVEL.WARN, tag, msg, thr);
    }

    /**
     * Send a WARN log message and log the exception.
     *
     * @param msg The message you would like logged.
     * @param thr An exception to log
     */
    public static void w(String msg, Throwable thr) {
        log(LEVEL.WARN, null, msg, thr);
    }

    /**
     * Send an empty What a Terrible Failure log message and log the exception.
     *
     * @param thr An exception to log
     */
    public static void wtf(Throwable thr) {
        log(LEVEL.ASSERT, null, null, thr);
    }

   
    /**
     * What a Terrible Failure: Report a condition that should never happen.
     * The error will always be logged at level ASSERT with the call stack.
     * Depending on system configuration, a report may be added to the
     * {@link android.os.DropBoxManager} and/or the process may be terminated
     * immediately with an error dialog.
     * @param tag Used to identify the source of a log message.
     * @param msg The message you would like logged.
     */
    public static void wtf(String tag, String msg) {
        log(LEVEL.ASSERT, tag, msg, null);
    }

    /**
     * Send a What a Terrible Failure log message
     *
     * @param msg The message you would like logged.
     */
    public static void wtf(String msg) {
        log(LEVEL.ASSERT, null, msg, null);
    }

    /**
     * Send a What a Terrible Failure log message and log the exception.
     *
     * @param msg The message you would like logged.
     * @param thr An exception to log
     */
    public static void wtf(String tag, String msg, Throwable thr) {
        log(LEVEL.ASSERT, tag, msg, thr);
    }

    /**
     * Send a What a Terrible Failure log message and log the exception.
     *
     * @param msg The message you would like logged.
     * @param thr An exception to log
     */
    public static void wtf(String msg, Throwable thr) {
        log(LEVEL.ASSERT, null, msg, thr);
    }


    /**
     * 删除“特定日期”之前的日志文件,例如删除2016年1月2日之前的日志文件则参数分别传入
     * 2016 1 2
     * @param year  特定日期的文本年eg：  2016
     * @param month 特定日期文本月 eg：1
     * @param day   特定日期文本日 eg：2
     */
	public static void deleteLogFiles(int year,int month,int day){
        if(isLogInit()){
            ClearLogFilesService.startDeleteLogFilesByLimitDate(context,year,month,day);
        }else{
            android.util.Log.e("LOG","未初始化日志生成模块");
        }
    }


    /**
	 * 日志级别LEVEL定义
	 * @author Michelle_Hong
	 * @version [2015.05.28]
	 *
	 */
	public enum LEVEL{
        VERBOSE(2, "V"),
        DEBUG(3, "D"),
        INFO(4, "I"),
        WARN(5, "W"),
        ERROR(6, "E"),
        ASSERT(7, "A");

        final String levelString;
        final int level;

        private LEVEL() {
            throw new AssertionError();
        }

        private LEVEL(int level, String levelString) {
            this.level = level;
            this.levelString = levelString;
        }

        public String getLevelString() {
            return this.levelString;
        }

        public int getLevel() {
            return this.level;
        }
    }

    private Log() {
        throw new AssertionError();
    }


    private static void log(LEVEL level, String tag, String msg, Throwable tr) {
        if (!isEnabled) {
            return;
        }

        String curTag = getCurrentTag(tag);

        if (isLog2ConsoleEnabled) {
//            android.util.Log.d("doLog","console........");
            log2Console(level, curTag, msg, tr);
        }

        if (isLog2FileEnabled) {
//            android.util.Log.d("doLog","file........");
            log2File(level, curTag, msg, tr);
        }
    }


    private static String getCurrentTag(String tag) {
        if (!TextUtils.isEmpty(tag)) {
            return tag;
        }

        if (!TextUtils.isEmpty(GLOBAL_TAG)) {
            return GLOBAL_TAG;
        }

        StackTraceElement[] stacks = Thread.currentThread().getStackTrace();
        if (stacks.length >= 4) {
            return stacks[3].getClassName();
        }

        return null;
    }


    protected static void log2Console(LEVEL level, String tag, String msg, Throwable thr) {
        switch (level) {
            case VERBOSE:
                if (thr == null) {
                    android.util.Log.v(tag, msg);
                } else {
                    android.util.Log.v(tag, msg, thr);
                }
                break;
            case DEBUG:
                if (thr == null) {
                    android.util.Log.d(tag, msg);
                } else {
                    android.util.Log.d(tag, msg, thr);
                }
                break;
            case INFO:
                if (thr == null) {
                    android.util.Log.i(tag, msg);
                } else {
                    android.util.Log.i(tag, msg, thr);
                }
                break;
            case WARN:
                if (thr == null) {
                    android.util.Log.w(tag, msg);
                } else if (TextUtils.isEmpty(msg)) {
                    android.util.Log.w(tag, thr);
                } else {
                    android.util.Log.w(tag, msg, thr);
                }
                break;
            case ERROR:
                if (thr == null) {
                    android.util.Log.e(tag, msg);
                } else {
                    android.util.Log.e(tag, msg, thr);
                }
                break;
            case ASSERT:
                if (thr == null) {
                    android.util.Log.wtf(tag, msg);
                } else if (TextUtils.isEmpty(msg)) {
                    android.util.Log.wtf(tag, thr);
                } else {
                    android.util.Log.wtf(tag, msg, thr);
                }
                break;
            default:
                break;
        }
    }


    private static void log2File(final LEVEL level, final String tag, final String msg, final Throwable tr) {
        if (getExecutor() == null) {
            setExecutor(Executors.newFixedThreadPool(5));
        }

        getExecutor().submit(new Runnable() {
            @Override
            public void run() {
                if (generator_all == null) {
                    //            generator = new FilePathGenerator.DefaultFilePathGenerator("","","");
                    generator_all = new FilePathGenerator.DefaultFilePathGenerator(context, "", FilePathGenerator.LOGINALLFOLDER, "", "");
                }
                if (formatter == null) {
                    formatter = new LogFormatter.ConsoleFormatter();
                }

                boolean isFilter = false;  //过滤 表示不过滤

                if (filters != null) {
                    for (LogFilter f : filters) {
                        if (f.filter(level, tag, msg)) {
                            isFilter = true;
                            break;
                        }
                    }
                }

                if (!isFilter && !TextUtils.isEmpty(generator_all.getTargetpath())) {
                    //if this tag is true，then delete Log files when lack of storage
                    if(isDeleteFileWhenStorageLack){

                        if (ConcurrentTotalFileSizeWLatch.getSDFreeSize() < limitFreeStorage){  //then delete the log files
                            ClearLogFilesService.startClearLogFiles(context);
                        }
                    }
                    Log2File.log2file(generator_all.getTargetpath(), formatter.format(level, tag, msg, tr));
                    if (isLog2LevelFileEnabled) {
                        switch (level) {
                            case ERROR:
                                if (generator_error == null) {
                                    generator_error = new FilePathGenerator.DefaultFilePathGenerator(context, "", FilePathGenerator.LOGERRORFOLDER, "", "");
                                }
                                Log2File.log2file(generator_error.getTargetpath(), formatter.format(level, tag, msg, tr));
                                break;
                            case DEBUG:
                                if (generator_debug == null) {
                                    generator_debug = new FilePathGenerator.DefaultFilePathGenerator(context, "", FilePathGenerator.LOGDEBUGFOLDER, "", "");
                                }
                                Log2File.log2file(generator_debug.getTargetpath(), formatter.format(level, tag, msg, tr));
                                break;
                            case INFO:
                                if (generator_info == null) {
                                    generator_info = new FilePathGenerator.DefaultFilePathGenerator(context, "", FilePathGenerator.LOGINFOFOLDER, "", "");
                                }
                                Log2File.log2file(generator_info.getTargetpath(), formatter.format(level, tag, msg, tr));
                                break;
                            default:
                                break;
                        }
                    }
                }
            }
        });

    }
}
