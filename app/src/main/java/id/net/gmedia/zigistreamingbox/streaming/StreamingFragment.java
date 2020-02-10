package id.net.gmedia.zigistreamingbox.streaming;


import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.Settings;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import co.id.gmedia.coremodul.ApiVolley;
import co.id.gmedia.coremodul.AppRequestCallback;
import co.id.gmedia.coremodul.ItemValidation;
import id.net.gmedia.zigistreamingbox.MainActivity;
import id.net.gmedia.zigistreamingbox.R;
import id.net.gmedia.zigistreamingbox.RemoteUtils.ServiceUtils;
import id.net.gmedia.zigistreamingbox.utils.InternetCheck;
import id.net.gmedia.zigistreamingbox.utils.Url;

import static androidx.constraintlayout.widget.Constraints.TAG;
import static id.net.gmedia.zigistreamingbox.utils.Screen.getScreenWidth;

/**
 * A simple {@link Fragment} subclass.
 */
public class StreamingFragment extends Fragment  implements KategoriAdapter.KategoriAdapterCallback {

    LinearLayout llKategori;
    public static final String LOG_TAG = MainActivity.class.getSimpleName();
    public static List<KategoriModel> itemList = new ArrayList<>();
    public List<ItemModel> itemModels = new ArrayList<>();
    public KategoriAdapter kategoriAdapter;
    public ItemAdapter itemAdapter;
    Context context;
    String kategori;
    private ItemValidation iv = new ItemValidation();

    View view;

    public static String KEY_ACTIVITY = "fragment";

    //For remote
    private NsdManager mNsdManager;
    private ServerSocket serverSocket;
    private SocketServerThread socketServerThread;

    private WifiManager wifi;
    public static RecyclerView rvKategori;

    public StreamingFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view= inflater.inflate(R.layout.fragment_streaming, container, false);
        llKategori = view.findViewById(R.id.ll_kategori);

        ViewGroup.LayoutParams p_ll_kategori = llKategori.getLayoutParams();
        p_ll_kategori.width = getScreenWidth()/6;
        llKategori.setLayoutParams(p_ll_kategori);
        context = getContext();

        rvKategori = (RecyclerView)view.findViewById(R.id.rv_kategori);

        kategoriAdapter = new KategoriAdapter(getContext(), itemList , this);
        GridLayoutManager layoutManager = new GridLayoutManager(context,1);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvKategori.setLayoutManager(layoutManager);
        rvKategori.setItemAnimator(new DefaultItemAnimator());
        rvKategori.setAdapter(kategoriAdapter);
        initData();

        if (Build.VERSION.SDK_INT >= 21) {
            mNsdManager = (NsdManager) getActivity().getSystemService(Context.NSD_SERVICE);
            registerService(ServiceUtils.DEFAULT_PORT);
            initializeReceiver();
        }

