package com.mi.elog.acache;

import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Michelle_Hong on 2016/9/1 0001.
 * The key to Process Safe, one is lastuasgeData for concurrently operation(put get)。
 *
 * @des 缓存管理器
 */

public class ACacheManager {

    private final long sizeLimie;
    private final int countLimie;
    private AtomicLong cacheSize;
    private AtomicInteger cacheCount;
    protected File cacheDir;
    //the synchronized collection
    //
    private final Map<File,Long> lastusageData = new ConcurrentHashMap<File,Long>();

    public ACacheManager(File cacheDir,long sizeLimie,int countLimie){
        this.cacheDir = cacheDir;
        this.sizeLimie = sizeLimie;
        this.countLimie = countLimie;
        cacheSize = new AtomicLong();
        cacheCount = new AtomicInteger();
        calculateCacheSizeAndCacheCount();

    }

    /**
     * 计算文件的size
     * @param file
     * @return
     */
    private long calculateSize(File file){
        return file.length();
    }

    /**
     * 计算缓存文件的总size和缓存文件的数量，以及将文件放到Map里
     */
    private void calculateCacheSizeAndCacheCount(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                int  size = 0;
                int count = 0;
                File[] cachedFiles = cacheDir.listFiles();
                if(cachedFiles != null){
                    for(File cachedFile : cachedFiles){
                        size += calculateSize(cachedFile);
                        count ++;
                        lastusageData.put(cachedFile,cachedFile.lastModified());
                    }
                    cacheSize.set(size);
                    cacheCount.set(count);
                }
            }
        }).start();
    }

    /**
     * 移除旧的文件
     * @return  删除文件的size
     */
    private long removeNext(){
        if(lastusageData.isEmpty()){
            return 0;
        }

        Long oldestUsage = null;  //最早的文件，最早的文件，时间越小
        File mostLongUsedFile = null;
        Set<Map.Entry<File,Long>> entries =lastusageData.entrySet();
        //加锁
        synchronized (lastusageData){
            //遍历,获取最早的Map对，
            for(Map.Entry<File ,Long> entry : entries){
                if(mostLongUsedFile == null){
                    mostLongUsedFile = entry.getKey();
                    oldestUsage = entry.getValue();
                }else{
                    Long lastValueUsage = entry.getValue();
                    if(lastValueUsage < oldestUsage){
                        oldestUsage = lastValueUsage;
                        mostLongUsedFile = entry.getKey();
                    }
                }
            }
        }
        //删除文件
        long fileSize = calculateSize(mostLongUsedFile);
        if(mostLongUsedFile.delete()){
            lastusageData.remove(mostLongUsedFile);
        }
        return fileSize;
    }

    /**
     * 根据key创建File对象
     * @param key
     * @return
     */
    public File newFile(String key){
        return new File(cacheDir,key.hashCode()+"");
    }


    public void put(File file){
        //cache Count
        int curCacheCount = cacheCount.get();
        //如果超过了文件上限,删文件(循环执行)
        while(curCacheCount + 1 > countLimie){
            long freedSize = removeNext();
            //更新缓存值
            cacheSize.addAndGet(-freedSize);
            //更新缓存文件数
            curCacheCount = cacheCount.addAndGet(-1);
        }

        long valueSize = calculateSize(file);
        long curCacheSize = cacheSize.get();
        //如果超过缓存最大上限，删文件（循环执行）
        while(curCacheSize + valueSize > sizeLimie){
            long freedSize = removeNext();
            curCacheSize = cacheSize.addAndGet(-freedSize);
        }

        //增加文件
        cacheCount.addAndGet(1);
        cacheSize.addAndGet(valueSize);
        Long currentTime = System.currentTimeMillis();
        file.setLastModified(currentTime);
        lastusageData.put(file,currentTime);
    }


    public File get(String key){
        File file = newFile(key);
        Long currentTime = System.currentTimeMillis();
        file.setLastModified(currentTime);
        lastusageData.put(file,currentTime);
        return file;
    }

    public boolean remove(String key){
        File targetFile = get(key);
        return targetFile.delete();
    }


    public void clear(){
        lastusageData.clear();
        cacheSize.set(0);
        File[] files = cacheDir.listFiles();
        if(files != null){
            for(File f : files){
                f.delete();
            }
        }
    }





}
