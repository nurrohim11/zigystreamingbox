package id.net.gmedia.zigistreamingbox;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.media.AudioManager;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.rd.PageIndicatorView;

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
import java.util.Timer;
import java.util.TimerTask;

import co.id.gmedia.coremodul.ApiVolley;
import co.id.gmedia.coremodul.AppRequestCallback;
import co.id.gmedia.coremodul.CustomModel;
import co.id.gmedia.coremodul.ItemValidation;
import co.id.gmedia.coremodul.SessionManager;
import id.net.gmedia.zigistreamingbox.RemoteUtils.ServiceUtils;
import id.net.gmedia.zigistreamingbox.adapter.SliderHomeAdapter;
import id.net.gmedia.zigistreamingbox.live.LiveViewActivity;
import id.net.gmedia.zigistreamingbox.live.adapter.KategoriChannelAdapter;
import id.net.gmedia.zigistreamingbox.live.adapter.ListChannelIconAdapter;
import id.net.gmedia.zigistreamingbox.live.adapter.LiveItemAdapter;
import id.net.gmedia.zigistreamingbox.live.model.LiveItemModel;
import id.net.gmedia.zigistreamingbox.streaming.ItemAdapter;
import id.net.gmedia.zigistreamingbox.streaming.ItemModel;
import id.net.gmedia.zigistreamingbox.streaming.KategoriAdapter;
import id.net.gmedia.zigistreamingbox.streaming.KategoriModel;
import id.net.gmedia.zigistreamingbox.utils.SavedChanelManager;
import id.net.gmedia.zigistreamingbox.utils.ServerURL;

public class HomeActivity extends AppCompatActivity {


    RecyclerView rvMenu;
    LinearLayout llMenu;
    ImageView imgLogo;
    SessionManager sessionManager;
    private LinearLayout llHome, llStreaming, llLiveStreaming, llFcmId;
    private List<MenuModel> menuModels = new ArrayList<>();
    private List<SliderModel> sliderModels = new ArrayList<>();
    private List<CustomModel> masterList = new ArrayList<>();
    public SliderHomeAdapter sliderHomeAdapter;
    private ItemValidation iv = new ItemValidation();
    private AdapterMenuUtama mAdapter;
    private int savedC = 0;
    private int saveLive = 0;
    private int state_layar = 1;
    private NsdManager mNsdManager;

    public static List<KategoriModel> itemKategoriStreaming = new ArrayList<>();
    public List<ItemModel> itemModelTvStreaming = new ArrayList<>();
    public KategoriAdapter kategoriStreamingAdapter;
    public ItemAdapter itemStreamingAdapter;
    String kategori_streaming ="", kategori_live_streaming ="";
    private RecyclerView rvKategoriStreaming, rvitemKategoriStreaming, rvLiveStreaming;

    LiveItemAdapter liveItemAdapter;
    private SavedChanelManager chanelManager;
    private ProgressBar pbLoading;
    private SavedChanelManager savedChanel;
    private List<LiveItemModel> customItems = new ArrayList<>();

//    List<KategoriChannelModel> kategoriChannelModel = new ArrayList<>();
//    RecyclerView rvKategoriLiveStreaming;
//    KategoriChannelAdapter kategoriChannelAdapter;

    private static String TAG ="MainActivity>>";
    public static final String LOG_TAG = ">>>>>>>>>";

    //For remote
    private NsdManager mNsdManagerRemote;
    private ServerSocket serverSocket;
    private SocketServerThread socketServerThread;

    private WifiManager wifi;
    private  int id_menu=0;

    String device_token="";

    PageIndicatorView pageIndicatorView;
    ViewPager viewPager;
    private static int currentPage = 0;
    private static int NUM_PAGES = 0;
    private int count_slider= 0;

    TextView tvFcmId;
    int requestCodeStreaming =0;
    int requestCodeLiveStreaming =0;

    boolean setBack = false;
    int selectedItemLiveStreaming= 0;
    AudioManager audioManager;

    private InetAddress hostAddress;
    private int hostPort;

    public static String TAG_LINK ="";

    //    private int selectedTv =-1;
//    private String type ="";
    public static String fcm_client="";
    String link_tv ="";
    private static boolean isChannel =false;
    private RequestQueue queue;

    static boolean active_tv = false;
    Bundle bundles;
    String type_kategori_live="";
    String type_kategori_streaming="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        sessionManager = new SessionManager(this);

        if(sessionManager.getKategoriLiveStreaming().equals("0"))
            type_kategori_live="all";
        else
            type_kategori_live="";

