package com.mi.elog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


/**
 * 
 * @描述：系统关机时广播接受处理
 * 		 检测日志状况，在满足条件时，删除处理日志文件
 * @author：Michelle_Hong 
 * @创建时间：2015-6-9上午10:07:40
 * @see
 */
public  class ClearLogFilesBroadcastReceiver extends BroadcastReceiver {
	private final String TAG = "--ClearLogFilesBroadcastReceiver--";

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		Log.d(TAG, "收到系统开机广播 in ClearLogFilesBroadcastReceiver  "+intent.getAction());
		ClearLogFilesService.startClearLogFiles(context);
	}

}
