package com.android.unlock;

/**
 * Created by yezersky on 15-1-3.
 */
public class Data {
    private static boolean isInit = false;

    public static boolean getIsInit() {
        return isInit;
    }

    public static void setIsInit(boolean b) {
        Data.isInit = b;
    }
}