        //ServiceUtils.DEFAULT_PORT = ConnectionUtil.getPort(ServerActivity.this);
        mNsdManager = (NsdManager) getSystemService(Context.NSD_SERVICE);
        registerService(ServiceUtils.DEFAULT_PORT);
        initializeReceiver();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);

        llMenu =findViewById(R.id.ll_menu);
        rvMenu = findViewById(R.id.rv_menu);
        imgLogo = findViewById(R.id.img_logo);

        llHome = findViewById(R.id.ll_home);
        llLiveStreaming = findViewById(R.id.ll_live_streaming);
        llStreaming = findViewById(R.id.ll_streaming);
        llFcmId = findViewById(R.id.ll_fcmid);
        tvFcmId = findViewById(R.id.tv_fcm);

        viewPager = findViewById(R.id.pager);
        pageIndicatorView = findViewById(R.id.pageIndicatorView);

        ServiceUtils.lockedClient = "";
        wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        mAdapter = new AdapterMenuUtama(this,menuModels);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        rvMenu.setLayoutManager(mLayoutManager);
        rvMenu.setItemAnimator(new DefaultItemAnimator());
        rvMenu.setAdapter(mAdapter);
//
        // load menu
//        initDatasetMenu();

        // == ========= ==inisialisasi kontent menu streaming == ========= ==
        // init kategori menu streaming
        rvKategoriStreaming = (RecyclerView)findViewById(R.id.rv_kategori_streaming);
        kategoriStreamingAdapter = new KategoriAdapter(this, itemKategoriStreaming );
        GridLayoutManager layoutManager = new GridLayoutManager(this,1);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvKategoriStreaming.setLayoutManager(layoutManager);
        rvKategoriStreaming.setItemAnimator(new DefaultItemAnimator());
        rvKategoriStreaming.setAdapter(kategoriStreamingAdapter);
        // init item tv kategori streaming

        // item tv
        rvitemKategoriStreaming = (RecyclerView) findViewById(R.id.rv_item_streaming);
        itemStreamingAdapter = new ItemAdapter(this, itemModelTvStreaming);
        RecyclerView.LayoutManager layoutManagerStreaming = new GridLayoutManager(this, 4);
        rvitemKategoriStreaming.setLayoutManager(layoutManagerStreaming);
        rvitemKategoriStreaming.addItemDecoration(new GridSpacingItemDecoration(4, dpToPx(10), true));
        rvitemKategoriStreaming.setItemAnimator(new DefaultItemAnimator());
        rvitemKategoriStreaming.setAdapter(itemStreamingAdapter);

        // == ========= ==inisialisasi kontent menu live streaming == ========= ==
        rvLiveStreaming = findViewById(R.id.rv_item_live_streaming);
        liveItemAdapter = new LiveItemAdapter(this, customItems);
        RecyclerView.LayoutManager gridLayoutManager = new GridLayoutManager(this, 5);
        rvLiveStreaming.setLayoutManager(gridLayoutManager);
        rvLiveStreaming.addItemDecoration(new GridSpacingItemDecoration(5, dpToPx(10), true));
        rvLiveStreaming.setItemAnimator(new DefaultItemAnimator());
        rvLiveStreaming.setAdapter(liveItemAdapter);


        llHome.setVisibility(View.INVISIBLE);
        llLiveStreaming.setVisibility(View.INVISIBLE);
        llStreaming.setVisibility(View.INVISIBLE);
        llFcmId.setVisibility(View.INVISIBLE);

        kategoriStreamingAdapter.selectedPosition = savedC;
        itemStreamingAdapter.selectedPosition = savedC;

//        getListChannel("");

        sliderHomeAdapter = new SliderHomeAdapter(this, sliderModels);
        initDataSlider();
        initSlider();

        ItemAdapter.selectedPosition =-1;