        // item tv
        RecyclerView rv_item = (RecyclerView) view.findViewById(R.id.rv_item);
        itemAdapter = new ItemAdapter(context, itemModels);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(context, 4);
        rv_item.setLayoutManager(mLayoutManager);
        rv_item.addItemDecoration(new GridSpacingItemDecoration(4, dpToPx(10), true));
        rv_item.setItemAnimator(new DefaultItemAnimator());
        rv_item.setAdapter(itemAdapter);
        try {
            String message = getArguments().getString(KEY_ACTIVITY);
            if (message != null) {
                Toast.makeText(context, "Id "+message, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Kosong", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        initItemTv();

        return  view;
    }

    private void initData() {
        JSONObject jBody = new JSONObject();

        new ApiVolley(context, jBody, "GET", Url.getKategori,
                new AppRequestCallback(new AppRequestCallback.ResponseListener() {
                    @Override
                    public void onSuccess(String response, String message) {
                        itemList.clear();
                        try{
                            JSONArray obj = new JSONArray(response);
                            Log.d(LOG_TAG,">>>>"+obj);
                            for(int i = 0; i < obj.length(); i++){
                                JSONObject jadwal = obj.getJSONObject(i);
                                KategoriModel k = new KategoriModel(
                                        jadwal.getString("id")
                                        ,jadwal.getString("kategori")
                                );
                                itemList.add(k);
                            }
                            kategoriAdapter.notifyDataSetChanged();
                        }
                        catch (JSONException e){
                            Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();

                        }
                    }

                    @Override
                    public void onEmpty(String message) {

                        itemList.clear();
                        kategoriAdapter.notifyDataSetChanged();

                    }

                    @Override
                    public void onFail(String message) {
                        Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show();
                    }
                })
        );
    }

    @Override
    public void onRowKategoriCallback(String id_kategori) {
        kategori = id_kategori;
        getDataWithConnection();
    }

    public void changeKategori(String id_kategori){
        kategori = id_kategori;
        getDataWithConnection();
    }

    public static void process(){

    }

    private void initItemTv(){
        JSONObject jBody = new JSONObject();
        try {
            jBody.put("kategori",kategori);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        new ApiVolley(context, jBody, "POST", Url.getItemTV,
                new AppRequestCallback(new AppRequestCallback.ResponseListener() {
                    @Override
                    public void onSuccess(String response, String message) {
                        itemModels.clear();
                        try{
                            JSONArray obj = new JSONArray(response);
                            Log.d(LOG_TAG,">>>>"+obj);
                            for(int i = 0; i < obj.length(); i++){
                                JSONObject jadwal = obj.getJSONObject(i);
                                ItemModel m = new ItemModel(
                                        jadwal.getString("id")
                                        ,jadwal.getString("title")
                                        ,jadwal.getString("icon")
                                        ,jadwal.getString("url")
                                        ,jadwal.getString("kategori")
                                        ,jadwal.getString("package")
                                        ,jadwal.getString("url_playstore")
                                        ,jadwal.getString("url_web")
                                );
                                itemModels.add(m);
                            }
                            itemAdapter.notifyDataSetChanged();
                        }
                        catch (JSONException e){
                            Toast.makeText(context, "Error item", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onEmpty(String message) {

                        itemModels.clear();
                        kategoriAdapter.notifyDataSetChanged();

                    }

                    @Override
                    public void onFail(String message) {
                        Toast.makeText(context, "Failed item", Toast.LENGTH_SHORT).show();
                    }
                })
        );
    }

    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view);
            int column = position % spanCount;

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount;
                outRect.right = (column + 1) * spacing / spanCount;

                if (position < spanCount) {
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom

            } else {
                outRect.left = column * spacing / spanCount;
                outRect.right = spacing - (column + 1) * spacing / spanCount;
                if (position >= spanCount) {
                    outRect.top = spacing;
                }
            }
        }
    }

    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        int maxleng = (itemList != null) ? itemList.size() : 0;
//        KategoriModel item = itemList.get(KategoriAdapter.selectedPosition);
//        switch (keyCode){
//            case 19:
//                if(KategoriAdapter.selectedPosition - 1 >= 0){
//                    // Tombol atas
//                    KategoriAdapter.selectedPosition = kategoriAdapter.selectedPosition - 1;
//                    KategoriAdapter adapter = (KategoriAdapter) rvKategori.getAdapter();
//                    assert adapter != null;
//                    adapter.notifyDataSetChanged();
//                    rvKategori.smoothScrollToPosition(kategoriAdapter.selectedPosition);
//                }
//                break;
//            case 20:
//                if(KategoriAdapter.selectedPosition + 1 < maxleng){
//                    // tombol bawah
//                    KategoriAdapter.selectedPosition = KategoriAdapter.selectedPosition + 1;
//                    KategoriAdapter adapter = (KategoriAdapter) rvKategori.getAdapter();
//                    adapter.notifyDataSetChanged();
//                    rvKategori.smoothScrollToPosition(KategoriAdapter.selectedPosition);
//                }
//                break;
//            case 23:
//                Toast.makeText(context, "clicked", Toast.LENGTH_SHORT).show();
//                break;
//        }
//        return super.onKeyDown(keyCode, event);
//    }

    @Override
    public void onPause() {
        if (mNsdManager != null) {
            try{
                mNsdManager.unregisterService(mRegistrationListener);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        if(serverSocket != null){
            try {
                if (Build.VERSION.SDK_INT >= 21) {
                    serverSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        super.onPause();
    }

    @Override
    public void onStop() {

        if (mNsdManager != null) {
            try{
                mNsdManager.unregisterService(mRegistrationListener);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        if(serverSocket != null){
            try {
                if (Build.VERSION.SDK_INT >= 21) {
                    serverSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        super.onStop();
    }

    public void getDataWithConnection() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        //return activeNetworkInfo != null && activeNetworkInfo.isConnected();

        if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
            new InternetCheck(getActivity()).isInternetConnectionAvailable(new InternetCheck.InternetCheckListener() {

                @Override
                public void onComplete(boolean connected) {
                    if(connected){

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                initItemTv();
                            }
                        });
                    }else{
                        Snackbar.make(view.findViewById(android.R.id.content), R.string.wifi_not_connected,
                                Snackbar.LENGTH_INDEFINITE).setAction("OK",
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (wifi.isWifiEnabled() == false)
                                        {
                                            wifi.setWifiEnabled(true);
                                        }
                                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                                    }
                                }).show();
                    }
                }
            });
        } else {
            Log.d("conect error", "No network available!");
        }
    }

    // ===================================== Remote ================================

    NsdManager.RegistrationListener mRegistrationListener = new NsdManager.RegistrationListener() {

        @Override
        public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
            String mServiceName = NsdServiceInfo.getServiceName();
            ServiceUtils.SERVICE_NAME = mServiceName;
            Log.d(TAG, "Registered name : " + mServiceName);
        }

        @Override
        public void onRegistrationFailed(NsdServiceInfo serviceInfo,
                                         int errorCode) {
            // Registration failed! Put debugging code here to determine
            // why.
        }

        @Override
        public void onServiceUnregistered(NsdServiceInfo serviceInfo) {
            // Service has been unregistered. This only happens when you
            // call
            // NsdManager.unregisterService() and pass in this listener.
            Log.d(TAG,
                    "Service Unregistered : " + serviceInfo.getServiceName());
        }

        @Override
        public void onUnregistrationFailed(NsdServiceInfo serviceInfo,
                                           int errorCode) {
            // Unregistration failed. Put debugging code here to determine
            // why.
        }
    };


    public void registerService(int port) {
        NsdServiceInfo serviceInfo = new NsdServiceInfo();
        serviceInfo.setServiceName(ServiceUtils.SERVICE_NAME);
        serviceInfo.setServiceType(ServiceUtils.SERVICE_TYPE);

        try {
            serviceInfo.setHost(InetAddress.getByName(getMyIPAddress(true)));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        serviceInfo.setPort(port);

        try {
            mNsdManager.registerService(serviceInfo,
                    NsdManager.PROTOCOL_DNS_SD,
                    mRegistrationListener);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private static String getMyIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        boolean isIPv4 = sAddr.indexOf(':')<0;
                        //Log.d(TAG, "getIPAddress: "+ addr.getHostName());
                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim<0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) { } // for now eat exceptions
        return "";
    }

    private void initializeReceiver() {
        socketServerThread = new SocketServerThread();
        socketServerThread.start();
    }

    private class SocketServerThread extends Thread {

        @Override
        public void run() {

            Socket socket = null;
            DataInputStream dataInputStream = null;
            DataOutputStream dataOutputStream = null;

            try {
                Log.i(TAG, "Creating server socket");
                serverSocket = new ServerSocket(ServiceUtils.DEFAULT_PORT);

                while (true) {
                    socket = serverSocket.accept();
                    dataInputStream = new DataInputStream(
                            socket.getInputStream());
                    dataOutputStream = new DataOutputStream(
                            socket.getOutputStream());

                    String messageFromClient, messageToClient, request;

                    //If no message sent from client, this code will block the program
                    messageFromClient = dataInputStream.readUTF();

                    final JSONObject jsondata;

                    try {
                        jsondata = new JSONObject(messageFromClient);
                        request = jsondata.getString("request");

                        if (request.equals(ServiceUtils.REQUEST_CODE)) {

                            String clientIPAddress = jsondata.getString("ipAddress");
                            String typeCommand = jsondata.getString("type");

                            if(typeCommand.equals("request_connection")){

                                if(ServiceUtils.lockedClient.equals("") || ServiceUtils.lockedClient.equals(clientIPAddress)){

                                    ServiceUtils.lockedClient = clientIPAddress;
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(context, "Connected device " + ServiceUtils.lockedClient, Toast.LENGTH_LONG).show();
                                        }
                                    });
                                    messageToClient = "1";
                                }else{
                                    messageToClient = "0";
                                }
                                dataOutputStream.writeUTF(messageToClient);

                            } else if(typeCommand.equals("clear_connection")){

                                if(clientIPAddress.equals(ServiceUtils.lockedClient)){
                                    ServiceUtils.lockedClient = "";
                                    messageToClient = "1";
                                }else{
                                    messageToClient = "0";
                                }
                                dataOutputStream.writeUTF(messageToClient);
                            }else{

                                if(ServiceUtils.lockedClient.equals(clientIPAddress) || ServiceUtils.lockedClient.equals("")){
                                    ServiceUtils.lockedClient = clientIPAddress;
                                    String keyCode = jsondata.getString("keyCode");
                                    Log.d(TAG, "ip Client: " +clientIPAddress);
                                    // Add client IP to a list
                                    getAction(iv.parseNullInteger(keyCode));
                                    messageToClient = "Connection Accepted";
                                    dataOutputStream.writeUTF(messageToClient);
                                }else{
                                    messageToClient = "Connection Rejected ip not registered";
                                    dataOutputStream.writeUTF(messageToClient);
                                }
                            }
                        } else {
                            // There might be other queries, but as of now nothing.
                            dataOutputStream.flush();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Unable to get request");
                        dataOutputStream.flush();
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (dataInputStream != null) {
                    try {
                        dataInputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (dataOutputStream != null) {
                    try {
                        dataOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void getAction(final int keyCode){

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getActivity().dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, keyCode));
                getActivity().dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, keyCode));
            }
        });
    }

}
