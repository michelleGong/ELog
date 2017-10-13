package com.mi.elog;

import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 
 * @描述：写入文件核心功能类
 * @author：Michelle_Hong 
 * @创建时间：2015-6-9上午10:07:11
 * @see
 */
public class Log2File {

    private static ExecutorService executor = null;

    
    protected static ExecutorService getExecutor() {
        return executor;
    }

  
    protected static void setExecutor(ExecutorService executor) {
        Log2File.executor = executor;
    }

   
    
    protected static void log2file(final String path, final String str) {
        if (executor == null) {
            executor = Executors.newFixedThreadPool(10);
            ConcurrentTotalFileSizeWLatch.setExecutor(executor);
        }

        if (executor != null) {

            executor.execute(new Runnable() {
                @Override
                public void run() {
                    PrintWriter out = null;
                    BufferedWriter bufferedWriter = null;
                    FileWriter fileWriter = null;
                    File file = GetFileFromPath(path);

                    if (file != null && file.canWrite()) {
                        try {
                            fileWriter = new FileWriter(file, true);
                            bufferedWriter = new BufferedWriter(fileWriter,1024);
                            out = new PrintWriter(bufferedWriter);
                            out.println(str);
                            out.flush();
                        } catch (IOException e) {
                           Log.w("Log2File","when write log to file IOException",e);
                        } finally {
                            try {
                                if(out != null){
                                    out.close();
                                }
                                if(bufferedWriter != null){
                                    bufferedWriter.close();
                                }
                                if(fileWriter != null){
                                    fileWriter.close();
                                }
                            } catch (IOException e) {
                                Log.w("Log2File","when close IO IOException",e);
                            }

                        }
                    }
                }
            });
        }
    }


    /**
     * 写入文件核心方法
     * @param path  被写入文件的目录
     *
     */
    private static File GetFileFromPath(String path) {
        boolean ret;
        boolean isExist;
        boolean isWritable;
        File file = null;

        if (TextUtils.isEmpty(path)) {
            Log.e("Error", "The path of Log file is Null.");
            return file;
        }

        file = new File(path);

        isExist = file.exists();
        isWritable = file.canWrite();

        if (isExist) {
            if (isWritable) {
                //Log.i("Success", "The Log file exist,and can be written! -" + file.getAbsolutePath());
            } else {
                Log.e("Error", "The Log file can not be written.");
            }
        } else {
            //create the log file
            try {
                ret = file.createNewFile();
                if (ret) {
                    Log.i("Success", "The Log file was successfully created! -" + file.getAbsolutePath());
                } else {
                    Log.i("Success", "The Log file exist! -" + file.getAbsolutePath());
                }

                isWritable = file.canWrite();
                if (!isWritable) {
                    Log.e("Error", "The Log file can not be written.");
                }
            } catch (IOException e) {
                Log.e("Error", "Failed to create The Log file.");
                e.printStackTrace();
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        return file;
    }
}
