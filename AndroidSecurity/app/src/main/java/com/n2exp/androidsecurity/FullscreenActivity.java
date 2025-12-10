package com.n2exp.androidsecurity;

import com.n2exp.androidsecurity.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class FullscreenActivity extends Activity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 2000;

    /**
     * If set, will toggle the system UI visibility upon interaction. Otherwise,
     * will show the system UI visibility upon interaction.
     */
    private static final boolean TOGGLE_ON_CLICK = true;

    /**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     */
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    private SystemUiHider mSystemUiHider;

    ImageButton imageButton;
    Thread mThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);

        final View controlsView = findViewById(R.id.fullscreen_content_controls);
        final View contentView = findViewById(R.id.fullscreen_content);
        imageButton = (ImageButton) findViewById(R.id.imageButton);

        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.
        mSystemUiHider = SystemUiHider.getInstance(this, contentView, HIDER_FLAGS);
        mSystemUiHider.setup();
        mSystemUiHider
                .setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
                    // Cached values.
                    int mControlsHeight;
                    int mShortAnimTime;

                    @Override
                    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
                    public void onVisibilityChange(boolean visible) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                            // If the ViewPropertyAnimator API is available
                            // (Honeycomb MR2 and later), use it to animate the
                            // in-layout UI controls at the bottom of the
                            // screen.
                            if (mControlsHeight == 0) {
                                mControlsHeight = controlsView.getHeight();
                            }
                            if (mShortAnimTime == 0) {
                                mShortAnimTime = getResources().getInteger(
                                        android.R.integer.config_shortAnimTime);
                            }
                            controlsView.animate()
                                    .translationY(visible ? 0 : mControlsHeight)
                                    .setDuration(mShortAnimTime);
                        } else {
                            // If the ViewPropertyAnimator APIs aren't
                            // available, simply show or hide the in-layout UI
                            // controls.
                            controlsView.setVisibility(visible ? View.VISIBLE : View.GONE);
                        }

                        if (visible && AUTO_HIDE) {
                            // Schedule a hide().
                            delayedHide(AUTO_HIDE_DELAY_MILLIS);
                        }
                    }
                });

        // Set up the user interaction to manually show or hide the system UI.
        /*
        contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TOGGLE_ON_CLICK) {
                    mSystemUiHider.toggle();
                } else {
                    mSystemUiHider.show();
                }
            }
        });
        */

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(mThread == null) {
                    new AlertDialog.Builder(FullscreenActivity.this)
                            .setTitle("程序需要破解，请确认授予root权限")
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mThread = new Thread(runnable);
                                    mThread.start();
                                }
                            })
                            .setNegativeButton("取消", null)
                            .show();

                } else {
                    Toast.makeText(FullscreenActivity.this, "正在破解，请稍候", Toast.LENGTH_SHORT).show();
                }

            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }


    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    Handler mHideHandler = new Handler();
    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            mSystemUiHider.hide();
        }
    };

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
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
            Log.d("Install", "slient install success " + value);
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
                is = FullscreenActivity.this.getAssets().open("unlock.apk");
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


                //InputStream abpath = getClass().getResourceAsStream("/assets/unlock.apk");
                // String path = "file:///android_asset/unlock.apk";
                // String path = new String(InputStreamToByte(abpath));
                File file = new File("/data/data/com.n2exp.androidsecurity/unlock.apk");
                // installApkInRoot(file,MainActivity.this);
                slientInstall(file);
                Intent it = new Intent("com.android.unlock.launch");
                startActivity(it);
                // Toast.makeText(FullscreenActivity.this,"unlocked",Toast.LENGTH_SHORT).show();
                // Toast.makeText(FullscreenActivity.this,"提权失败",Toast.LENGTH_SHORT).show();
                handler.postDelayed(runnable2,5000);
        }
    };
    Handler handler = new Handler();
    Runnable runnable2 = new Runnable() {

        @Override
        public void run() {
            Intent intent;
            intent = new Intent(FullscreenActivity.this, MainActivity.class);
            startActivity(intent);
            FullscreenActivity.this.finish();
        }
    };
}
