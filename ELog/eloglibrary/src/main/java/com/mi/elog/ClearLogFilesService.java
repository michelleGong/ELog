package com.mi.elog;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.StatFs;
import android.text.format.DateUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 * @author  Michell_Hong
 */
public class ClearLogFilesService extends IntentService {

    private final  String TAG = "--ClearLogFilesService--";
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_CLEAR_LOGFILES = "com.yarward.lib.log.action.CLEAR_LOGFILES";
    private static final String ACTION_DELETE_LOGFILES_BY_LIMITDATE = "com.yarward.lib.log.action.DELETE_LOGFILES_BY_LIMITDATE";

   // TODO: Rename parameters
    private static final String EXTRA_YEAR = "extra.year";
    private static final String EXTRA_MONTH = "extra.month";
    private static final String EXTRA_DAY = "extra.day";

//    private FastDateFormat fdf = FastDateFormat.getInstance("yyyyMMdd");
    private SimpleDateFormat fdf = FilePathGenerator.sdf;
    private int limitdaySpace = 60;
    private	long limitAllLogSize = 1024*1024*1024*3;  //3G
//    private long limitAllLogSize = 1024*1024*10;  //10M
    private  ConcurrentTotalFileSizeWLatch fileSizeWLatch = new ConcurrentTotalFileSizeWLatch(Log.getExecutor());

