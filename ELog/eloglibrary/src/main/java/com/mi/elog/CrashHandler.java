package com.mi.elog;

import android.content.Context;

/**
 * Created by Michelle_Hong on 2016/8/25 0025.
 */

public class CrashHandler implements Thread.UncaughtExceptionHandler {

    private static final String TAG = "CrashHandler";
    private static CrashHandler crashHandler;
    /**
     * Context
     */
    private Context context;
    /**
     * The default Sys UnCaughtException Handler
     */
    private Thread.UncaughtExceptionHandler uncaughtExceptionHandler;
    private CrashHandler(){

    }


    public synchronized static CrashHandler getInstance(){
        if(crashHandler == null){
            crashHandler = new CrashHandler();
        }
        return crashHandler;
    }



    public void init(Context context){
        this.context = context;
        //init uncaughtExceptionHandler
        uncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        //set CrashHandler the system Handler
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    /**
     * when UncaughtException happened ,this method will be invoked
     * @param thread
     * @param ex
     */
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        boolean res = handleException(ex);
        android.util.Log.d(TAG,res+"******************");
        if(uncaughtExceptionHandler != null){
            //the system default Handler handle the UnCaughtException
            android.util.Log.d(TAG,"******************");
            uncaughtExceptionHandler.uncaughtException(thread,ex);
        }
    }


    /**
     * handler UncaughtException custom
     * @param exceptin
     * @return
     */
    private boolean handleException(final Throwable exceptin){
        if(exceptin == null){
            return false;
        }else{
            /*new Thread() {

                @Override
                public void run() {
                    Looper.prepare();
                    String err = "[" + exceptin.getMessage() + "]";
                    Toast.makeText(context, "程序出现异常." + err, Toast.LENGTH_LONG)
                            .show();

                    Looper.loop();
                }

            }.start();*/
            if(Log.isLogInit()){
                Log.e(TAG,"系统未处理异常",exceptin);
            }else{
                Log.init(context);
                Log.e(TAG,"系统未处理异常",exceptin);
            }
            return true;
        }
    }

}
