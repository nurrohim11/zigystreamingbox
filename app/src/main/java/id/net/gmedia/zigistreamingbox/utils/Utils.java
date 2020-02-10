package id.net.gmedia.zigistreamingbox.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.util.ArrayList;
import java.util.List;

public class Utils {
    public static boolean isPackageInstalled(String packageName, PackageManager packageManager) {
        try {
            return packageManager.getApplicationInfo(packageName, 0).enabled;
        }
        catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
    public static List<String> getInstalledAppsPackageNameList(Context context){
        // Initialize a new intent
        Intent intent = new Intent(Intent.ACTION_MAIN,null);

        // Set intent category
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        // Set intent flags
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

        // Initialize a new list of resolve info
        List<ResolveInfo> resolveInfoList = context.getPackageManager().queryIntentActivities(intent,0);

        // Initialize a new list of package name
        List<String> packageNameList = new ArrayList<>();

        for(ResolveInfo resolveInfo: resolveInfoList){

            // Get the activity info from resolve info
            ActivityInfo activityInfo = resolveInfo.activityInfo;

            // Get the package name from activity info's application info
            // Add the package name to the list
            packageNameList.add(activityInfo.applicationInfo.packageName);
        }

        // Return the package name list
        return packageNameList;
    }
}