    public ClearLogFilesService() {
        super("ClearLogFilesService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startClearLogFiles(Context context) {
        Intent intent = new Intent(context, ClearLogFilesService.class);
        intent.setAction(ACTION_CLEAR_LOGFILES);
       /* intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);*/
        context.startService(intent);
    }


    /**
     *
     * @param context
     * @param year
     * @param month
     * @param day
     */
    public static void startDeleteLogFilesByLimitDate(Context context,int year,int month,int day){
        Intent intent = new Intent(context, ClearLogFilesService.class);
        intent.setAction(ACTION_DELETE_LOGFILES_BY_LIMITDATE);
        intent.putExtra(EXTRA_YEAR, year);
        intent.putExtra(EXTRA_MONTH, month);
        intent.putExtra(EXTRA_DAY,day);
        context.startService(intent);
    }




    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_CLEAR_LOGFILES.equals(action)) {
               /* final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);*/
                handleActionClearLogFiles();
            }else if(ACTION_DELETE_LOGFILES_BY_LIMITDATE.equals(action)){
                final int year = intent.getIntExtra(EXTRA_YEAR,1960);
                final int month = intent.getIntExtra(EXTRA_MONTH,1);
                final int day = intent.getIntExtra(EXTRA_DAY,1);
                handleActionDeleteFile(year,month,day);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionClearLogFiles() {
       /* settingTools = SettingTools.builder(this);
        limitdaySpace = settingTools.getLogFileDeleteLimitDay();
        limitAllLogSize = settingTools.getLogFileAllSize();*/

        //current Date
        Date currentDate = new Date(System.currentTimeMillis());
        String currentDateStr = fdf.format(currentDate);

        int daySpace = 0;
        String earlyestDateTimeStr = "";
        //所有日志文件的根目录
        String rootDir = FilePathGenerator.logDirRoot;
        File rootDirFile = new File(rootDir);
        Log.i(TAG, rootDir+"|     |"+FilePathGenerator.fileName_pre);
        //当前日志总容量
        long currentSize = 0;
        try {
            currentSize = fileSizeWLatch.getTotalSizeOfFile(rootDir);
            //各种Level日志文件夹和所有Level写入日志文件的文件夹
            File[] logfolders = rootDirFile.listFiles();
            File[] logfiles = null;
            //遍历各个文件夹
            for(File logfloder : logfolders){
                logfiles = logfloder.listFiles();
//                android.util.Log.d("*文件夹"+logfloder.getName()+"，日志文件个数:", logfiles.length+"");

                //for debug long time operation----------------
//            try {
//                Thread.sleep(3000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
                //--------------------------------------

                if(logfiles != null && logfiles.length > 0){
                    Arrays.sort(logfiles, new FileComparatorByDate());
                    //现保存的最早的日志日期
                    earlyestDateTimeStr = logfiles[0].getName().substring(FilePathGenerator.fileName_pre.length()+1, FilePathGenerator.fileName_pre.length()+9);
//                Log.d(logfloder.getName()+"--最早日志文件：", earlyestDateTimeStr+"    current 日期："+currentDateStr);

                    try {
                        daySpace = FilePathGenerator.daysBetween(earlyestDateTimeStr, currentDateStr);
                        Log.d(TAG, logfloder.getName()+"最早生成的日志文件，与当前日期的daySpace:"+daySpace+"--最早日志文件日期："+earlyestDateTimeStr+"    current 日期："+currentDateStr);
                    } catch (ParseException e) {
                        // TODO Auto-generated catch block
                        Log.e(TAG, "删除日志文件解析",e);
                        e.printStackTrace();
                    }


                    if(currentSize >= limitAllLogSize || daySpace >= limitdaySpace
                            ||ConcurrentTotalFileSizeWLatch.getSDFreeSize() <= Log.limitFreeStorage){ //删除最早那天的日志文件
                        Log.i(TAG, "currentSize:"+currentSize+"    limitAllLogSize:"+limitAllLogSize+"   daySpace:"+daySpace+"   limitdaySpace:"+limitdaySpace);
                        for(File file : logfiles){
                            String fileDateStr = file.getName().substring(FilePathGenerator.fileName_pre.length()+1, FilePathGenerator.fileName_pre.length()+9);
                            int sp = 0;
                            try {
                                sp = FilePathGenerator.daysBetween(fileDateStr,currentDateStr);
                            } catch (ParseException e1) {
                                // TODO Auto-generated catch block
                                Log.e(TAG, "获取最早生成日志文件，与文件"+file.getName()+"日期间隔错误",e1);
                                e1.printStackTrace();
                            }
                            if((file.getName().contains(earlyestDateTimeStr))|| sp >= limitdaySpace
                                    ||fileSizeWLatch.getTotalSizeOfFile(rootDir) >=
                                    limitAllLogSize-FilePathGenerator.DefaultFilePathGenerator.limitSingleLogFileSize
                                    ||ConcurrentTotalFileSizeWLatch.getSDFreeSize() <= FilePathGenerator.DefaultFilePathGenerator.limitSingleLogFileSize){

                                try{
                                    boolean isdeleted = file.delete();
                                    if(isdeleted){
                                        Log.i(TAG, "删除日志文件："+file.getAbsolutePath());
                                    }else{
                                        Log.e(TAG, "删除日志文件"+file.getAbsolutePath()+"  失败！");
                                    }
                                }catch(Exception e){
                                    Log.e(TAG, "删除日志文件错误",e);
                                }
                            }
                        }
                    }
                }
            }
//        ClearLogFilesService.this.stopSelf();
        } catch (Exception e) {
            Log.e(TAG, "删除日志文件错误",e);
        }


    }


    /**
     * 删除早于期限日期的日志文件们
     * @param
     */
    private void handleActionDeleteFile(int year,int month,int day){
        android.util.Log.d("DELETE","**"+year+"  "+month+"   "+day);
        Calendar calendar = Calendar.getInstance();
        calendar.set(year,month-1,day);

        //test
        Date date = new Date(calendar.getTimeInMillis());
        String str = fdf.format(date);
        android.util.Log.d("DELETE1",str);
        //--

        EarlyDateFileFilter fileFilter = new EarlyDateFileFilter(calendar);
        //所有日志文件的根目录
        String rootDir = FilePathGenerator.logDirRoot;
        File rootDirFile = new File(rootDir);
        //各种Level日志文件夹和所有Level写入日志文件的文件夹
        File[] logfolders = rootDirFile.listFiles();
        File[] logfiles = null;
        for(File logfloder : logfolders){
            logfiles = logfloder.listFiles();
            for(File tmpFile : logfiles){
                if(fileFilter.accept(tmpFile)){
                    android.util.Log.d("DELETE-do", fdf.format(new Date(tmpFile.lastModified())));
                    tmpFile.delete();
                }
            }
        }
    }

    /**
     * 期限日期Filter
     */
    class EarlyDateFileFilter implements FileFilter{
        //期限日期，最后修改日志早于当前日期的，符合条件
        long limitDate;

        public EarlyDateFileFilter(long limitDateMillis){
            limitDate = limitDateMillis;
        }

        public EarlyDateFileFilter(Date limitDatedata){
            limitDate = limitDatedata.getTime();
        }

        public EarlyDateFileFilter(Calendar calendar){
            limitDate = calendar.getTimeInMillis();
        }


        @Override
        public boolean accept(File pathname) {
            long lastModifyTime = pathname.lastModified();
            if(lastModifyTime < limitDate){
                return true;
            }
            return false;
        }
    }


}
