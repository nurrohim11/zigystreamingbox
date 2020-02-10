package id.net.gmedia.zigistreamingbox.utils;

import android.content.res.Resources;

public class Screen {

    public static int getScreenWidth() {
        int width = Resources.getSystem().getDisplayMetrics().widthPixels;
        return  width;
    }

    public static int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }
}
