package com.android.unlock;

import android.content.Context;
import android.util.Log;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVAnalytics;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.SaveCallback;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Created by yezersky on 15-1-2.
 */
public class AVService {

    public static void AVInit(Context context) {
        AVOSCloud.initialize(context, "q0i8sy5x13h9ft8gmy3g1nap14k1gpyvllgcfjb0vgdgty04", "d2g6k6i8ez8rgo680edud68eixnbymmkifb7uyb5rojg2yrq");
        Data.setIsInit(true);
        // 启用崩溃错误报告
        AVAnalytics.enableCrashReport(context, true);
    }

    public static void AVOpen() {
        AVObject testObject = new AVObject("TestObject");
        testObject.put("foo", "bar");
        testObject.saveInBackground();
    }

    public static void Upload(final String fileMD5,final String name,final String path) {
        final AVFile avFile;
        // avFile = new AVFile("JobApplication", "hello world".getBytes());
        try {
            avFile = AVFile.withAbsoluteLocalPath(name, path);
            avFile.saveInBackground(new SaveCallback() {
                @Override
                public void done(AVException e) {
                    if(e != null) {

                    } else {
                        AVObject avObject = new AVObject("File");
                        avObject.put("md5", fileMD5);
                        avObject.put("name", name);
                        avObject.put("file", avFile);
                        avObject.saveInBackground();
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean VerifyFileExist(String md5) {
        AVObject avObject;
        // AVQuery<AVObject> query = new AVQuery<AVObject>("File");
        AVQuery<AVObject> query = AVQuery.getQuery("File");
        query.whereEqualTo("md5", md5);
        try {
            avObject = query.getFirst();
        } catch (AVException e) {
            avObject = null;
            e.printStackTrace();
            Log.d("失败", "查询错误: " + e.getMessage());
        }
        if(avObject == null) {
            return false;
        } else {
            return true;
        }
    }



}
