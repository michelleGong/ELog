package com.mi.elog;

import android.text.TextUtils;

import java.security.InvalidParameterException;


/**
 * @文件名称：LogFilter.java
 * @版权：
 * @创建时间：2015年5月29日上午8:05:08
 * @author Michelle_Hong
 * @描述： 日志输出的过滤器定义
 * @修改人：
 * @修改时间：
 * @修改内容：
 */
public abstract class LogFilter {

	  /**
     * 如果返回true,则需要过滤这条日志，不进行输出.
     *
     * @param level
     * @param tag
     * @param msg
     * @return
     */
    public abstract boolean filter(Log.LEVEL level, String tag, String msg);

    /**
     * 标签过滤器
     * @author Michelle_Hong
     *
     */
    public static class TagFilter extends LogFilter {
        private String tag = null;

       
        private TagFilter() {
            throw new AssertionError();
        }

        /**
         *  在该构造方法中，设置需要记录日志的标签，如果标签为null或者为空字符，则不会过滤任何标签TAG
         * @param tag  不被过滤的标签TAG
         */
        public TagFilter(String tag) {
            this.tag = tag;
        }

        @Override
        public boolean filter(Log.LEVEL level, String tag, String msg) {
            if (TextUtils.isEmpty(this.tag)) {
                return false;
            }

            if (this.tag.equals(tag)) {
                return false;
            }

            return true;
        }

    }

    /**
     * 日志级别Level过滤器
     * @描述：根据日志的级别过滤的过滤器
     * @author Michelle_Hong
     * @创建时间：2015年5月29日上午8:24:17
     * @see
     */
    public static class LevelFilter extends LogFilter {
        private Log.LEVEL level = null;

    
        private LevelFilter() {
            throw new AssertionError();
        }


        /**
         * 在构造方法中设置日志级别，低于这个日志Level的日志会被过滤掉
         * @param 不被过滤的最小日志Level
         */
        public LevelFilter(Log.LEVEL level) {
            if (level == null) {
                throw new InvalidParameterException("level is null or not valid.");
            }

            this.level = level;
        }

        @Override
        public boolean filter(Log.LEVEL level, String tag, String msg) {
            return level.getLevel() < this.level.getLevel();
        }
    }

    /**
     * 
     * @描述：内容过滤器
     * @author：Michelle_Hong 
     * @创建时间：2015年5月29日上午8:27:35
     * @see
     */
    public static class ContentFilter extends LogFilter {
        private String msg = null;

   
        private ContentFilter() {
            throw new AssertionError();
        }

        public ContentFilter(String msg) {
            this.msg = msg;
        }

        @Override
        public boolean filter(Log.LEVEL level, String tag, String msg) {
            if (level == null || TextUtils.isEmpty(tag) || TextUtils.isEmpty(msg)) {
                return true;
            }

            if (TextUtils.isEmpty(this.msg)) {
                return false;
            }

            if (tag.contains(this.msg) || msg.contains(this.msg)) {
                return false;
            }

            return true;
        }
    }
}
