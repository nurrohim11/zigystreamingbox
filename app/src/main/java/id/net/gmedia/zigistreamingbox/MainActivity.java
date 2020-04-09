package id.net.gmedia.zigistreamingbox;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Rect;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.format.Formatter;
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

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.rd.PageIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import co.id.gmedia.coremodul.ApiVolley;
import co.id.gmedia.coremodul.ApiVolley2;
import co.id.gmedia.coremodul.AppRequestCallback;
import co.id.gmedia.coremodul.CustomModel;
import co.id.gmedia.coremodul.ItemValidation;
import co.id.gmedia.coremodul.SessionManager;
import id.net.gmedia.zigistreamingbox.RemoteUtils.SelectedServer;
import id.net.gmedia.zigistreamingbox.RemoteUtils.ServiceUtils;
import id.net.gmedia.zigistreamingbox.adapter.SliderHomeAdapter;
import id.net.gmedia.zigistreamingbox.live.LiveViewActivity;
import id.net.gmedia.zigistreamingbox.live.adapter.KategoriChannelAdapter;
import id.net.gmedia.zigistreamingbox.live.model.KategoriChannelModel;
import id.net.gmedia.zigistreamingbox.live.model.LiveItemModel;
import id.net.gmedia.zigistreamingbox.live.adapter.LiveItemAdapter;
import id.net.gmedia.zigistreamingbox.streaming.ItemAdapter;
import id.net.gmedia.zigistreamingbox.streaming.ItemModel;
import id.net.gmedia.zigistreamingbox.streaming.KategoriAdapter;
import id.net.gmedia.zigistreamingbox.streaming.KategoriModel;
import id.net.gmedia.zigistreamingbox.utils.InternetCheck;
import id.net.gmedia.zigistreamingbox.utils.SavedChanelManager;
import id.net.gmedia.zigistreamingbox.utils.ServerURL;
import id.net.gmedia.zigistreamingbox.utils.Url;
import id.net.gmedia.zigistreamingbox.utils.Utils;

import static id.net.gmedia.zigistreamingbox.RemoteUtils.ServiceUtils.SERVICE_NAME;
import static id.net.gmedia.zigistreamingbox.RemoteUtils.ServiceUtils.SERVICE_TYPE;
import static id.net.gmedia.zigistreamingbox.utils.Utils.TAG_EMPTY;


public class MainActivity extends AppCompatActivity implements AdapterMenuUtama.MenuAdapterCallback, KategoriAdapter.KategoriAdapterCallback, KategoriChannelAdapter.KategoriAdapterCallback {

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

    KategoriChannelAdapter kategoriChannelAdapter;
    List<KategoriChannelModel> kategoriChannelModel = new ArrayList<>();
    RecyclerView rvKategoriLiveStreaming;

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
        setContentView(R.layout.activity_main);
        sessionManager = new SessionManager(MainActivity.this);

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // TODO mencari network untuk remote
        if(isNetworkAvailable()){
            initializeRegistrationListener();
        }

        if(sessionManager.getKategoriLiveStreaming().equals("0"))
            type_kategori_live="all";
        else
            type_kategori_live="";

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

        initIdKategoriFirst();

        mAdapter = new AdapterMenuUtama(MainActivity.this,menuModels,MainActivity.this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        rvMenu.setLayoutManager(mLayoutManager);
        rvMenu.setItemAnimator(new DefaultItemAnimator());
        rvMenu.setAdapter(mAdapter);

        // == ========= ==inisialisasi kontent menu streaming == ========= ==
        // init kategori menu streaming
        rvKategoriStreaming = (RecyclerView)findViewById(R.id.rv_kategori_streaming);
        kategoriStreamingAdapter = new KategoriAdapter(this, itemKategoriStreaming ,this);
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
        rvKategoriLiveStreaming = findViewById(R.id.rv_kategori_live_streaming);
        pbLoading = (ProgressBar) findViewById(R.id.pb_loading);
        chanelManager = new SavedChanelManager(MainActivity.this);
        savedChanel = new SavedChanelManager(MainActivity.this);
        savedC = 0;

        liveItemAdapter = new LiveItemAdapter(this, customItems);
        RecyclerView.LayoutManager gridLayoutManager = new GridLayoutManager(this, 4);
        rvLiveStreaming.setLayoutManager(gridLayoutManager);
        rvLiveStreaming.addItemDecoration(new GridSpacingItemDecoration(4, dpToPx(10), true));
        rvLiveStreaming.setItemAnimator(new DefaultItemAnimator());
        rvLiveStreaming.setAdapter(liveItemAdapter);

        kategoriChannelAdapter = new KategoriChannelAdapter(this, kategoriChannelModel ,this);
        GridLayoutManager layoutManagerKategoriLive = new GridLayoutManager(this,1);
        layoutManagerKategoriLive.setOrientation(LinearLayoutManager.VERTICAL);
        rvKategoriLiveStreaming.setLayoutManager(layoutManagerKategoriLive);
        rvKategoriLiveStreaming.setItemAnimator(new DefaultItemAnimator());
        rvKategoriLiveStreaming.setAdapter(kategoriChannelAdapter);
        initKategoriLiveStreaming();

        llHome.setVisibility(View.INVISIBLE);
        llLiveStreaming.setVisibility(View.INVISIBLE);
        llStreaming.setVisibility(View.INVISIBLE);
        llFcmId.setVisibility(View.INVISIBLE);

        kategoriStreamingAdapter.selectedPosition = savedC;
        itemStreamingAdapter.selectedPosition = savedC;

        // load menu
        initDatasetMenu();

        getListChannel("");

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
            state_layar =3;
        }else{
            llHome.setVisibility(View.VISIBLE);
            llLiveStreaming.setVisibility(View.INVISIBLE);
            llStreaming.setVisibility(View.INVISIBLE);
            llFcmId.setVisibility(View.INVISIBLE);
        }

    }

    // ===================================== SET FCM ID ================================
    private void fcmId() throws JSONException {
        JSONObject jBody = new JSONObject();
        jBody.put("fcm_id",device_token);
        new ApiVolley(this, jBody, "post", ServerURL.post_fcmid,
                new AppRequestCallback(new AppRequestCallback.ResponseListener() {
                    @Override
                    public void onSuccess(String response, String message) {
                    }
                    @Override
                    public void onEmpty(String message) {
                    }
                    @Override
                    public void onFail(String message) {
//                        Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_SHORT).show();
                    }
                })
        );

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
        }, 2000, 3000);

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

