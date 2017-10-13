package com.mi.elog;

import android.os.Environment;
import android.os.StatFs;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * (四）使用CountDownLatch和AtomicLong实现多线程下的并发控制
 *
 * @描述：
 * @author：Michelle_Hong
 * @创建时间：2015-6-3下午5:14:10
 * @see
 */
public class ConcurrentTotalFileSizeWLatch {
	public static ExecutorService service;
	final private AtomicLong pendingFileVisits = new AtomicLong();
	final private AtomicLong totalSize = new AtomicLong();
	final private CountDownLatch latch = new CountDownLatch(1);


	public ConcurrentTotalFileSizeWLatch(ExecutorService executorService) {
		if(service == null && executorService != null){
			service = executorService;
		}
	}

	public static void setExecutor(ExecutorService executor){
		service = executor;
	}

	private void updateTotalSizeOfFilesInDir(final File file) {
		long fileSize = 0;
		if (file.isFile())
			fileSize = file.length();
		else {
			final File[] children = file.listFiles();
			if (children != null) {
				for (final File child : children) {
					if (child.isFile())
						fileSize += child.length();
					else {
						pendingFileVisits.incrementAndGet();
						service.execute(new Runnable() {
							public void run() {
								updateTotalSizeOfFilesInDir(child);
							}
						});
					}
				}
			}
		}
		totalSize.addAndGet(fileSize);
		if (pendingFileVisits.decrementAndGet() == 0)
			latch.countDown();
	}

	public long getTotalSizeOfFile(final String fileName)
			throws InterruptedException {
		if(service == null){
			service = Executors.newFixedThreadPool(10);
			Log2File.setExecutor(service);
		}
		pendingFileVisits.incrementAndGet();
		updateTotalSizeOfFilesInDir(new File(fileName));
		latch.await(100, TimeUnit.SECONDS);
		return totalSize.longValue();


	}

//	public static void main(final String[] args) throws InterruptedException {
//		final long start = System.nanoTime();
//		final long total = new ConcurrentTotalFileSizeWLatch()
//				.getTotalSizeOfFile(fileName);
//		final long end = System.nanoTime();
//		System.out.println("Total Size: " + total);
//		System.out.println("Time taken: " + (end - start) / 1.0e9);
//	}

	public static long getSDFreeSize(){
		//取得SD卡文件路径
		File path = Environment.getExternalStorageDirectory();
		StatFs sf = new StatFs(path.getPath());
		//获取单个数据块的大小(Byte)
		long blockSize = sf.getBlockSize();
		//空闲的数据块的数量
		long freeBlocks = sf.getAvailableBlocks();
		//返回SD卡空闲大小
		return freeBlocks * blockSize;  //单位Byte
		//return (freeBlocks * blockSize)/1024;   //单位KB
//        return (freeBlocks * blockSize)/1024 /1024; //单位MB
	}

}
