package co.id.gmedia.coremodul;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class SessionManager {
    // Shared Preferences
    SharedPreferences pref;

    // Editor for Shared preferences
    SharedPreferences.Editor editor;

    // Context
    Context context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Sharedpref file name
    private static final String PREF_NAME = "GmediaTV";

    // All Shared Preferences Keys
    public static final String TAG_USERNAME = "username";
    public static final String TAG_PASSWORD= "password";
    public static final String TAG_NIK = "nik";
    public static final String TAG_NAMA = "nama";
    public static final String TAG_LEVEL = "level";
    public static final String TAG_LEVELNAMA = "level_nama";
    public static final String TAG_EMAIL = "email";
    public static final String TAG_FOTO = "foto";
    public static final String TAG_KONTAK = "kontak";
    public static final String TAG_ID = "id_record";
    public static final String TAG_FCMID ="fcm_id";
    public static final String TAG_COUNT_SLIDER = "count_slider";
    public static final String TAG_KATEGORI = "kategori";

    // Constructor
    public SessionManager(Context context){
        this.context = context;
        pref = this.context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    /**
     * Create login session
     * */
    public void createLoginSession(String id, String username, String nik, String nama, String level, String levelNama, String email, String foto, String kontak){

        editor.putString(TAG_ID, id);

        editor.putString(TAG_USERNAME, username);

        editor.putString(TAG_NIK, nik);

        editor.putString(TAG_NAMA, nama);

        editor.putString(TAG_LEVEL, level);

        editor.putString(TAG_LEVELNAMA, levelNama);

        editor.putString(TAG_EMAIL, email);

        editor.putString(TAG_FOTO, foto);

        editor.putString(TAG_KONTAK, kontak);

        // commit changes
        editor.commit();
    }

    public void saveSession(String nama, String kontak, String foto){

        editor.putString(TAG_NAMA, nama);

        editor.putString(TAG_FOTO, foto);

        editor.putString(TAG_KONTAK, kontak);

        // commit changes
        editor.commit();
    }

    public void saveKategoriLiveStreaming(String id_kategori){

        editor.putString(TAG_KATEGORI, id_kategori);

        // commit changes
        editor.commit();
    }


    public void saveFcmId(String fcm_id){

        editor.putString(TAG_FCMID, fcm_id);

        // commit changes
        editor.commit();
    }

    public void saveCountSlider(int count_slider){
        editor.putInt(TAG_COUNT_SLIDER,count_slider);
        editor.commit();
    }

    public void savePassword(String password){

        editor.putString(TAG_PASSWORD, password);

        editor.commit();
    }

    /**
     * Get stored session data
     * */

    public String getUserInfo(String key){
        return pref.getString(key, "");
    }

    public String getId(){
        return pref.getString(TAG_ID, "");
    }

    public String getUsername(){
        return pref.getString(TAG_USERNAME, "");
    }

    public String getNik(){
        return pref.getString(TAG_NIK, "");
    }

    public String getNama(){
        return pref.getString(TAG_NAMA, "");
    }

    public String getLevel(){
        return pref.getString(TAG_LEVEL, "");
    }

    public String getLevelNama(){
        return pref.getString(TAG_LEVELNAMA, "");
    }

    public String getEmail(){
        return pref.getString(TAG_EMAIL, "");
    }

    public String getFoto(){
        return pref.getString(TAG_FOTO, "");
    }

    public String getKontak(){
        return pref.getString(TAG_KONTAK, "");
    }

    public String getFcmid(){
        return pref.getString(TAG_FCMID, "");
    }

    public int getCountSlider(){
        return pref.getInt(TAG_COUNT_SLIDER, 0);
    }

    public String getKategoriLiveStreaming(){
        return pref.getString(TAG_KATEGORI, "");
    }

    /**
     * Clear session details
     * */
    public void logoutUser(Intent logoutIntent){

        // Clearing all data from Shared Preferences
        try {
            editor.clear();
            editor.commit();
        }catch (Exception e){
            e.printStackTrace();
        }

        logoutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(logoutIntent);
        ((Activity)context).finish();
        ((Activity)context).overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    /**
     * Quick check for login
     * **/
    // Get Login State
    public boolean isLoggedIn(){
        if(getUsername().isEmpty()){
            return false;
        }else{
            return true;
        }
        /*return pref.getBoolean(IS_LOGIN, false);*/
    }
}
