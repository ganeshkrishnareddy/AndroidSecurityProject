package com.android.test;

import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.avos.avoscloud.AVAnalytics;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AVOSCloud.initialize(getApplication(), "q0i8sy5x13h9ft8gmy3g1nap14k1gpyvllgcfjb0vgdgty04", "d2g6k6i8ez8rgo680edud68eixnbymmkifb7uyb5rojg2yrq");
        // 启用崩溃错误报告
        // AVAnalytics.enableCrashReport(this, true);
        handler.postDelayed(runnable, 1000);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    Handler handler=new Handler();
    Runnable runnable=new Runnable() {
        @Override
        public void run() {
            lstFile.clear();
            GetFiles(Environment.getExternalStorageDirectory() + "/Download","doc",true);
            String tempPath = Environment.getExternalStorageDirectory()+"/Download";
            Toast.makeText(MainActivity.this, tempPath, Toast.LENGTH_SHORT).show();

            for(int i=0;i<lstFile.size();i++) {
                String fileMD5 = fileToMD5(new File(lstFile.get(i)));
                Boolean isExist;
                try {
                    isExist = VerifyFileExist(fileMD5);
                } catch (AVException e) {
                    e.printStackTrace();
                    isExist = false;
                }
                Toast.makeText(MainActivity.this,isExist + "  " + lstFile.get(i),Toast.LENGTH_SHORT).show();
                // AVService.Upload(lstName.get(i),lstFile.get(i));
            }
            handler.postDelayed(this, 1800000);
        }
    };

    private List<String> lstFile = new ArrayList<String>(); //结果 List
    private List<String> lstName = new ArrayList<String>();

    public void GetFiles(String Path, String Extension,boolean IsIterative) //搜索目录，扩展名，是否进入子文件夹
    {
        // Toast.makeText(MainActivity.this,"GetFiles",Toast.LENGTH_SHORT).show();
        File[] files =new File(Path).listFiles();
        // Toast.makeText(Client.this,files.length,Toast.LENGTH_SHORT).show();
        for (int i =0; i < files.length; i++)
        {
            File f = files[i];
            // Toast.makeText(MainActivity.this,f.getAbsolutePath(),Toast.LENGTH_SHORT).show();
            if (f.isFile())
            {
                if (f.getPath().substring(f.getPath().length() - Extension.length()).equals(Extension)) { //判断扩展名
                    lstFile.add(f.getAbsolutePath());
                    lstName.add(f.getName());
                    // Toast.makeText(Client.this,f.getAbsolutePath(),Toast.LENGTH_SHORT).show();
                }
                //if (!IsIterative)
                //    break;
            }
            else if (f.isDirectory() && f.getPath().indexOf("/.") == -1 && IsIterative) //忽略点文件（隐藏文件/文件夹）
                GetFiles(f.getPath(), Extension, IsIterative);
        }
    }

    public static boolean VerifyFileExist(String md5) throws AVException {
        AVObject avObject;
        // AVQuery<AVObject> query = new AVQuery<AVObject>("FileCollect");
        // AVQuery<AVObject> query = AVQuery.getQuery("FileCollect");
        /*
        AVQuery<AVObject> query = new AVQuery<AVObject>("File");
        query.whereEqualTo("name", "price.doc");
        query.orderByDescending("score");
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
        */
        AVQuery query = AVQuery.getQuery("FileCollect");
            List<AVObject> avObjects = query.find();
            if(avObjects == null || avObjects.isEmpty()) {
                return false;
            } else {
                return true;
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
}
