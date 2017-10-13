package com.mi.elog;

import android.content.Context;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import java.text.SimpleDateFormat;


/**
 * @文件名称：LogFormatter.java
 * @版权：
 * @创建时间：2015年5月28日下午5:58:11
 * @author Michelle_Hong
 * @描述：  该文件决定日志向文件输出时的格式
 * @修改人：
 * @修改时间：
 * @修改内容：
 */
public abstract class LogFormatter {
	
	
	 /**
	  * 格式化日志信息
	  * @param level 日志Level
	  * @param tag   标识日志的标签TAG
	  * @param msg	  想要记录的日志信息
	  * @param tr    想要记录的异常错误等
	  * @return
	  */
	  public abstract String format(Log.LEVEL level, String tag, String msg, Throwable tr);
	  
	  
	  
	  
	  public static class DefaultFormatter extends LogFormatter{
		private final SimpleDateFormat formatter;
		
		public DefaultFormatter(){
			formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		}
		
		public DefaultFormatter(String formatOfTime){
	            if (TextUtils.isEmpty(formatOfTime)){
					formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	            }else{
	                formatter = new SimpleDateFormat(formatOfTime);
	            }
	     }
		
		@Override
		public String format(Log.LEVEL level, String tag, String msg, Throwable tr) {
            if (level == null || TextUtils.isEmpty(tag) || TextUtils.isEmpty(msg)){
                return "";
            }

            StringBuffer buffer = new StringBuffer();
            buffer.append(level.getLevelString());
            buffer.append("\t");
            buffer.append(formatter.format(System.currentTimeMillis()));
            buffer.append("\t");
            buffer.append(tag);
            buffer.append("\t");
            buffer.append(msg);
            if (tr != null) {
                buffer.append(System.getProperty("line.separator"));
                buffer.append(android.util.Log.getStackTraceString(tr));
            }
            buffer.append("\r");
            return buffer.toString();
        }
		 
	  }
	
	  
	  
	  public static class ConsoleFormatter extends LogFormatter{
	        private final SimpleDateFormat formatter;

	        public ConsoleFormatter(){
	            formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");
	        }

	        public ConsoleFormatter(String formatOfTime){
	            if (TextUtils.isEmpty(formatOfTime)){
	                formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");
	            }else{
	                formatter = new SimpleDateFormat(formatOfTime);
	            }
	        }

	        @Override
	        public String format(Log.LEVEL level, String tag, String msg, Throwable tr) {
	            if (level == null || TextUtils.isEmpty(tag) || TextUtils.isEmpty(msg)){
	                return "";
	            }

	            StringBuffer buffer = new StringBuffer();
	            buffer.append(level.getLevelString());
	            buffer.append("\t");
	            buffer.append(formatter.format(System.currentTimeMillis()));
	            buffer.append("\t");
	            buffer.append(android.os.Process.myPid());
	            buffer.append("\t");
	            buffer.append(android.os.Process.myTid());
	            buffer.append("\t");
	            buffer.append(tag);
	            buffer.append("\t");
				buffer.append(msg+"\t\n");
//				buffer.append(msg+"\t");
	            if (tr != null) {
	                buffer.append(System.getProperty("line.separator"));
	                buffer.append(android.util.Log.getStackTraceString(tr));
					buffer.append("\n");
	            }

	            return buffer.toString();
	        }
	    }


	public static class ConsoleFormatterWithPackage extends LogFormatter{
		private final SimpleDateFormat formatter;
		private String packagename;

		public ConsoleFormatterWithPackage(Context context){
			formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");
			packagename = context.getPackageName();
		}

		public ConsoleFormatterWithPackage(String formatOfTime, Context context){
			if (TextUtils.isEmpty(formatOfTime)){
				formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");
			}else{
				formatter = new SimpleDateFormat(formatOfTime);
			}
			packagename = context.getPackageName();
		}

		@Override
		public String format(Log.LEVEL level, String tag, String msg, Throwable tr) {
			if (level == null || TextUtils.isEmpty(tag) || TextUtils.isEmpty(msg)){
				return "";
			}

			StringBuffer buffer = new StringBuffer();
			buffer.append(packagename);
			buffer.append("\t");
			buffer.append(level.getLevelString());
			buffer.append("\t");
			buffer.append(formatter.format(System.currentTimeMillis()));
			buffer.append("\t");
			buffer.append(android.os.Process.myPid());
			buffer.append("\t");
			buffer.append(android.os.Process.myTid());
			buffer.append("\t");
			buffer.append(tag);
			buffer.append("\t");
			buffer.append(msg+"\t\n");
//				buffer.append(msg+"\t");
			if (tr != null) {
				buffer.append(System.getProperty("line.separator"));
				buffer.append(android.util.Log.getStackTraceString(tr));
				buffer.append("\n");
			}

			return buffer.toString();
		}
	}
	
}
