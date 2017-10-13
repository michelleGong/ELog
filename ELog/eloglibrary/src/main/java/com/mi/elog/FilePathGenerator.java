package com.mi.elog;

import android.content.Context;
import android.os.Environment;
import android.os.SystemClock;
import android.text.TextUtils;

import com.yarward.lib.log.acache.ACache;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 日志文件的生成器
 *
 * @描述：该类根据需求的不同定义各种日志文件的存储策略
 * @author：Michelle_Hong
 * @see
 */
public abstract class FilePathGenerator {

    public final String KEY_CURRENT_FILE_PATH = "current_log_file_path";
    public final String KEY_ALL_FILE_NAME = "all_level_file_name";
    public final String KEY_INFO_FILE_NAME = "info_level_file_name";
    public final String KEY_ERROR_FILE_NAME = "error_level_file_name";
    public final String KEY_DEBUG_FILE_NAME = "debug_level_file_name";
    protected ACache aCache;
    protected String cacheDir = Environment.getExternalStorageDirectory()+File.separator+"cacheDir"+File.separator;
    protected final String FILENAMESPLIT = "-";
    public static String logDirRoot = "/mnt/sdcard/androidYH/log";

    public final static String LOGINALLFOLDER = "all_level";
    public final static String LOGERRORFOLDER = "error_level";
    public final static String LOGDEBUGFOLDER = "debug_level";
    public final static String LOGINFOFOLDER = "info_level";
    public final static String LOGWARNFOLDER = "warn_level";
    public final static String LOGVERBOSEFOLDER = "verbose_level";

    public static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    protected String logFolder;

    public  static String  fileName_pre = "yhlog";

    protected String suffix = ".log";
    //上次写入的文件信息
    /*protected String lastFileName;
    protected String lastFilePath;

*/
    protected String targetpath;

    protected File file;
    protected String currentDateStr;
    protected long lastOperateTime = SystemClock.elapsedRealtime();
    private FilePathGenerator() {
        throw new AssertionError();
    }


    public FilePathGenerator(Context context, String fileName_pre, String suffix) {
        if (context == null) {
            throw new NullPointerException("while construct FilePathGenerator for wirting log to file,The context should not be null");
        }

        logDirRoot = context.getExternalFilesDir(null).getAbsolutePath() + File.separator + "log";

        if (!TextUtils.isEmpty(fileName_pre)) {
            this.fileName_pre = fileName_pre;
        }

        if (!TextUtils.isEmpty(suffix)) {
            this.suffix = suffix;
        }
        File file = new File(cacheDir);
        aCache = ACache.get(file);
    }


    private FilePathGenerator(Context context, String logDirRoot, String fileName_pre, String suffix) {
        if (context == null) {
            throw new NullPointerException("while construct FilePathGenerator for wirting log to file,The context should not be null");
        }
        if (!TextUtils.isEmpty(logDirRoot)) {
            this.logDirRoot = logDirRoot;
        }

        if (!TextUtils.isEmpty(fileName_pre)) {
            this.fileName_pre = fileName_pre;
        }

        if (!TextUtils.isEmpty(suffix)) {
            this.suffix = suffix;
        }
        File file = new File(cacheDir);
        aCache = ACache.get(file);
    }


    public abstract String generateFilePath();


    public abstract boolean isNeedGenerate();


    public abstract void onGernerate(String newPath, String oldPath);


    public synchronized final String getTargetpath() {
        long nowStamp = SystemClock.elapsedRealtime();
        if(nowStamp - lastOperateTime > 1000){
            Date currentDate = new Date(System.currentTimeMillis());
            currentDateStr = sdf.format(currentDate);
            lastOperateTime = nowStamp;
        }
        if (isNeedGenerate()) {
            String newPath = generateFilePath();
            onGernerate(newPath, targetpath);
            targetpath = newPath;
        }
        return targetpath;
    }


    public static class DefaultFilePathGenerator extends FilePathGenerator {
        public static long limitSingleLogFileSize = 1024 * 1024 * 10; //10M
        SimpleDateFormat sdf = FilePathGenerator.sdf;


        public DefaultFilePathGenerator() {
        }

        public DefaultFilePathGenerator(Context context, String fileName_pre, String suffix) {
            super(context, fileName_pre, suffix);
        }

        public DefaultFilePathGenerator(Context context, String logDirRoot, String fileName_pre, String suffix) {
            super(context, logDirRoot, fileName_pre, suffix);
        }

        public DefaultFilePathGenerator(Context context, String logDirRoot, String logFolder, String fileName_pre, String suffix) {
            super(context, logDirRoot, fileName_pre, suffix);
            this.logFolder = logFolder;
        }

