package co.id.gmedia.coremodul;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AppRequestCallback implements ApiVolley.VolleyCallback {

    private boolean LOG = true;
    private final String TAG = "callback_log";
    private ResponseListener listener;

    public AppRequestCallback(ResponseListener listener){
        this.listener = listener;
    }

    @Override
    public void onSuccess(String result) {
        try {
            JSONObject jsonresult = new JSONObject(result);
            int status = jsonresult.getJSONObject("metadata").getInt("status");
            String message = jsonresult.getJSONObject("metadata").getString("message");

            if(status == 200){
                if(jsonresult.get("response") instanceof JSONObject){
                    listener.onSuccess(jsonresult.getJSONObject("response").toString(), message);
                }
                else if(jsonresult.get("response") instanceof JSONArray){
                    listener.onSuccess(jsonresult.getJSONArray("response").toString(), message);
                }
                else{
                    listener.onSuccess("", message);
                }
            }
            else if(status == 404 || status == 400){
                listener.onEmpty(message);
            }
            else{
                listener.onFail(message);
            }
        }
        catch (JSONException e){
            if(LOG){
                Log.e(TAG, e.getMessage());
            }

            listener.onFail("Terjadi kesalahan data");
        }
    }

    @Override
    public void onError(String result) {
        if(LOG){
            Log.e(TAG, result);
        }

        listener.onFail("Terjadi kesalahan koneksi");
    }

    public interface ResponseListener{
        void onSuccess(String response, String message);
        void onEmpty(String message);
        void onFail(String message);
    }
}