//        LiveItemAdapter.selectedPosition =-1;


        // TODO handling bundle
        bundles=  getIntent().getExtras();
        Log.d(">>>>>", String.valueOf(bundles));
        if(bundles != null){
            boolean bool = bundles.getBoolean("back");
            if(bundles.getBoolean("back")){
                setBack = true;
            }else{
                setBack = false;
            }
        }else{
            AdapterMenuUtama.selectedPosition=0;
        }

        Log.d(">>>>>", String.valueOf(setBack));
        if(setBack){
            llLiveStreaming.setVisibility(View.VISIBLE);
            llHome.setVisibility(View.INVISIBLE);
            llStreaming.setVisibility(View.INVISIBLE);
            llFcmId.setVisibility(View.INVISIBLE);
            AdapterMenuUtama.selectedPosition=1;
            id_menu = 2;
            state_layar =2;
        }else{
            llHome.setVisibility(View.VISIBLE);
            llLiveStreaming.setVisibility(View.INVISIBLE);
            llStreaming.setVisibility(View.INVISIBLE);
            llFcmId.setVisibility(View.INVISIBLE);
        }
    }

    // ===================================== Get Menu Utama ================================
    private void initDatasetMenu(){
        MenuModel m = new MenuModel(1,"Beranda", "001");
        menuModels.add(m);
        m = new MenuModel(3,"Streaming", "003");
        menuModels.add(m);
        m = new MenuModel(2,"TV", "002");
        menuModels.add(m);
//        m = new MenuModel(4,"Fcm Id","006");
//        menuModels.add(m);
//        AdapterMenuUtama.selectedPosition = savedC;
        mAdapter.notifyDataSetChanged();
    }

    // ===================================== Get Data Slider ================================
    private void initSlider() {

        viewPager.setAdapter(sliderHomeAdapter);

        pageIndicatorView.setViewPager(viewPager);

        final float density = getResources().getDisplayMetrics().density;

        pageIndicatorView.setRadius(5 * density);

        NUM_PAGES = sessionManager.getCountSlider();

        // Auto start of viewpager
        final Handler handler = new Handler();
        final Runnable Update = new Runnable() {
            public void run() {
                if (currentPage == NUM_PAGES) {
                    currentPage = 0;
                }
                viewPager.setCurrentItem(currentPage++, true);
            }
        };
        Timer swipeTimer = new Timer();
        swipeTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(Update);
            }
        }, 6000, 4000);

        // Pager listener over indicator
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }
            @Override
            public void onPageSelected(int position) {
                currentPage = position;
                pageIndicatorView.setSelection(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {/*empty*/}
        });
    }

    private void initDataSlider() {
        JSONObject jBody = new JSONObject();
        count_slider =0;

        new ApiVolley(this, jBody, "GET", ServerURL.get_slider,
                new AppRequestCallback(new AppRequestCallback.ResponseListener() {
                    @Override
                    public void onSuccess(String response, String message) {
                        sliderModels.clear();
                        try{
                            JSONArray obj = new JSONArray(response);
                            sessionManager.saveCountSlider(obj.length());
                            for(int i = 0; i < obj.length(); i++){
                                JSONObject d = obj.getJSONObject(i);
                                SliderModel s = new SliderModel(
                                        d.getString("id")
                                        ,d.getString("image")
                                        ,d.getString("url")
                                );
                                sliderModels.add(s);
                            }
                            sliderHomeAdapter.notifyDataSetChanged();
                        }
                        catch (JSONException e){
                            Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();

                        }
                    }

                    @Override
                    public void onEmpty(String message) {

                        sliderModels.clear();
                        sliderHomeAdapter.notifyDataSetChanged();

                    }

                    @Override
                    public void onFail(String message) {
//                        Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_SHORT).show();
                    }
                })
        );
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Toast.makeText(this, String.valueOf(keyCode), Toast.LENGTH_SHORT).show();
        return super.onKeyDown(keyCode, event);
    }

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

    NsdManager.RegistrationListener mRegistrationListener = new NsdManager.RegistrationListener() {

        @Override
        public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
            String mServiceName = NsdServiceInfo.getServiceName();
            ServiceUtils.SERVICE_NAME = mServiceName;
        }

        @Override
        public void onRegistrationFailed(NsdServiceInfo serviceInfo,
                                         int errorCode) {
        }

        @Override
        public void onServiceUnregistered(NsdServiceInfo serviceInfo) {
        }

        @Override
        public void onUnregistrationFailed(NsdServiceInfo serviceInfo,
                                           int errorCode) {
        }
    };

    @Override
    protected void onPause() {
        if (mNsdManager != null) {
            try{
                mNsdManager.unregisterService(mRegistrationListener);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        super.onPause();
    }

    @Override
    protected void onStop() {

        if (mNsdManager != null) {
            try{
                mNsdManager.unregisterService(mRegistrationListener);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mNsdManager != null) {
            registerService(ServiceUtils.DEFAULT_PORT);
        }

        initializeReceiver();
    }

    @Override
    protected void onDestroy() {

        if (mNsdManager != null) {
            try{
                mNsdManager.unregisterService(mRegistrationListener);
            }catch (Exception e){

                e.printStackTrace();
            }
        }

        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        super.onDestroy();
    }

    private void initializeReceiver() {
        socketServerThread = new SocketServerThread();
        socketServerThread.start();
    }

//    @Override
//    public void loadContent(int id) {
//
//    }
//
//    @Override
//    public void onRowKategoriLiveCallback(String id_kategori) {
//
//    }
//
//    @Override
//    public void onRowKategoriCallback(String type, String id_kategori) {
//
//    }

    private class SocketServerThread extends Thread {

        @Override
        public void run() {

            Socket socket = null;
            DataInputStream dataInputStream = null;
            DataOutputStream dataOutputStream = null;

            try {
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
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(HomeActivity.this, "Connected device " + ServiceUtils.lockedClient, Toast.LENGTH_LONG).show();
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

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, keyCode));
                dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, keyCode));
            }
        });
    }

    // ===================================== Beautify Grid ================================

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
}