        @Override
        public  String generateFilePath() {
            String path = null;
            if (TextUtils.isEmpty(logDirRoot)) {
                return path;  //null
            }

            if (TextUtils.isEmpty(logFolder)) {
                return path;  //null
            }

            File logDir = new File(logDirRoot + File.separator + logFolder);
            if (!logDir.exists()) {
                try {
                    logDir.mkdirs();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
//            long nowStamp = SystemClock.elapsedRealtime();
//            if(nowStamp - lastOperateTime > 1000*2){
//                Date currentDate = new Date(System.currentTimeMillis());
//                currentDateStr = sdf.format(currentDate);
//                lastOperateTime = nowStamp;
//            }
            if(TextUtils.isEmpty(currentDateStr)){
                Date currentDate = new Date(System.currentTimeMillis());
                currentDateStr = sdf.format(currentDate);
            }

            //创建日志文件
            StringBuffer sb = new StringBuffer();
            sb.append(fileName_pre);
            sb.append(FILENAMESPLIT);
            String lastFileName = "";
            if(LOGINALLFOLDER.equals(logFolder)){
                lastFileName = aCache.getAsString(KEY_ALL_FILE_NAME);
                android.util.Log.d("*DEBUG*1","1      "+lastFileName);
            }else if(LOGERRORFOLDER.equals(logFolder)){
                lastFileName = aCache.getAsString(KEY_ERROR_FILE_NAME);
            }else if(LOGDEBUGFOLDER.equals(logFolder)){
                lastFileName = aCache.getAsString(KEY_DEBUG_FILE_NAME);
            }else if(LOGINFOFOLDER.equals(logFolder)){
                lastFileName = aCache.getAsString(KEY_INFO_FILE_NAME);
            }
            if(!TextUtils.isEmpty(lastFileName)){
                String[] str_array = lastFileName.split(FILENAMESPLIT);
                if (str_array != null && str_array.length == 3) {
                    android.util.Log.d("*DEBUG*2","2   "+currentDateStr+"     "+str_array[1]);
                    if (currentDateStr.equals(str_array[1])) {
                        sb.append(currentDateStr);
                        sb.append(FILENAMESPLIT);
                        String str_last = str_array[2];
                        String[] suffix_array = str_last.split("\\.");
                        if (suffix_array != null && suffix_array.length >= 2) {
                            try {
                                Integer fileIndex = Integer.parseInt(suffix_array[0]) + 1;
                                sb.append(String.valueOf(fileIndex.intValue()));
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }

                        }
                    } else {
                        sb.append(currentDateStr);
                        sb.append(FILENAMESPLIT);
                        sb.append("1");
                    }
                } else {
                    sb.append(currentDateStr);
                    sb.append(FILENAMESPLIT);
                    sb.append("1");
                }
            }else{
                sb.append(currentDateStr);
                sb.append(FILENAMESPLIT);
                sb.append("1");
            }

            sb.append(suffix);
            String strFilename = sb.toString();
            file = new File(logDir, strFilename);
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if(LOGINALLFOLDER.equals(logFolder)){
                aCache.put(KEY_ALL_FILE_NAME,strFilename);
                android.util.Log.d("*DEBUG*3","3    "+strFilename);
            }else if(LOGERRORFOLDER.equals(logFolder)){
                aCache.put(KEY_ERROR_FILE_NAME,strFilename);
            }else if(LOGDEBUGFOLDER.equals(logFolder)){
                aCache.put(KEY_DEBUG_FILE_NAME,strFilename);
            }else if(LOGINFOFOLDER.equals(logFolder)){
                aCache.put(KEY_INFO_FILE_NAME,strFilename);
            }
            path = file.getAbsolutePath();
            return path;

        }

        @Override
        public boolean isNeedGenerate() {
            //the time that to write the log
            if(TextUtils.isEmpty(currentDateStr)){
                Date currentDate = new Date(System.currentTimeMillis());
                currentDateStr = sdf.format(currentDate);
            }
            String lastFileName = "";
            if(LOGINALLFOLDER.equals(logFolder)){
                lastFileName = aCache.getAsString(KEY_ALL_FILE_NAME);
            }else if(LOGERRORFOLDER.equals(logFolder)){
                lastFileName = aCache.getAsString(KEY_ERROR_FILE_NAME);
            }else if(LOGDEBUGFOLDER.equals(logFolder)){
                lastFileName = aCache.getAsString(KEY_DEBUG_FILE_NAME);
            }else if(LOGINFOFOLDER.equals(logFolder)){
                lastFileName = aCache.getAsString(KEY_INFO_FILE_NAME);
            }


            if (TextUtils.isEmpty(lastFileName) || lastFileName == null || "".equals(lastFileName)) {
                return true;
            } else {
                File lastFile = new File(logDirRoot + File.separator + logFolder+File.separator+lastFileName);
                targetpath = logDirRoot + File.separator + logFolder+File.separator+lastFileName;
                if (lastFile == null || !lastFile.exists()) {
                    return true;
                }
                if (lastFile.length() >= limitSingleLogFileSize) {
                    return true;
                }
                String[] str_array = lastFileName.split(FILENAMESPLIT);
                if (str_array != null && str_array.length >= 3) {
                    String lastfiledata = str_array[1];
                    if (currentDateStr.equals(lastfiledata)) {
                        return false;
                    } else {
                        return true;
                    }
                } else {
                    return true;
                }
            }

        }

        @Override
        public void onGernerate(String newPath, String oldPath) {

        }
    }


    /**
     * 计算两个日期之间相差的天数
     *
     * @param smdate 较小的时间
     * @param bdate  较大的时间
     * @return 相差天数
     * @throws java.text.ParseException
     */
    public static int daysBetween(Date smdate, Date bdate) throws java.text.ParseException {
        smdate = sdf.parse(sdf.format(smdate));
        bdate = sdf.parse(sdf.format(bdate));
        Calendar cal = Calendar.getInstance();
        cal.setTime(smdate);
        long time1 = cal.getTimeInMillis();
        cal.setTime(bdate);
        long time2 = cal.getTimeInMillis();
        long between_days = (time2 - time1) / (1000 * 3600 * 24);

        return Integer.parseInt(String.valueOf(between_days));
    }

    /**
     * 字符串的日期格式的计算
     *
     * @throws java.text.ParseException
     */
    public static int daysBetween(String smdate, String bdate) throws java.text.ParseException {

        Calendar cal = Calendar.getInstance();
        cal.setTime(sdf.parse(smdate));
        long time1 = cal.getTimeInMillis();
        cal.setTime(sdf.parse(bdate));
        long time2 = cal.getTimeInMillis();
        long between_days = (time2 - time1) / (1000 * 3600 * 24);

        return Integer.parseInt(String.valueOf(between_days));
    }

}
