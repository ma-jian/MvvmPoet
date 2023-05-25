package com.mm.common.utils;

import android.os.Environment;
import android.text.TextUtils;

import com.mm.common.DefaultSDKInitialize;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * 本地日志
 */
public class LocalLog {
    private static final String MYLOGFILEName = "Log.log";// 本类输出的日志文件名称
    private static final SimpleDateFormat myLogSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);// 日志的输出格式
    private static final SimpleDateFormat logfile = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);// 日志文件格式

    private static String TAG = "LocalLog";

    public static void log(String msg) {
        writeLogFile("", msg);
    }


    public static void log(String packageName, String msg) {
        writeLogFile(packageName, msg);
    }

    /**
     * 打开日志文件并写入日志
     *
     * @param text
     */
    private static void writeLogFile(String packageName, String text) {// 新建或打开日志文件
        Date nowTime = new Date();
        String needWriteFile = logfile.format(nowTime);
        String needWriteMessage = myLogSdf.format(nowTime) + ":" + text;
        File cacheDir = DefaultSDKInitialize.mApplication.getExternalCacheDir();
        File dirsFile;
        if (TextUtils.isEmpty(packageName)) {
            dirsFile = new File(cacheDir + "/log");
        } else {
            dirsFile = new File(cacheDir + "/log/" + packageName);
        }
        if (!dirsFile.exists()) {
            dirsFile.mkdirs();
        }
        //Log.i("创建文件","创建文件");
        File file = new File(dirsFile, needWriteFile + "_" + MYLOGFILEName);// MYLOG_PATH_SDCARD_DIR
        if (!file.exists()) {
            try {
                //在指定的文件夹中创建文件
                file.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            FileWriter filerWriter = new FileWriter(file, true);// 后面这个参数代表是不是要接上文件中原来的数据，不进行覆盖
            BufferedWriter bufWriter = new BufferedWriter(filerWriter);
            bufWriter.write(needWriteMessage);
            bufWriter.newLine();
            bufWriter.close();
            filerWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除制定的日志文件
     */
    public static void delFile() {// 删除日志文件
        String needDelFile = logfile.format(getDateBefore());
        File dirPath = Environment.getExternalStorageDirectory();
        File file = new File(dirPath, needDelFile + MYLOGFILEName);// MYLOG_PATH_SDCARD_DIR
        if (file.exists()) {
            file.delete();
        }
    }

    /**
     * 得到现在时间前的几天日期，用来得到需要删除的日志文件名
     */
    private static Date getDateBefore() {
        Date nowtime = new Date();
        Calendar now = Calendar.getInstance();
        now.setTime(nowtime);
        // sd卡中日志文件的最多保存天数
        int SDCARD_LOG_FILE_SAVE_DAYS = 0;
        now.set(Calendar.DATE, now.get(Calendar.DATE) - SDCARD_LOG_FILE_SAVE_DAYS);
        return now.getTime();
    }
}