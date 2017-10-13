package com.mi.elog.acache;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Michelle_Hong on 2016/9/10 0010.reference
 *
 * single patern
 * @des
 */

public class ACache {

    //max cahce file size
    private static final long MAX_SIZE = Long.MAX_VALUE;
    //max count of cache files
    private static final int MAX_COUNT = Integer.MAX_VALUE; // 不限制存放数据的数量
    //key operation object
    private ACacheManager mCache;
    //Collections keeps the Acache Objects ,key : cache File Path + pid
    private static Map<String,ACache> mInstanceMap = new HashMap<String,ACache>();

    /**
     * constructor
     * @param cacheDir
     * @param max_size
     * @param max_count
     */
    private ACache(File cacheDir, long max_size, int max_count) throws RuntimeException{
        if(!cacheDir.exists() && !cacheDir.mkdirs()){
            throw new RuntimeException("cann't make dir in "+cacheDir.getAbsolutePath());
        }
        mCache = new ACacheManager(cacheDir,max_size,max_count);
    }

    public static ACache get(File cacheDir,long max_size,int max_count) throws RuntimeException{
        //one process keep one Acache Object,relatived to one File
        ACache manager = mInstanceMap.get(cacheDir.getAbsoluteFile()+myPid());
        if(manager == null){
            manager = new ACache(cacheDir,max_size,max_count);
            mInstanceMap.put(cacheDir.getAbsolutePath()+myPid(),manager);
        }
        return manager;
    }

    public static ACache get(Context context, long max_size, int max_count) throws RuntimeException{
        //default File dir is app‘s data/data/cache
        File f = new File(context.getCacheDir(),"Acache");
        return get(f,max_size,max_count);
    }

    public static ACache get(File cacheDir) {
        return get(cacheDir, MAX_SIZE, MAX_COUNT);
    }

    /**
     * get Pid
     * @return
     */
    private static String myPid(){
        return "_"+android.os.Process.myPid();
    }

    /**
     * Provides a means to save a cached file before the data are available.
     * Since writing about the file is complete, and its close method is called,
     * its contents will be registered in the cache. Example of use:
     * <p>
     * ACache cache = new ACache(this) try { OutputStream stream =
     * cache.put("myFileName") stream.write("some bytes".getBytes()); // now
     * update cache! stream.close(); } catch(FileNotFoundException e){
     * e.printStackTrace() }
     */
    class xFileOutputStream extends FileOutputStream {
        File file;

        public xFileOutputStream(File file) throws FileNotFoundException {
            super(file);
            this.file = file;
        }

        public void close() throws IOException {
            super.close();
            mCache.put(file);
        }
    }


    // =======================================
    // ============ String数据 读写 ==============
    // =======================================