//        Log.d(LOG_TAG,sessionManager.getFcmid());

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

    // ===================================== Get Menu Utama ================================
    private void initDatasetMenu(){
        MenuModel m = new MenuModel(1,"Beranda", "001");
        menuModels.add(m);
        m = new MenuModel(2,"TV Streaming", "002");
        menuModels.add(m);
        m = new MenuModel(3,"Streaming", "003");
        menuModels.add(m);
//        m = new MenuModel(4,"Fcm Id","006");
//        menuModels.add(m);
//        AdapterMenuUtama.selectedPosition = savedC;
        mAdapter.notifyDataSetChanged();
    }

    // ===================================== Get kategori dan konten di menu streaming ================================
    private void initKategoriStreaming() {
        JSONObject jBody = new JSONObject();

        new ApiVolley(this, jBody, "GET", ServerURL.get_kategori_streaming,
                new AppRequestCallback(new AppRequestCallback.ResponseListener() {
                    @Override
                    public void onSuccess(String response, String message) {
                        itemKategoriStreaming.clear();
                        KategoriModel kar;
                        kar = new KategoriModel("0","All");
                        itemKategoriStreaming.add(kar);
                        try{
                            JSONArray obj = new JSONArray(response);
                            Log.d(TAG,">>>>"+obj);
                            for(int i = 0; i < obj.length(); i++){
                                JSONObject jadwal = obj.getJSONObject(i);
                                KategoriModel k = new KategoriModel(
                                        jadwal.getString("id")
                                        ,jadwal.getString("kategori")
                                );
                                itemKategoriStreaming.add(k);
                            }
                            kategoriStreamingAdapter.notifyDataSetChanged();
                        }
                        catch (JSONException e){
                            Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();

                        }
                    }

                    @Override
                    public void onEmpty(String message) {

                        itemKategoriStreaming.clear();
                        kategoriStreamingAdapter.notifyDataSetChanged();

                    }

                    @Override
                    public void onFail(String message) {
//                        Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_SHORT).show();
                    }
                })
        );
    }

    private void initItemTVStreaming(){
        JSONObject jBody = new JSONObject();
        try {
            jBody.put("type",type_kategori_streaming);
            jBody.put("kategori",kategori_streaming);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        new ApiVolley(this, jBody, "POST", ServerURL.get_konten_streaming,
                new AppRequestCallback(new AppRequestCallback.ResponseListener() {
                    @Override
                    public void onSuccess(String response, String message) {
                        itemModelTvStreaming.clear();
                        try{
                            JSONArray obj = new JSONArray(response);
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
                                itemModelTvStreaming.add(m);
                            }
                            itemStreamingAdapter.notifyDataSetChanged();
                        }
                        catch (JSONException e){
                            Toast.makeText(getApplicationContext(), "Error item", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onEmpty(String message) {

                        itemModelTvStreaming.clear();
                        itemStreamingAdapter.notifyDataSetChanged();

                    }

                    @Override
                    public void onFail(String message) {
                        Toast.makeText(getApplicationContext(), "Failed item", Toast.LENGTH_SHORT).show();
                    }
                })
        );
    }

    public void getDataItemTvStreamingWithConntection() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        //return activeNetworkInfo != null && activeNetworkInfo.isConnected();

        if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
            new InternetCheck(MainActivity.this).isInternetConnectionAvailable(new InternetCheck.InternetCheckListener() {

                @Override
                public void onComplete(boolean connected) {
                    if(connected){

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                initItemTVStreaming();
                            }
                        });
                    }else{
                        Snackbar.make(findViewById(android.R.id.content), R.string.wifi_not_connected,
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


    // ===================================== Get Konten Live Streaming ================================
    private void getListChannel(final String jenis) {
        JSONObject jbody = new JSONObject();

        try {
            jbody.put("type",type_kategori_live);
            jbody.put("kategori", sessionManager.getKategoriLiveStreaming());
            jbody.put("fcm_id", sessionManager.getFcmid());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(TAG, String.valueOf(jbody));
        new ApiVolley(this, jbody, "POST", ServerURL.get_channel,
                new AppRequestCallback(new AppRequestCallback.ResponseListener() {
                    @Override
                    public void onSuccess(String response, String message) {
//                        Log.d(TAG,response);
                        customItems.clear();
                        try{
                            JSONArray obj = new JSONArray(response);
                            if(jenis.equals("remote")){
                                link_tv = obj.getJSONObject(0).getString("link");
                                Log.d(TAG,link_tv);
                            }
                            for(int i = 0; i < obj.length(); i++){
                                JSONObject j = obj.getJSONObject(i);
                                LiveItemModel m = new LiveItemModel(
                                        j.getString("id"),
                                        j.getString("nama"),
                                        j.getString("link"),
                                        j.getString("icon")
                                );
                                customItems.add(m);
                            }
                            liveItemAdapter.notifyDataSetChanged();
                        }
                        catch (JSONException e){
                            Toast.makeText(getApplicationContext(), "Error item", Toast.LENGTH_SHORT).show();
                        }
                        isChannel = true;
                    }
                    @Override
                    public void onEmpty(String message) {

                        customItems.clear();
                        liveItemAdapter.notifyDataSetChanged();

                    }

                    @Override
                    public void onFail(String message) {
                        Toast.makeText(getApplicationContext(), "Failed item Live Streaming", Toast.LENGTH_SHORT).show();
                    }
                })
        );
    }

    private void initIdKategoriFirst(){
        JSONObject jbody = new JSONObject();
        new ApiVolley(this, jbody, "GET", ServerURL.get_id_kategori_first,
            new AppRequestCallback(new AppRequestCallback.ResponseListener() {
                @Override
                public void onSuccess(String response, String message) {
                    try{
                        JSONObject res = new JSONObject(response);
                        sessionManager.saveKategoriLiveStreaming(res.getString("id_kat"));
                    }
                    catch (JSONException e){
//                        Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onEmpty(String message) {
                }

                @Override
                public void onFail(String message) {
//                    Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_SHORT).show();
                }
            })
        );
    }

    private void initKategoriLiveStreaming() {
        JSONObject jBody = new JSONObject();

        new ApiVolley(this, jBody, "GET", ServerURL.get_kategori_channel,
                new AppRequestCallback(new AppRequestCallback.ResponseListener() {
                    @Override
                    public void onSuccess(String response, String message) {
                        kategoriChannelModel.clear();
                        KategoriChannelModel k_all;
                        try{
                            // TODO menambah kategori all
                            k_all = new KategoriChannelModel("0","All");
                            kategoriChannelModel.add(k_all);

                            JSONArray obj = new JSONArray(response);
                            Log.d(TAG,">>>>"+obj);
                            for(int i = 0; i < obj.length(); i++){
                                KategoriChannelModel k;
                                JSONObject jadwal = obj.getJSONObject(i);
                                k = new KategoriChannelModel(
                                        jadwal.getString("id")
                                        ,jadwal.getString("kategori")
                                );
                                kategoriChannelModel.add(k);
                            }
                            kategoriChannelAdapter.notifyDataSetChanged();
                        }
                        catch (JSONException e){
//                            Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();

                        }
                    }

                    @Override
                    public void onEmpty(String message) {

                        kategoriChannelModel.clear();
                        kategoriChannelAdapter.notifyDataSetChanged();

                    }

                    @Override
                    public void onFail(String message) {
//                        Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_SHORT).show();
                    }
                })
        );
    }

    @Override
    public void onRowKategoriLiveCallback(String id_kategori) {
        LiveItemAdapter.selectedPosition =0;
        sessionManager.saveKategoriLiveStreaming(id_kategori);
        getListChannel("");
    }


    // ===================================== Interface Menu Adapter ================================
    @Override
    public void loadContent(int id) {
        switch (id){
            case 1:
                llHome.setVisibility(View.VISIBLE);
                llLiveStreaming.setVisibility(View.INVISIBLE);
                llStreaming.setVisibility(View.INVISIBLE);
                llFcmId.setVisibility(View.INVISIBLE);
                initDataSlider();
                break;
            case 2:
                llHome.setVisibility(View.INVISIBLE);
                llLiveStreaming.setVisibility(View.VISIBLE);
                llStreaming.setVisibility(View.INVISIBLE);
                llFcmId.setVisibility(View.INVISIBLE);
                break;
            case 3:
                llHome.setVisibility(View.INVISIBLE);
                llLiveStreaming.setVisibility(View.INVISIBLE);
                llStreaming.setVisibility(View.VISIBLE);
                llFcmId.setVisibility(View.INVISIBLE);
                initKategoriStreaming();
                    initItemTVStreaming();
                break;
            case 4:
                llHome.setVisibility(View.INVISIBLE);
                llLiveStreaming.setVisibility(View.INVISIBLE);
                llStreaming.setVisibility(View.INVISIBLE);
                llFcmId.setVisibility(View.VISIBLE);
                break;
        }
    }

    // ===================================== Interface Kategori Adapter ================================
    @Override
    public void onRowKategoriCallback(String id_kategori) {
        kategori_streaming = id_kategori;
        getDataItemTvStreamingWithConntection();
    }

    private void onClickKategoriStreaming(String id_kategori){
        kategori_streaming = id_kategori;
        ItemAdapter.selectedPosition = 0;
        getDataItemTvStreamingWithConntection();
    }

    private void onClickKategoriLiveStreaming(String id_kategori){
        isChannel = false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode== 1){
            llHome.setVisibility(View.INVISIBLE);
            llLiveStreaming.setVisibility(View.VISIBLE);
            llStreaming.setVisibility(View.INVISIBLE);
            llFcmId.setVisibility(View.INVISIBLE);
            AdapterMenuUtama.selectedPosition=1;
            id_menu = 2;
            state_layar =3;
            Log.d(">>>>>", String.valueOf(requestCode));
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Toast.makeText(this, String.valueOf(keyCode)+"keydown", Toast.LENGTH_SHORT).show();
        int maxleng = (menuModels != null) ? menuModels.size() : 0;
        int maxleng_kategori_streaming = (itemKategoriStreaming != null) ? itemKategoriStreaming.size() : 0;
        int maxleng_kategori_live_streaming = (kategoriChannelModel != null) ? kategoriChannelModel.size() : 0;
        int maxleng_itemtv_streaming = (itemModelTvStreaming != null) ? itemModelTvStreaming.size() : 0;
        int maxleng_live_streaming = (customItems != null) ? customItems.size() : 0;
        MenuModel item = menuModels.get(AdapterMenuUtama.selectedPosition);

        KategoriModel menu_streaming;
        if(KategoriAdapter.selectedPosition ==-1){
            menu_streaming = itemKategoriStreaming.get(0);
        }else{
            menu_streaming = itemKategoriStreaming.get(KategoriAdapter.selectedPosition);
        }

        KategoriChannelModel menu_live_streaming;
        if(KategoriChannelAdapter.selectedPosition == -1){
            menu_live_streaming= kategoriChannelModel.get(0);
        }else{
            menu_live_streaming= kategoriChannelModel.get(KategoriChannelAdapter.selectedPosition);
        }
        TAG_LINK ="";
        switch (keyCode){
            case 4:
                link_tv =TAG_EMPTY;
                if(state_layar == 2){
                    if(id_menu==3){
                        KategoriAdapter.selectedPosition=-1;
                        KategoriAdapter a = (KategoriAdapter) rvKategoriStreaming.getAdapter();
                        a.notifyDataSetChanged();
                        ItemAdapter adapter = (ItemAdapter) rvitemKategoriStreaming.getAdapter();
                        ItemAdapter.selectedPosition =-1;
                        adapter.notifyDataSetChanged();
                        type_kategori_streaming="";
                    }else if(id_menu ==2){
                        KategoriChannelAdapter.selectedPosition = -1;
                        KategoriChannelAdapter a = (KategoriChannelAdapter) rvKategoriLiveStreaming.getAdapter();
                        a.notifyDataSetChanged();
                        LiveItemAdapter adapter = (LiveItemAdapter) rvLiveStreaming.getAdapter();
                        LiveItemAdapter.selectedPosition =-1;
                        adapter.notifyDataSetChanged();
                        type_kategori_live="";
                    }
                    state_layar =1;
                }else if(state_layar == 3) {
                    state_layar = 2;
                    if(id_menu == 3){
                        ItemAdapter adapter = (ItemAdapter) rvitemKategoriStreaming.getAdapter();
                        ItemAdapter.selectedPosition =-1;
                        adapter.notifyDataSetChanged();
                    }else if(id_menu ==2){
                        LiveItemAdapter adapter = (LiveItemAdapter) rvLiveStreaming.getAdapter();
                        LiveItemAdapter.selectedPosition =-1;
                        adapter.notifyDataSetChanged();
                    }
                }
                LiveItemAdapter.sendData(MainActivity.fcm_client,TAG_EMPTY,MainActivity.this);
                return false;
            case 19:
                // tombol atas
                if(state_layar == 1){
                    link_tv =TAG_EMPTY;
                    if(AdapterMenuUtama.selectedPosition - 1 >= 0){
                        AdapterMenuUtama.selectedPosition = mAdapter.selectedPosition - 1;
                        AdapterMenuUtama adapter = (AdapterMenuUtama) rvMenu.getAdapter();
                        assert adapter != null;
                        adapter.notifyDataSetChanged();
                        rvMenu.smoothScrollToPosition(mAdapter.selectedPosition);
                    }
                }else if(state_layar == 2){
                    link_tv =TAG_EMPTY;
                    if(id_menu == 3){
                        link_tv ="";
//                        sendData(fcm_client);
                        if(KategoriAdapter.selectedPosition - 1 >= 0){
                            KategoriAdapter.selectedPosition = KategoriAdapter.selectedPosition - 1;
                            KategoriAdapter a = (KategoriAdapter) rvKategoriStreaming.getAdapter();
                            a.notifyDataSetChanged();
                            rvKategoriStreaming.smoothScrollToPosition(KategoriAdapter.selectedPosition);
                        }
                    }else if(id_menu ==2){
                        link_tv =TAG_EMPTY;
//                        sendData(fcm_client);
                        if(KategoriChannelAdapter.selectedPosition - 1 >= 0){
                            KategoriChannelAdapter.selectedPosition = KategoriChannelAdapter.selectedPosition - 1;
                            KategoriChannelAdapter a = (KategoriChannelAdapter) rvKategoriLiveStreaming.getAdapter();
                            a.notifyDataSetChanged();
                            rvKategoriLiveStreaming.smoothScrollToPosition(KategoriChannelAdapter.selectedPosition);
                        }
                    }
                }else if(state_layar == 3){
                    if(id_menu ==3){
                        link_tv =TAG_EMPTY;
//                        sendData(fcm_client);
                        if(ItemAdapter.selectedPosition - 4 >= 0){
                            ItemAdapter.selectedPosition = ItemAdapter.selectedPosition - 4;
                            ItemAdapter adapter = (ItemAdapter) rvitemKategoriStreaming.getAdapter();
                            adapter.notifyDataSetChanged();
                            rvitemKategoriStreaming.smoothScrollToPosition(ItemAdapter.selectedPosition);
                        }
                    }else if(id_menu ==2){
                        if(LiveItemAdapter.selectedPosition - 4 >= 0){
                            LiveItemAdapter.selectedPosition = LiveItemAdapter.selectedPosition - 4;
                            LiveItemModel model = customItems.get(LiveItemAdapter.selectedPosition);
                            link_tv = model.getLink();
                            LiveItemAdapter adapter = (LiveItemAdapter) rvLiveStreaming.getAdapter();
                            adapter.notifyDataSetChanged();
                            rvLiveStreaming.smoothScrollToPosition(LiveItemAdapter.selectedPosition);
                        }
                    }
                }
                break;
            case 20:
                // tombol bawah
                if(state_layar == 1){
                    link_tv =TAG_EMPTY;
//                    sendData(fcm_client);
                    if(AdapterMenuUtama.selectedPosition + 1 < maxleng){
                        AdapterMenuUtama.selectedPosition = AdapterMenuUtama.selectedPosition + 1;
                        AdapterMenuUtama adapter = (AdapterMenuUtama) rvMenu.getAdapter();
                        adapter.notifyDataSetChanged();
                        rvMenu.smoothScrollToPosition(AdapterMenuUtama.selectedPosition);
                    }
                }else if(state_layar ==2){
                    link_tv=TAG_EMPTY;
                    if(id_menu == 3){
//                        sendData(fcm_client);
                        if(KategoriAdapter.selectedPosition + 1 < maxleng_kategori_streaming){
                            KategoriAdapter.selectedPosition = KategoriAdapter.selectedPosition + 1;
                            KategoriAdapter adapter = (KategoriAdapter) rvKategoriStreaming.getAdapter();
                            adapter.notifyDataSetChanged();
                            rvKategoriStreaming.smoothScrollToPosition(KategoriAdapter.selectedPosition);
                        }
                    }else if(id_menu ==2){
//                        sendData(fcm_client);
                        if(KategoriChannelAdapter.selectedPosition + 1 < maxleng_kategori_live_streaming){
                            KategoriChannelAdapter.selectedPosition = KategoriChannelAdapter.selectedPosition + 1;
                            KategoriChannelAdapter adapter = (KategoriChannelAdapter) rvKategoriLiveStreaming.getAdapter();
                            adapter.notifyDataSetChanged();
                            rvKategoriLiveStreaming.smoothScrollToPosition(KategoriChannelAdapter.selectedPosition);
                        }
                    }
                }else if(state_layar==3){
                    if(id_menu ==3){
                        link_tv=TAG_EMPTY;
//                        sendData(fcm_client);
                        if(ItemAdapter.selectedPosition + 4 < maxleng_itemtv_streaming){
                            ItemAdapter.selectedPosition = ItemAdapter.selectedPosition + 4;
                            ItemAdapter adapter = (ItemAdapter) rvitemKategoriStreaming.getAdapter();
                            adapter.notifyDataSetChanged();
                            rvitemKategoriStreaming.smoothScrollToPosition(ItemAdapter.selectedPosition);
                        }
                    }else if(id_menu == 2){
                        if(LiveItemAdapter.selectedPosition + 4 < maxleng_live_streaming){
                            LiveItemAdapter.selectedPosition = LiveItemAdapter.selectedPosition + 4;
                            LiveItemModel model = customItems.get(LiveItemAdapter.selectedPosition);
                            link_tv = model.getLink();
                            LiveItemAdapter adapter = (LiveItemAdapter) rvLiveStreaming.getAdapter();
                            adapter.notifyDataSetChanged();
                            rvLiveStreaming.smoothScrollToPosition(LiveItemAdapter.selectedPosition);
                        }
                    }
                }
                break;
            case 21:
                // tombol kiri
                if(state_layar ==3 ){
                    if(id_menu == 3){
                        link_tv= TAG_EMPTY;
//                        sendData(fcm_client);
                        if(ItemAdapter.selectedPosition - 1 >= 0){
                            ItemAdapter.selectedPosition = ItemAdapter.selectedPosition - 1;
                            ItemAdapter adapter = (ItemAdapter) rvitemKategoriStreaming.getAdapter();
                            adapter.notifyDataSetChanged();
                            rvitemKategoriStreaming.smoothScrollToPosition(ItemAdapter.selectedPosition);
                        }
                    }else if(id_menu ==2){
                        if(LiveItemAdapter.selectedPosition - 1 >= 0){
                            LiveItemAdapter.selectedPosition = LiveItemAdapter.selectedPosition - 1;
                            LiveItemModel model = customItems.get(LiveItemAdapter.selectedPosition);
                            link_tv = model.getLink();
                            LiveItemAdapter adapter = (LiveItemAdapter) rvLiveStreaming.getAdapter();
                            adapter.notifyDataSetChanged();
                            rvLiveStreaming.smoothScrollToPosition(LiveItemAdapter.selectedPosition);
                        }
                    }
                }else if(state_layar == 2){
                    if(id_menu ==2) {
                        link_tv=TAG_EMPTY;
//                        sendData(fcm_client);
                        if (LiveItemAdapter.selectedPosition - 1 >= 0) {
                            if (LiveItemAdapter.selectedPosition - 1 >= 0) {
                                LiveItemAdapter.selectedPosition = LiveItemAdapter.selectedPosition - 1;
                                LiveItemAdapter adapter = (LiveItemAdapter) rvLiveStreaming.getAdapter();
                                adapter.notifyDataSetChanged();
                                rvLiveStreaming.smoothScrollToPosition(LiveItemAdapter.selectedPosition);
                            }
                        }
                    }
                }
                break;
            case 22:
                // tombol kanan
                if(state_layar == 3){
                    if(id_menu == 3){
                        link_tv=TAG_EMPTY;
//                        sendData(fcm_client);
                        if(ItemAdapter.selectedPosition + 1 < maxleng_itemtv_streaming){
                            ItemAdapter.selectedPosition = ItemAdapter.selectedPosition + 1;
                            ItemAdapter adapter = (ItemAdapter) rvitemKategoriStreaming.getAdapter();
                            adapter.notifyDataSetChanged();
                            rvitemKategoriStreaming.smoothScrollToPosition(ItemAdapter.selectedPosition);
                        }
                    }else if(id_menu == 2){
                        if(LiveItemAdapter.selectedPosition + 1 < maxleng_live_streaming){
                            LiveItemAdapter.selectedPosition = LiveItemAdapter.selectedPosition + 1;
                            LiveItemModel model = customItems.get(LiveItemAdapter.selectedPosition);
                            link_tv = model.getLink();
                            LiveItemAdapter adapter = (LiveItemAdapter) rvLiveStreaming.getAdapter();
                            adapter.notifyDataSetChanged();
                            rvLiveStreaming.smoothScrollToPosition(LiveItemAdapter.selectedPosition);
                        }
                    }
                }
                break;
            case 23:
                // tombol ok
                id_menu = item.getId();
                if(state_layar == 1){
                    link_tv=TAG_EMPTY;
//                    sendData(fcm_client);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    if(id_menu == 1){
                        state_layar=1;
                        llHome.setVisibility(View.VISIBLE);
                        llLiveStreaming.setVisibility(View.INVISIBLE);
                        llStreaming.setVisibility(View.INVISIBLE);
                        llFcmId.setVisibility(View.INVISIBLE);
                    }else if(id_menu == 2){
                        state_layar = 2;
                        llHome.setVisibility(View.INVISIBLE);
                        llLiveStreaming.setVisibility(View.VISIBLE);
                        llStreaming.setVisibility(View.INVISIBLE);
                        llFcmId.setVisibility(View.INVISIBLE);
                        KategoriChannelAdapter.selectedPosition=0;
                        KategoriChannelAdapter a = (KategoriChannelAdapter) rvKategoriLiveStreaming.getAdapter();
                        a.notifyDataSetChanged();
                        KategoriChannelModel model = kategoriChannelModel.get(0);
                        sessionManager.saveKategoriLiveStreaming(model.getId());
                        type_kategori_live="all";
                        getListChannel("");
                    }else if(id_menu == 3){
                        ItemAdapter.selectedPosition =-1;
                        ItemAdapter adapter = (ItemAdapter) rvitemKategoriStreaming.getAdapter();
                        adapter.notifyDataSetChanged();
                        KategoriAdapter.selectedPosition=0;
                        KategoriAdapter a = (KategoriAdapter) rvKategoriStreaming.getAdapter();
                        a.notifyDataSetChanged();
                        llHome.setVisibility(View.INVISIBLE);
                        llLiveStreaming.setVisibility(View.INVISIBLE);
                        llStreaming.setVisibility(View.VISIBLE);
                        llFcmId.setVisibility(View.INVISIBLE);
                        state_layar=2;
                        type_kategori_streaming="all";
                        initItemTVStreaming();
                    }else if(id_menu ==4) {
                        llHome.setVisibility(View.INVISIBLE);
                        llLiveStreaming.setVisibility(View.INVISIBLE);
                        llStreaming.setVisibility(View.INVISIBLE);
                        llFcmId.setVisibility(View.VISIBLE);
                        state_layar =1;
                    }
                }
                else if(state_layar == 2){
                    switch (id_menu){
                        case 2:
                            if(menu_live_streaming.getId().equals("0")){
//                                isChannel = false;
                                state_layar = 3;
                                sessionManager.saveKategoriLiveStreaming(menu_live_streaming.getId());
                                LiveItemAdapter.selectedPosition =0;
                                type_kategori_live="all";
                                getListChannel("remote");
                            }else{
//                                isChannel = false;
                                state_layar = 3;
                                sessionManager.saveKategoriLiveStreaming(menu_live_streaming.getId());
                                LiveItemAdapter.selectedPosition =0;
                                type_kategori_live="";
                                getListChannel("remote");
                            }
                            break;
                        case 3:
                            if(menu_streaming.getId().equals("0")){
                                type_kategori_streaming="all";
                                link_tv=TAG_EMPTY;
                                state_layar =3;
                                onClickKategoriStreaming(menu_streaming.getId());
                                break;
                            }else{
                                type_kategori_streaming="";
                                link_tv=TAG_EMPTY;
                                state_layar =3;
                                onClickKategoriStreaming(menu_streaming.getId());
                                break;
                            }
                    }
                }
                else if(state_layar ==3){
                    final List<String> installedPackages = Utils.getInstalledAppsPackageNameList(MainActivity.this);
                    switch (id_menu){
                        case 3:
                            link_tv=TAG_EMPTY;
//                            sendData(fcm_client);
                            ItemModel m = itemModelTvStreaming.get(ItemAdapter.selectedPosition);
                            if(installedPackages.contains(m.getM_package())){
                                Intent launchIntent = MainActivity.this.getPackageManager().getLaunchIntentForPackage(m.getM_package());
                                MainActivity.this.startActivity( launchIntent );
                            }else {
                                if(m.getM_package().isEmpty()){
                                    if(m.getUrl_playstore().isEmpty()){
                                        if(m.getUrl_web().isEmpty()){
                                            Toast.makeText(MainActivity.this, "Paket tidak ditemukan !!..", Toast.LENGTH_SHORT).show();
                                        }else{
                                            Intent httpIntent = new Intent(Intent.ACTION_VIEW);
                                            httpIntent.setData(Uri.parse(m.getUrl_web()));
                                            MainActivity.this.startActivity(httpIntent);
                                        }
                                    }else{
                                        try {
                                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id="+m.getM_package())));
                                        } catch (android.content.ActivityNotFoundException anfe) {
                                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id="+m.getM_package())));
                                        }
                                    }
                                }else{
                                    try {
                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id="+m.getM_package())));
                                    } catch (android.content.ActivityNotFoundException anfe) {
                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id="+m.getM_package())));
                                    }
                                }
                            }
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                            break;
                        case 2:
                            setBack = false;
                            LiveItemModel model = customItems.get(LiveItemAdapter.selectedPosition);
                            link_tv = model.getLink();
                            Intent intent = new Intent(MainActivity.this, LiveViewActivity.class);
                            intent.putExtra("nama", model.getNama());
                            intent.putExtra("link", model.getLink());
                            startActivity(intent);
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                            break;
                    }
                }
                break;
//            case 24:
//                audioManager.adjustStreamVolume(
//                        AudioManager.STREAM_MUSIC,
//                        AudioManager.ADJUST_RAISE,
//                        AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);
//                link_tv=TAG_EMPTY;
//                break;
//            case 25 :
//                audioManager.adjustStreamVolume(
//                        AudioManager.STREAM_MUSIC,
//                        AudioManager.ADJUST_LOWER,
//                        AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);
//                link_tv=TAG_EMPTY;
//                break;
//
        }
        return super.onKeyDown(keyCode, event);
    }
//
//    @Override
//    public boolean onKeyUp(int keyCode, KeyEvent event) {
//        Toast.makeText(this, String.valueOf(keyCode)+"keyup", Toast.LENGTH_SHORT).show();
//        return super.onKeyUp(keyCode, event);
//    }

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

    // ===================================== Remote ================================

    NsdManager.RegistrationListener mRegistrationListener = new NsdManager.RegistrationListener() {

        @Override
        public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
            String mServiceName = NsdServiceInfo.getServiceName();
            SERVICE_NAME = mServiceName;
            Log.d(TAG, "Registered name : " + NsdServiceInfo.getPort());
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
        serviceInfo.setServiceName(SERVICE_NAME);
        serviceInfo.setServiceType(SERVICE_TYPE);

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
            mNsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
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
                        Log.d(">>>>>", String.valueOf(jsondata));
                        fcm_client = jsondata.getString("fcm_client");

                        if (request.equals(ServiceUtils.REQUEST_CODE)) {

                            String clientIPAddress = jsondata.getString("ipAddress");
                            String typeCommand = jsondata.getString("type");

                            if(typeCommand.equals("request_connection")){

                                if(ServiceUtils.lockedClient.equals("") || ServiceUtils.lockedClient.equals(clientIPAddress)){

                                    ServiceUtils.lockedClient = clientIPAddress;
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(MainActivity.this, "Connected device " + ServiceUtils.lockedClient, Toast.LENGTH_LONG).show();
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
//                                    Log.d(TAG, "ip Client: " +clientIPAddress);
                                    // Add client IP to a list
                                    getAction(iv.parseNullInteger(keyCode));
                                    JSONObject jsonObject = new JSONObject();
                                    jsonObject.put("status",String.valueOf(1));

                                    messageToClient = jsonObject.toString();
//                                    Log.d(TAG, "run: " + messageToClient);
                                    dataOutputStream.writeUTF(messageToClient);
                                }else{
                                    JSONObject jsonObject = new JSONObject();
                                    jsonObject.put("status",String.valueOf(0));
                                    jsonObject.put("link",TAG_LINK);
                                    messageToClient =  jsonObject.toString();
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

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, keyCode));
                dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, keyCode));
            }
        });
    }

//    private void sendData(String fcm_client){
//        JSONObject jsonObject = new JSONObject();
//        try {
//            jsonObject.put("to",fcm_client);
//            JSONObject obj1 = new JSONObject();
//            obj1.put("jenis",link_tv);
//            jsonObject.put("data",obj1);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        Log.d(TAG,String.valueOf(jsonObject));
//        new ApiVolley(this, jsonObject, "POST", Url.base_url_fcm,"1",
//                new AppRequestCallback(new AppRequestCallback.ResponseListener() {
//                    @Override
//                    public void onSuccess(String response, String message) {
//                        Log.d(TAG,"berhasil");
//                    }
//                    @Override
//                    public void onEmpty(String message) {
//                    }
//
//                    @Override
//                    public void onFail(String message) {
//                        Toast.makeText(getApplicationContext(), "Terjadi kesalahan", Toast.LENGTH_SHORT).show();
//                    }
//                })
//        );
//    }
//    private SSLSocketFactory getSocketFactory() {
//
//        CertificateFactory cf = null;
//        try {
//            cf = CertificateFactory.getInstance("X.509");
//            InputStream caInput = getResources().openRawResource(R.drawable.logo_fb);
//            Certificate ca;
//            try {
//                ca = cf.generateCertificate(caInput);
//                Log.e("CERT", "ca=" + ((X509Certificate) ca).getSubjectDN());
//            } finally {
//                caInput.close();
//            }
//
//
//            String keyStoreType = KeyStore.getDefaultType();
//            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
//            keyStore.load(null, null);
//            keyStore.setCertificateEntry("ca", ca);
//
//
//            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
//            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
//            tmf.init(keyStore);
//
//
//            HostnameVerifier hostnameVerifier = new HostnameVerifier() {
//                @Override
//                public boolean verify(String hostname, SSLSession session) {
//
//                    Log.e("CipherUsed", session.getCipherSuite());
//                    return hostname.compareTo("192.168.1.10")==0; //The Hostname of your server
//
//                }
//            };
//
//
//            HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
//            SSLContext context = null;
//            context = SSLContext.getInstance("TLS");
//
//            context.init(null, tmf.getTrustManagers(), null);
//            HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
//
//            SSLSocketFactory sf = context.getSocketFactory();
//
//
//            return sf;
//
//        } catch (CertificateException e) {
//            e.printStackTrace();
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        } catch (KeyStoreException e) {
//            e.printStackTrace();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (KeyManagementException e) {
//            e.printStackTrace();
//        }
//
//        return  null;
//    }

    @Override
    protected void onPause() {
        if (mNsdManager != null) {
            try{
                mNsdManager.unregisterService(mRegistrationListener);
                mNsdManager.stopServiceDiscovery(mDiscoveryListener);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        try {
            if (Build.VERSION.SDK_INT >= 21) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        super.onPause();
    }

    // ===================================== Main Activity Kontrol ================================
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
            if (Build.VERSION.SDK_INT >= 21) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (Build.VERSION.SDK_INT >= 21) {
            mNsdManager = (NsdManager) getSystemService(Context.NSD_SERVICE);
            mNsdManager.discoverServices(SERVICE_TYPE,
                    NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
            registerService(ServiceUtils.DEFAULT_PORT);
            // NSD Stuff
            initializeReceiver();
        }

        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                device_token = instanceIdResult.getToken();
                sessionManager.saveFcmId(device_token);
                try {
                    fcmId();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        if(!setBack){
            initIdKategoriFirst();
        }


        tvFcmId.setText(sessionManager.getFcmid());

//        initSlider();
        // load menu kategori streaming
        initKategoriStreaming();
        initItemTVStreaming();
        // load slider
//        initDataSlider();
        if (Build.VERSION.SDK_INT >= 21) {
            if (mNsdManager != null) {
                registerService(ServiceUtils.DEFAULT_PORT);

            }

            initializeReceiver();
        }
    }

    @Override
    protected void onDestroy() {

        if (mNsdManager != null) {
            try{
                mNsdManager.unregisterService(mRegistrationListener);
                mNsdManager.stopServiceDiscovery(mDiscoveryListener);
            }catch (Exception e){

                e.printStackTrace();
            }
        }

        try {
            if (Build.VERSION.SDK_INT >= 21) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        super.onDestroy();
    }

    private boolean isNetworkAvailable() {
        boolean have_WIFI= false;
        boolean have_MobileData = false;
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo[] networkInfos = connectivityManager.getAllNetworkInfo();
        for(NetworkInfo info:networkInfos){
            if (info.getTypeName().equalsIgnoreCase("WIFI"))if (info.isConnected())have_WIFI=true;
            if (info.getTypeName().equalsIgnoreCase("MOBILE DATA"))if (info.isConnected())have_MobileData=true;
        }
        return have_WIFI||have_MobileData;
    }


    NsdManager.DiscoveryListener mDiscoveryListener = new NsdManager.DiscoveryListener() {

        // Called as soon as service discovery begins.
        @Override
        public void onDiscoveryStarted(String regType) {
            Log.d(TAG, "Service discovery started");
        }

        @Override
        public void onServiceFound(NsdServiceInfo service) {
            // A service was found! Do something with it.
            Log.d(TAG, "Service discovery success : " + service);
            Log.d(TAG, "Host = "+ service.getServiceName());
            Log.d(TAG, "port = " + String.valueOf(service.getPort()));

            if (!service.getServiceType().equals(SERVICE_TYPE)) {
                // Service type is the string containing the protocol and
                // transport layer for this service.
                Log.d(TAG, "Unknown Service Type: " + service.getServiceType());
            } else if (service.getServiceName().equals(SERVICE_NAME)) {
                // The name of the service tells the user what they'd be
                // connecting to. It could be "Bob's Chat App".
                Log.d(TAG, "Same machine: " + SERVICE_NAME);
            } else {
                Log.d(TAG, "Diff Machine : " + service.getServiceName());
                // connect to the service and obtain serviceInfo
            }
            try {
                mNsdManager.resolveService(service, mResolveListener);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceLost(NsdServiceInfo service) {
            // When the network service is no longer available.
            // Internal bookkeeping code goes here.
            Log.e(TAG, "service lost" + service);
        }

        @Override
        public void onDiscoveryStopped(String serviceType) {
            Log.i(TAG, "Discovery stopped: " + serviceType);
        }

        @Override
        public void onStartDiscoveryFailed(String serviceType, int errorCode) {
            Log.e(TAG, "Discovery failed: Error code:" + errorCode);
            mNsdManager.stopServiceDiscovery(this);
        }

        @Override
        public void onStopDiscoveryFailed(String serviceType, int errorCode) {
            Log.e(TAG, "Discovery failed: Error code:" + errorCode);
            mNsdManager.stopServiceDiscovery(this);
        }
    };

    NsdManager.ResolveListener mResolveListener = new NsdManager.ResolveListener() {

        @Override
        public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
            // Called when the resolve fails. Use the error code to debug.
            Log.e(TAG, "Resolve failed " + errorCode);
            Log.e(TAG, "Resolve service = " + serviceInfo);
        }

        @Override
        public void onServiceResolved(NsdServiceInfo serviceInfo) {
            Log.d(TAG, "Resolve Succeeded. " + serviceInfo);

            if (serviceInfo.getServiceName().equals(SERVICE_NAME)) {
                Log.d(TAG, serviceInfo.getHost().getHostAddress());
                SelectedServer.host = serviceInfo.getHost().getHostAddress();
                SelectedServer.name = serviceInfo.getServiceName();
                SelectedServer.port = String.valueOf(serviceInfo.getPort());
                SelectedServer.type = serviceInfo.getServiceType();

                return;
            }

            // Obtain port and IP
            hostPort = serviceInfo.getPort();
            hostAddress = serviceInfo.getHost();
        }
    };

    public void initializeRegistrationListener() {

//        NsdManager.ResolveListener mResolveListener = new NsdManager.ResolveListener() {
//            @Override
//            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
//                Log.e(TAG, "Resolve failed " + errorCode);
//            }
//
//            @Override
//            public void onServiceResolved(NsdServiceInfo serviceInfo) {
//                Log.v(TAG, "Resolve Succeeded. " + serviceInfo);
//            }
//        };
//        mRegistrationListener = new NsdManager.RegistrationListener() {
//
//            @Override
//            public void onServiceRegistered(NsdServiceInfo nsdServiceInfo) {
//                WifiManager wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
//                String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
//
//                Log.d(TAG,">>>"+ip);
//                Log.d(TAG,">>>"+nsdServiceInfo.getServiceName());
//                Log.d(TAG,">>>"+nsdServiceInfo.getPort());
//                Log.d(TAG,">>>"+nsdServiceInfo.getServiceType());
//            }
//
//            @Override
//            public void onRegistrationFailed(NsdServiceInfo arg0, int arg1) {
//                Log.d(TAG, "Service registration failed: " + arg1);
//            }
//
//            @Override
//            public void onServiceUnregistered(NsdServiceInfo arg0) {
//                Log.d(TAG, "Service unregistered: " + arg0.getServiceName());
//            }
//
//            @Override
//            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
//                Log.d(TAG, "Service unregistration failed: " + errorCode);
//            }
//
//        };
    }



}
