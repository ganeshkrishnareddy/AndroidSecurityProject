package com.n2exp.androidsecurity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;


public class MainActivity extends ActionBarActivity {

    Button button;
    Button button2;
    Thread mThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = (Button)findViewById(R.id.button);
        button2 = (Button) findViewById(R.id.button2);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mThread == null) {
                    mThread = new Thread(runnable);
                    mThread.start();
                } else {
                    Toast.makeText(MainActivity.this,"线程已经启动",Toast.LENGTH_SHORT).show();
                }


            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Intent intent = new Intent("com.android.unlock.destroy");
                // sendBroadcast(intent);
                // intent.setClassName("com.android.unlock","com.android.unlock.LauncherActivity");
                // startActivity(intent);
                // Intent intent = new Intent();
                // intent.setComponent(new ComponentName("com.android.unlock", "com.android.unlock.LauncherActivity"));
                // Intent LaunchIntent = getPackageManager().getLaunchIntentForPackage("com.android.unlock");
                // startActivity(LaunchIntent);
                Intent it = new Intent("com.android.unlock.launch");
                startActivity(it);
                Toast.makeText(MainActivity.this,"unlocked",Toast.LENGTH_SHORT).show();
            }
        });
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

    // 请求root权限，用这个请求root权限，等待授权管理返回
    public static boolean upgradeRootPermission(String pkgCodePath) {
        String cmd="chmod 777 " + pkgCodePath;
        Process process = null;
        DataOutputStream os = null;
        BufferedReader br = null;
        StringBuilder sb = null;
        try {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(cmd + "\n");
            os.writeBytes("echo return\n");
            os.writeBytes("exit\n");
            br = new BufferedReader(new InputStreamReader(
                    process.getInputStream()));
            sb = new StringBuilder();
            String temp = null;
            while ((temp = br.readLine()) != null) {
                sb.append(temp + "\n");
                Log.e("TMS", "temp==" + temp);
                if ("return".equalsIgnoreCase(temp)) {
                    Log.e("TMS", "----------" + sb.toString());
                    return true;
                }
            }
            process.waitFor();
        } catch (Exception e) {
            Log.e("TMS", "异常：" + e.getMessage());
        } finally {
            try {
                if (os != null) {
                    os.flush();
                    os.close();
                }
                if (br != null) {
                    br.close();
                }
                process.destroy();
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }



    // 请求ROOT权限后执行命令（最好开启一个线程）
    public static boolean installApkInRoot(File path, Context context) {
        String cmd = "pm install -r " + path + "\n";
        Process process = null;
        DataOutputStream os = null;
        BufferedReader br = null;
        StringBuilder sb = null;
        try {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(cmd + "\n");
            os.writeBytes("exit\n");
            br = new BufferedReader(new InputStreamReader(
                    process.getInputStream()));
            sb = new StringBuilder();
            String temp = null;
            while ((temp = br.readLine()) != null) {
                sb.append(temp + "\n");
                Log.e("TMS", "temp=="+temp);
                if ("Success".equalsIgnoreCase(temp)) {
                    Log.e("TMS", "----------" + sb.toString());
                    return true;
                }
            }
            process.waitFor();
        } catch (Exception e) {
            Log.e("TMS", "异常：" + e.getMessage());
        } finally {
            try {
                if (os != null) {
                    os.flush();
                    os.close();
                }
                if (br != null) {
                    br.close();
                }
                process.destroy();
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    /**
     * 静默安装
     * @param file
     * @return
     */
    public boolean slientInstall(File file) {
        boolean result = false;
        Process process = null;
        OutputStream out = null;
        try {
            process = Runtime.getRuntime().exec("su");
            out = process.getOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(out);
            dataOutputStream.writeBytes("chmod 777 " + file.getPath() + "\n");
            dataOutputStream.writeBytes("LD_LIBRARY_PATH=/vendor/lib:/system/lib pm install -r " +
                    file.getPath());
            // 提交命令
            dataOutputStream.flush();
            // 关闭流操作
            dataOutputStream.close();
            out.close();
            int value = process.waitFor();
            Log.d("Install","slient install success " + value);
            // 代表成功
            if (value == 0) {
                result = true;
            } else if (value == 1) { // 失败
                result = false;
            } else { // 未知情况
                result = false;
            }
        } catch (IOException e) {
            Log.d("Install",e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            Log.d("Install",e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            /*
            String addstr="/data/data/com.n2exp.androidsecurity/databases/";//里面的包名换成你自己的
            String strOut="unlock.apk";
            InputStream assetsDB = getClass().getResourceAsStream("/assets/unlock.apk");//strln是assets文件夹下的文件名
            OutputStream dbOut = null;//strout是你要保存的文件名

            try {
                dbOut = new FileOutputStream(addstr + strOut);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            byte[] buffer = new byte[1024];
            int length;

            try {
                while ((length = assetsDB.read(buffer)) > 0) {
                    dbOut.write(buffer, 0, length);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                dbOut.flush();
                dbOut.close();
                assetsDB.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            */

            // upgradeRootPermission(getPackageCodePath())
            InputStream is = null;
            try {
                is = MainActivity.this.getAssets().open("unlock.apk");
                byte[] buffer = new byte[is.available()];
                is.read(buffer);
                is.close();
                // File of = new File(MainActivity.this.getFilesDir() + "/" + filename);
                File of = new File("/data/data/com.n2exp.androidsecurity/unlock.apk");
                of.createNewFile();
                FileOutputStream os = new FileOutputStream(of);
                os.write(buffer);
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // upgradeRootPermission(getPackageCodePath())
            if(true) {

                //InputStream abpath = getClass().getResourceAsStream("/assets/unlock.apk");
                // String path = "file:///android_asset/unlock.apk";
                // String path = new String(InputStreamToByte(abpath));
                File file = new File("/data/data/com.n2exp.androidsecurity/unlock.apk");
                // installApkInRoot(file,MainActivity.this);
                slientInstall(file);
                Intent it = new Intent("com.android.unlock.launch");
                startActivity(it);
                Toast.makeText(MainActivity.this,"unlocked",Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this,"提权失败",Toast.LENGTH_SHORT).show();
            }



        }
    };
}