    /**
     * 保存String数据到缓存文件中
     * @param key 保存的key值 （缓存文件名，key取哈希）
     * @param value 保存String数据
     */
    public void put(String key,String value){
        //该key值对应的缓存文件的对象,file name is the key’s hashcode
        File file = mCache.newFile(key);
        BufferedWriter bufferedWriter  = null;
        FileWriter fileWriter = null;
        //write the value
        try {
            fileWriter = new FileWriter(file);
            bufferedWriter = new BufferedWriter(fileWriter,1024);
            bufferedWriter.write(value);
            bufferedWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {

            if(bufferedWriter != null){
                try {
                    bufferedWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(fileWriter != null){
                try {
                    fileWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        //save the cache File，此处保证进程安全
        mCache.put(file);
    }


    /**
     * 保存String数据到缓存中
     * 保存数据的时候，会将保存的当前时间和saveTime写入内容中，在取数据的时候做检测，是否过期
     * @param key       保存的key值
     * @param value     保存的String数据
     * @param saveTime  保存时间，秒
     */
    public void put(String key,String value,int saveTime){
        put(key, AcacheUtils.newStringWithDataInfo(saveTime,value));
    }

    /**
     * 读取String数据
     * @param key
     * @return
     */
    public String getAsString(String key){
        //从manager里集合保存的文件，里找到key对应的文件(此处保证进程安全)
        File file = mCache.get(key);
        if(!file.exists()){
            return null;
        }
        boolean removeFile = false;
        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        try {
            fileReader = new FileReader(file);
            bufferedReader = new BufferedReader(fileReader);
            String readString = "";
            String currentLine;
            while((currentLine = bufferedReader.readLine()) != null){
                readString += currentLine;
            }
            if(!AcacheUtils.isDue(readString)){
                return AcacheUtils.clearDateInfo(readString);
            }else{
                removeFile = true;
                return null;
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if(fileReader != null){
                    fileReader.close();
                }
                if(bufferedReader != null){
                    bufferedReader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(removeFile){
                remove(key);
            }
        }
        return null;
    }

    // =======================================
    // ============= JSONObject 数据 读写 ==============
    // =======================================

    /**
     * 保存 JSONObject数据 到 缓存中
     *
     * @param key   保存的key
     * @param value 保存的JSON数据
     */
    public void put(String key, JSONObject value) {
        put(key, value.toString());
    }

    /**
     * 保存 JSONObject数据 到 缓存中
     *
     * @param key      保存的key
     * @param value    保存的JSONObject数据
     * @param saveTime 保存的时间，单位：秒
     */
    public void put(String key, JSONObject value, int saveTime) {
        put(key, value.toString(), saveTime);
    }

    /**
     * 读取JSONObject数据
     *
     * @param key
     * @return JSONObject数据
     */
    public JSONObject getAsJSONObject(String key) {
        String JSONString = getAsString(key);
        try {
            JSONObject obj = new JSONObject(JSONString);
            return obj;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // =======================================
    // ============ JSONArray 数据 读写 =============
    // =======================================

    /**
     * 保存 JSONArray数据 到 缓存中
     *
     * @param key   保存的key
     * @param value 保存的JSONArray数据
     */
    public void put(String key, JSONArray value) {
        put(key, value.toString());
    }

    /**
     * 保存 JSONArray数据 到 缓存中
     *
     * @param key      保存的key
     * @param value    保存的JSONArray数据
     * @param saveTime 保存的时间，单位：秒
     */
    public void put(String key, JSONArray value, int saveTime) {
        put(key, value.toString(), saveTime);
    }

    /**
     * 读取JSONArray数据
     *
     * @param key
     * @return JSONArray数据
     */
    public JSONArray getAsJSONArray(String key) {
        String JSONString = getAsString(key);
        try {
            JSONArray obj = new JSONArray(JSONString);
            return obj;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    // =======================================
    // ============== byte 数据 读写 =============
    // =======================================

    /**
     * 保存 byte数据 到 缓存中
     *
     * @param key   保存的key
     * @param value 保存的数据
     */
    public void put(String key, byte[] value) {
        File file = mCache.newFile(key);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            out.write(value);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.flush();
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            mCache.put(file);
        }
    }




    /**
     * Cache for a stream
     *
     * @param key the file name.
     * @return OutputStream stream for writing data.
     * @throws FileNotFoundException if the file can not be created.
     */
    public OutputStream put(String key) throws FileNotFoundException {
        return new xFileOutputStream(mCache.newFile(key));
    }

    /**
     * @param key the file name.
     * @return (InputStream or null) stream previously saved in cache.
     * @throws FileNotFoundException if the file can not be opened
     */
    public InputStream get(String key) throws FileNotFoundException {
        File file = mCache.get(key);
        if (!file.exists())
            return null;
        return new FileInputStream(file);
    }


    /**
     * 保存 byte数据 到 缓存中
     *
     * @param key      保存的key
     * @param value    保存的数据
     * @param saveTime 保存的时间，单位：秒
     */
    public void put(String key, byte[] value, int saveTime) {
        put(key, AcacheUtils.newByteArrayWithDateInfo(saveTime, value));
    }

    /**
     * 获取 byte 数据
     *
     * @param key
     * @return byte 数据
     */
    public byte[] getAsBinary(String key) {
        RandomAccessFile RAFile = null;
        boolean removeFile = false;
        try {
            File file = mCache.get(key);
            if (!file.exists())
                return null;
            RAFile = new RandomAccessFile(file, "r");
            byte[] byteArray = new byte[(int) RAFile.length()];
            RAFile.read(byteArray);
            if (!AcacheUtils.isDue(byteArray)) {
                return AcacheUtils.clearDateInfo(byteArray);
            } else {
                removeFile = true;
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (RAFile != null) {
                try {
                    RAFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (removeFile)
                remove(key);
        }
    }

    // =======================================
    // ============= 序列化 数据 读写 ===============
    // =======================================

    /**
     * 保存 Serializable数据 到 缓存中
     *
     * @param key   保存的key
     * @param value 保存的value
     */
    public void put(String key, Serializable value) {
        put(key, value, -1);
    }

    /**
     * 保存 Serializable数据到 缓存中
     *
     * @param key      保存的key
     * @param value    保存的value
     * @param saveTime 保存的时间，单位：秒
     */
    public void put(String key, Serializable value, int saveTime) {
        ByteArrayOutputStream baos = null;
        ObjectOutputStream oos = null;
        try {
            baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);
            oos.writeObject(value);
            byte[] data = baos.toByteArray();
            if (saveTime != -1) {
                put(key, data, saveTime);
            } else {
                put(key, data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                oos.close();
            } catch (IOException e) {
            }
        }
    }

    /**
     * 读取 Serializable数据
     *
     * @param key
     * @return Serializable 数据
     */
    public Object getAsObject(String key) {
        byte[] data = getAsBinary(key);
        if (data != null) {
            ByteArrayInputStream bais = null;
            ObjectInputStream ois = null;
            try {
                bais = new ByteArrayInputStream(data);
                ois = new ObjectInputStream(bais);
                Object reObject = ois.readObject();
                return reObject;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            } finally {
                try {
                    if (bais != null)
                        bais.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    if (ois != null)
                        ois.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;

    }

    // =======================================
    // ============== bitmap 数据 读写 =============
    // =======================================

    /**
     * 保存 bitmap 到 缓存中
     *
     * @param key   保存的key
     * @param value 保存的bitmap数据
     */
    public void put(String key, Bitmap value) {
        put(key, AcacheUtils.Bitmap2Bytes(value));
    }

    /**
     * 保存 bitmap 到 缓存中
     *
     * @param key      保存的key
     * @param value    保存的 bitmap 数据
     * @param saveTime 保存的时间，单位：秒
     */
    public void put(String key, Bitmap value, int saveTime) {
        put(key, AcacheUtils.Bitmap2Bytes(value), saveTime);
    }

    /**
     * 读取 bitmap 数据
     *
     * @param key
     * @return bitmap 数据
     */
    public Bitmap getAsBitmap(String key) {
        if (getAsBinary(key) == null) {
            return null;
        }
        return AcacheUtils.Bytes2Bimap(getAsBinary(key));
    }

    // =======================================
    // ============= drawable 数据 读写 =============
    // =======================================

    /**
     * 保存 drawable 到 缓存中
     *
     * @param key   保存的key
     * @param value 保存的drawable数据
     */
    public void put(String key, Drawable value) {
        put(key, AcacheUtils.drawable2Bitmap(value));
    }

    /**
     * 保存 drawable 到 缓存中
     *
     * @param key      保存的key
     * @param value    保存的 drawable 数据
     * @param saveTime 保存的时间，单位：秒
     */
    public void put(String key, Drawable value, int saveTime) {
        put(key, AcacheUtils.drawable2Bitmap(value), saveTime);
    }

    /**
     * 读取 Drawable 数据
     *
     * @param key
     * @return Drawable 数据
     */
    public Drawable getAsDrawable(String key) {
        if (getAsBinary(key) == null) {
            return null;
        }
        return AcacheUtils.bitmap2Drawable(AcacheUtils.Bytes2Bimap(getAsBinary(key)));
    }

    /**
     * 获取缓存文件
     *
     * @param key
     * @return value 缓存的文件
     */
    public File file(String key) {
        File f = mCache.newFile(key);
        if (f.exists())
            return f;
        return null;
    }

    private void remove(String key){
        mCache.remove(key);
    }

    /**
     * 清除所有数据
     */
    public void clear() {
        mCache.clear();
    }


}
