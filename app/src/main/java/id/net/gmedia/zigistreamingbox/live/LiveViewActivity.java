package id.net.gmedia.zigistreamingbox.live;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Looper;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.TextView;
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
import java.util.Timer;
import java.util.TimerTask;

import co.id.gmedia.coremodul.ApiVolley;
import co.id.gmedia.coremodul.ApkInstaller;
import co.id.gmedia.coremodul.AppRequestCallback;
import co.id.gmedia.coremodul.ImageUtils;
import co.id.gmedia.coremodul.ItemValidation;
import co.id.gmedia.coremodul.SessionManager;
import id.net.gmedia.zigistreamingbox.CustomView.ScrollTextView;
import id.net.gmedia.zigistreamingbox.MainActivity;
import id.net.gmedia.zigistreamingbox.R;
import id.net.gmedia.zigistreamingbox.RemoteUtils.ServiceUtils;
import id.net.gmedia.zigistreamingbox.live.adapter.ListChannelIconAdapter;
import id.net.gmedia.zigistreamingbox.live.model.LiveItemModel;
import id.net.gmedia.zigistreamingbox.utils.CustomVideoView;
import id.net.gmedia.zigistreamingbox.utils.InternetCheck;
import id.net.gmedia.zigistreamingbox.utils.SavedChanelManager;
import id.net.gmedia.zigistreamingbox.utils.ServerURL;

public class LiveViewActivity extends AppCompatActivity {

    private static CustomVideoView vvPlayVideo;
    private ItemValidation iv = new ItemValidation();
    private boolean showNavigator = false;
    private int timerTime = 10; // in second
    private int timerAdsChecker = 2; // in minutes
    private int timerATChecker = 2; // in minutes
    private int animationDuration = 800; // in milisecond
    private TextView tvVolume;
    private SeekBar sbVolume;
    private AudioManager audioManager;
    private SearchView edtSearch;
    private TextView tvSearch;
    private static RelativeLayout rvListVideoContainer;
    private List<LiveItemModel> masterList;
    private static SavedChanelManager savedChanel;
    private static boolean itemOnSelect = false;
    private int delayTime = 5000; // Delay before hide the view
    private int channelTime = 1600; // Delay before hide the view
    private Timer timerAds;
    private CountDownTimer cAdsTimer;
    private CountDownTimer cNormalAdsTimer;
    private RecyclerView rvChannel;
    private RelativeLayout rlUpContainer;
    private int repeateMarquee = 5;
    private static ProgressBar pbLoading;
    private LinearLayout llYoutubeContainer;
    private static boolean isTypeChannel;
    private LinearLayout llChannelSelector;
    private TextView tvChannelSelector;
    private Timer timer;
    private boolean buttonOnYoutube = false;
    private boolean buttonOnUp = false;
    private static int lastPositionChannel = 0;
    private static int rowPerCard = 7;

    //For remote
    private NsdManager mNsdManager;
    private ServerSocket serverSocket;
    private SocketServerThread socketServerThread;
    private final String TAG = "Chanel";
    private static RelativeLayout rvScreenContainer;
    //private static RelativeLayout rvScreenContainer2;
    private WebView wvAds;
    private List<LiveItemModel> adsList;
    private boolean isLoad = true;
    private boolean isAppearTextLoad = true;
    private boolean isFirstLoad = true;
    private WifiManager wifi;
    private static double scaleVideo = 1;
    private static boolean isFullScreen = true;
    private boolean isTimerPaused = false;
    private boolean isTimerATPaused = false;
    private ImageView ivLogoTV;
    private ImageView ivUp, ivDown;
    private ScrollTextView tvUser;
    private static boolean tapped = false;
    private String appearText = "";

    SessionManager sessionManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_view);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        isTypeChannel = false;
        isTimerPaused = false;
        isTimerATPaused = false;
        scaleVideo = 1;
        isFullScreen = true;
        isFirstLoad = true;
        //isYoutube = false;
        itemOnSelect = false;
        scaleVideo = 1;
        isLoad = true;

        savedChanel = new SavedChanelManager(LiveViewActivity.this);
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){

            String nama = bundle.getString("nama");
            String link = bundle.getString("link");
            if(nama != null && link != null) savedChanel.saveLastChanel(nama,link);
        }

        initUI();

        // For Remote access
        //ServiceUtils.DEFAULT_PORT = ConnectionUtil.getPort(ServerActivity.this);
        mNsdManager = (NsdManager) getSystemService(Context.NSD_SERVICE);
        registerService(ServiceUtils.DEFAULT_PORT);
        initializeReceiver();
    }

    private void initUI() {

        tvVolume = (TextView) findViewById(R.id.tv_volume);
        sbVolume = (SeekBar) findViewById(R.id.sb_volume);
        /*ypYoutube = (YouTubePlayerView) findViewById(R.id.yp_youtube);
        ypYoutube.initialize(GoogleAPI.APIKey, this);*/
        vvPlayVideo = (CustomVideoView) findViewById(R.id.vv_stream);
        llYoutubeContainer = (LinearLayout) findViewById(R.id.ll_youtube_container);
        rvChannel = (RecyclerView) findViewById(R.id.rv_chanel);
        rlUpContainer = (RelativeLayout) findViewById(R.id.rl_up_container);
        rvListVideoContainer = (RelativeLayout) findViewById(R.id.rl_list_chanel);
        ivUp = (ImageView) findViewById(R.id.iv_up);
        ivLogoTV = (ImageView) findViewById(R.id.iv_tv_logo);
        ivDown = (ImageView) findViewById(R.id.iv_down);
        pbLoading = (ProgressBar) findViewById(R.id.pb_loading);
        llChannelSelector = (LinearLayout) findViewById(R.id.ll_channel_selector);
        tvChannelSelector = (TextView) findViewById(R.id.tv_channel_selector);
        rvScreenContainer = (RelativeLayout) findViewById(R.id.rv_screen_container);
        /*rvScreenContainer2 = (RelativeLayout) findViewById(R.id.rv_screen_container2);*/
        wvAds = (WebView) findViewById(R.id.wv_ads);
        wvAds.setWebViewClient(new WebViewClient());
        tvUser = (ScrollTextView) findViewById(R.id.tv_user);
        tvUser.setSelected(true);
        tvUser.setTextColor(getResources().getColor(R.color.color_text_marquee));
        tvUser.startScroll();

        tvUser.setVisibility(View.INVISIBLE);

        sessionManager = new SessionManager(LiveViewActivity.this);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        sbVolume.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));

        tapped = true;
        showNavigationItem(LiveViewActivity.this);
        if(savedChanel.isSaved()) playVideo(LiveViewActivity.this, savedChanel.getNama(), savedChanel.getLink());
        wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        //getDataWithConnection();

        vvPlayVideo.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                itemOnSelect = false;
                if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                    tapped = true;
                    showNavigationItem(LiveViewActivity.this);
                }

                return true;
            }
        });

        sbVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                tvVolume.setText(String.valueOf(i));
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, i, 0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        llYoutubeContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                redirrectToYoutube();
            }
        });

        rvChannel.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                itemOnSelect = true;
                tapped = true;
                showNavigationItem(LiveViewActivity.this);
                return false;
            }
        });

        rlUpContainer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                itemOnSelect = true;
                tapped = true;
                showNavigationItem(LiveViewActivity.this);
                return false;
            }
        });

    }

    private void redirrectToYoutube(){

        try {

            Intent i = getPackageManager().getLaunchIntentForPackage(ServerURL.pnYoutube);
            startActivity(i);
        } catch (Exception e) {

            ApkInstaller atualizaApp = new ApkInstaller();
            atualizaApp.setContext(LiveViewActivity.this);
            atualizaApp.execute(ServerURL.bwYoutubeForTV);
        }
    }

    private static void showNavigationItem(final Context context){

        ((Activity) context).runOnUiThread(new Runnable() {

            @Override
            public void run() {

                if(rvListVideoContainer.getVisibility() == View.GONE){
                    rvListVideoContainer.setVisibility(View.VISIBLE);
                    rvListVideoContainer.animate()
                            .translationY(0)
                            .alpha(1.0f)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                }
                            });
                }else if(rvListVideoContainer.getVisibility() == View.VISIBLE && !itemOnSelect){

                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            rvListVideoContainer.clearAnimation();
                            rvListVideoContainer.animate()
                                    .translationY(0)
                                    .alpha(0.0f)
                                    .setListener(new AnimatorListenerAdapter() {
                                        @Override
                                        public void onAnimationEnd(Animator animation) {
                                            super.onAnimationEnd(animation);
                                            rvListVideoContainer.setVisibility(View.GONE);
                                        }
                                    });
                        }
                    });
                }
            }
        });
    }


    public static void playVideo(final Context context, final String nama, final String url){
        itemOnSelect = false;
        tapped = true;
        showNavigationItem(context);

        vvPlayVideo.stopPlayback();
        vvPlayVideo.clearAnimation();
        vvPlayVideo.suspend();
        vvPlayVideo.setVideoURI(null);

        pbLoading.setVisibility(View.VISIBLE);

        if(false){

        }else{
            vvPlayVideo.setVisibility(View.VISIBLE);
            if (Looper.myLooper() == null)
            {
                Looper.prepare();
            }
            new Thread(new Runnable() {
                public void run() {

                    ((Activity)context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            try {

                                vvPlayVideo.stopPlayback();
                                vvPlayVideo.clearAnimation();
                                vvPlayVideo.suspend();
                                vvPlayVideo.setVideoURI(null);

//                                Uri uri = Uri.parse("http://channel1.fiberstream.id/hls/metrotvhd.m3u8");
                                Uri uri = Uri.parse(url);
                                savedChanel.saveLastChanel(nama, url);
                                vvPlayVideo.setVideoURI(uri);
                                //vvPlayVideo.setMediaController(mediaController);
                                vvPlayVideo.requestFocus();

                            } catch (Exception e) {
                                // NETWORK ERROR such as Timeout
                                e.printStackTrace();

                                pbLoading.setVisibility(View.GONE);
                                vvPlayVideo.stopPlayback();
                                vvPlayVideo.clearAnimation();
                                vvPlayVideo.suspend();
                                vvPlayVideo.setVideoURI(null);
                                Toast.makeText(context, "Channel sudah tidak tersedia", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }).start();

            vvPlayVideo.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                @Override
                public void onPrepared(MediaPlayer mp) {

                    pbLoading.setVisibility(View.GONE);
                    mp.start();

                    fullScreenVideo(context, scaleVideo);
                    mp.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {

                        @Override
                        public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {

                            mp.start();
                            fullScreenVideo(context, scaleVideo);
                        }
                    });
                }
            });

            vvPlayVideo.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {

                    pbLoading.setVisibility(View.GONE);
                    vvPlayVideo.stopPlayback();
                    vvPlayVideo.clearAnimation();
                    vvPlayVideo.suspend();
                    vvPlayVideo.setVideoURI(null);
                    Toast.makeText(context, "Channel sudah tidak tersedia", Toast.LENGTH_LONG).show();
                    return true;
                }
            });

        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        int maxLength = (masterList == null) ? 0 : masterList.size();
        switch (keyCode){

            case 19: // up
                itemOnSelect = true;
                tapped = true;
                showNavigationItem(LiveViewActivity.this);

                if(buttonOnYoutube){

                    if(masterList != null){

                        llYoutubeContainer.setBackground(getResources().getDrawable(R.drawable.background_radian_black));
                        rlUpContainer.setBackground(getResources().getDrawable(R.drawable.background_radian_black));
                        buttonOnYoutube = false;
                        buttonOnUp = false;

                        lastPositionChannel = masterList.size() - 1;
                        // Play Last Video
                        ListChannelIconAdapter.selectedPosition  = lastPositionChannel;
                        ListChannelIconAdapter adapter = (ListChannelIconAdapter) rvChannel.getAdapter();
                        adapter.notifyDataSetChanged();
                        rvChannel.smoothScrollToPosition(lastPositionChannel);
                        //ensureVisible(rvChannel, ListChanelAdapter.selectedPosition);
                        /*CustomItem item = masterList.get(ListChanelAdapter.selectedPosition);
                        playVideo(item.getItem2(),item.getItem3());*/
                    }
                }else if(buttonOnUp){

                    ListChannelIconAdapter.selectedPosition = -1;
                    ListChannelIconAdapter adapter = (ListChannelIconAdapter) rvChannel.getAdapter();
                    adapter.notifyDataSetChanged();

                    rlUpContainer.setBackground(getResources().getDrawable(R.drawable.background_radian_black));
                    buttonOnUp = false;
                    llYoutubeContainer.setBackground(getResources().getDrawable(R.drawable.background_radian_red));
                    buttonOnYoutube = true;
                }else{

                    if(ListChannelIconAdapter.selectedPosition - rowPerCard >= 0){

                        ListChannelIconAdapter.selectedPosition = ListChannelIconAdapter.selectedPosition - rowPerCard;
                        lastPositionChannel = ListChannelIconAdapter.selectedPosition;
                        ListChannelIconAdapter adapter = (ListChannelIconAdapter) rvChannel.getAdapter();
                        adapter.notifyDataSetChanged();
                        //lvChanel.setSelection(ListChanelAdapter.selectedPosition);
                        //ensureVisible(lvChanel, ListChanelAdapter.selectedPosition);
                        /*CustomItem item = masterList.get(ListChanelAdapter.selectedPosition);
                        playVideo(item.getItem2(),item.getItem3());*/
                        rvChannel.smoothScrollToPosition(lastPositionChannel);
                        llYoutubeContainer.setBackground(getResources().getDrawable(R.drawable.background_radian_black));
                        rlUpContainer.setBackground(getResources().getDrawable(R.drawable.background_radian_black));
                        buttonOnYoutube = false;
                        buttonOnUp = false;
                    }else{

                        ListChannelIconAdapter.selectedPosition = -1;
                        ListChannelIconAdapter adapter = (ListChannelIconAdapter) rvChannel.getAdapter();
                        adapter.notifyDataSetChanged();

                        rlUpContainer.setBackground(getResources().getDrawable(R.drawable.background_radian_red));
                        buttonOnUp = true;
                        llYoutubeContainer.setBackground(getResources().getDrawable(R.drawable.background_radian_black));
                        buttonOnYoutube = false;
                    }
                }

                break;
            case 20:

                itemOnSelect = true;
                tapped = true;
                showNavigationItem(LiveViewActivity.this);

                if(buttonOnUp){

                    ListChannelIconAdapter.selectedPosition  = 0;
                    ListChannelIconAdapter adapter = (ListChannelIconAdapter) rvChannel.getAdapter();
                    adapter.notifyDataSetChanged();
                    lastPositionChannel = ListChannelIconAdapter.selectedPosition;
                    rvChannel.smoothScrollToPosition(lastPositionChannel);
                    rlUpContainer.setBackground(getResources().getDrawable(R.drawable.background_radian_black));
                    llYoutubeContainer.setBackground(getResources().getDrawable(R.drawable.background_radian_black));
                    buttonOnYoutube = false;
                    buttonOnUp= false;
                }else if(buttonOnYoutube){

                    ListChannelIconAdapter.selectedPosition  = -1;
                    ListChannelIconAdapter adapter = (ListChannelIconAdapter) rvChannel.getAdapter();
                    adapter.notifyDataSetChanged();

                    llYoutubeContainer.setBackground(getResources().getDrawable(R.drawable.background_radian_black));
                    buttonOnYoutube = false;
                    rlUpContainer.setBackground(getResources().getDrawable(R.drawable.background_radian_red));
                    buttonOnUp = true;
                }else{
                    if(ListChannelIconAdapter.selectedPosition + rowPerCard < maxLength){
                        ListChannelIconAdapter.selectedPosition  = ListChannelIconAdapter.selectedPosition + rowPerCard;
                        ListChannelIconAdapter adapter = (ListChannelIconAdapter) rvChannel.getAdapter();
                        adapter.notifyDataSetChanged();
                        lastPositionChannel = ListChannelIconAdapter.selectedPosition;
                        rvChannel.smoothScrollToPosition(lastPositionChannel);
                        //lvChanel.setSelection(ListChanelAdapter.selectedPosition);
                        //ensureVisible(lvChanel, ListChanelAdapter.selectedPosition);
                    /*CustomItem item = masterList.get(ListChanelAdapter.selectedPosition);
                    playVideo(item.getItem2(),item.getItem3());*/
                        rlUpContainer.setBackground(getResources().getDrawable(R.drawable.background_radian_black));
                        llYoutubeContainer.setBackground(getResources().getDrawable(R.drawable.background_radian_black));
                        buttonOnYoutube = false;
                        buttonOnUp= false;
                    }else{

                        ListChannelIconAdapter.selectedPosition  = -1;
                        ListChannelIconAdapter adapter = (ListChannelIconAdapter) rvChannel.getAdapter();
                        adapter.notifyDataSetChanged();

                        llYoutubeContainer.setBackground(getResources().getDrawable(R.drawable.background_radian_red));
                        buttonOnYoutube = true;
                        rlUpContainer.setBackground(getResources().getDrawable(R.drawable.background_radian_black));
                        buttonOnUp = false;
                    }
                }
                break;

            case 22:

                itemOnSelect = true;
                tapped = true;
                showNavigationItem(LiveViewActivity.this);

                if(!buttonOnUp && !buttonOnYoutube){
                    if(ListChannelIconAdapter.selectedPosition + 1 < maxLength){
                        ListChannelIconAdapter.selectedPosition = ListChannelIconAdapter.selectedPosition + 1;
                        ListChannelIconAdapter adapter = (ListChannelIconAdapter) rvChannel.getAdapter();
                        adapter.notifyDataSetChanged();
                        lastPositionChannel = ListChannelIconAdapter.selectedPosition;
                        rvChannel.smoothScrollToPosition(lastPositionChannel);
                        rlUpContainer.setBackground(getResources().getDrawable(R.drawable.background_radian_black));
                        llYoutubeContainer.setBackground(getResources().getDrawable(R.drawable.background_radian_black));
                        buttonOnYoutube = false;
                        buttonOnUp= false;
                    }
                }
                break;
            case 21:

                itemOnSelect = true;
                tapped = true;
                showNavigationItem(LiveViewActivity.this);

                if(!buttonOnUp && !buttonOnYoutube){
                    if(ListChannelIconAdapter.selectedPosition - 1 >= 0){
                        ListChannelIconAdapter.selectedPosition = ListChannelIconAdapter.selectedPosition - 1;
                        ListChannelIconAdapter adapter = (ListChannelIconAdapter) rvChannel.getAdapter();
                        adapter.notifyDataSetChanged();
                        lastPositionChannel = ListChannelIconAdapter.selectedPosition;
                        rvChannel.smoothScrollToPosition(lastPositionChannel);
                        rlUpContainer.setBackground(getResources().getDrawable(R.drawable.background_radian_black));
                        llYoutubeContainer.setBackground(getResources().getDrawable(R.drawable.background_radian_black));
                        buttonOnYoutube = false;
                        buttonOnUp= false;
                    }
                }
                break;
            case 23: // OK

                if(buttonOnYoutube){

                    redirrectToYoutube();
                }else if(buttonOnUp){

                    itemOnSelect = false;
                    tapped = true;
                    showNavigationItem(LiveViewActivity.this);
                }else{

                    if(rvListVideoContainer.getVisibility() == View.GONE){
                    /*CustomItem item = masterList.get(ListChanelAdapter.selectedPosition);
                    playVideo(item.getItem2(),item.getItem3());*/
                        itemOnSelect = true;
                        showNavigationItem(LiveViewActivity.this);
                    }else{

                        if(masterList != null){
                            LiveItemModel item = masterList.get(ListChannelIconAdapter.selectedPosition);
                            playVideo(LiveViewActivity.this, item.getNama(),item.getLink());
                            itemOnSelect = false;
                            showNavigationItem(LiveViewActivity.this);
                        }
                    }
                }

                break;
            case 4:
                onBackPressed();
                break;
//            case 7:
//                selectChannel("0");
//                break;
//            case 8:
//                selectChannel("1");
//                break;
//            case 9:
//                selectChannel("2");
//                break;
//            case 10:
//                selectChannel("3");
//                break;
//            case 11:
//                selectChannel("4");
//                break;
//            case 12:
//                selectChannel("5");
//                break;
//            case 13:
//                selectChannel("6");
//                break;
//            case 14:
//                selectChannel("7");
//                break;
//            case 15:
//                selectChannel("8");
//                break;
//            case 16:
//                selectChannel("9");
//                break;
            case 32:
                tapped = true;
                showNavigationItem(LiveViewActivity.this);
                break;
            case  61:
                getDevice();
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void selectChannel(final String number){

        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                if(tvChannelSelector.getText().length() < 4){
                    isTypeChannel = true;
                    itemOnSelect = true;
                    showNavigationItem(LiveViewActivity.this);
                    tvChannelSelector.setText(tvChannelSelector.getText().toString()+number);
                    if(llChannelSelector.getVisibility() == View.GONE){
                        llChannelSelector.setVisibility(View.VISIBLE);
                        llChannelSelector.animate()
                                .translationY(0)
                                .alpha(1.0f)
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        super.onAnimationEnd(animation);
                                    }
                                });
                    }
                    if(number.equals("") && tvChannelSelector.getText().length() == 1) isTypeChannel = false;
                }else{
                    isTypeChannel = false;
                }
            }
        });

        timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {

                if(isTypeChannel){

                    isTypeChannel = false;
                    if(tvChannelSelector.getText().length() == 1){
                        selectChannel("");
                    }
                }else {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            if(iv.parseNullInteger(tvChannelSelector.getText().toString()) > 0 && iv.parseNullInteger(tvChannelSelector.getText().toString())< masterList.size()){
                                ListChannelIconAdapter.selectedPosition = iv.parseNullInteger(tvChannelSelector.getText().toString()) - 1;
                                ListChannelIconAdapter adapter = (ListChannelIconAdapter) rvChannel.getAdapter();
                                adapter.notifyDataSetChanged();
                                rvChannel.smoothScrollToPosition(ListChannelIconAdapter.selectedPosition);
                                LiveItemModel item = masterList.get(ListChannelIconAdapter.selectedPosition);
                                playVideo(LiveViewActivity.this, item.getNama(),item.getLink());

                            }else{

                                Toast.makeText(LiveViewActivity.this, "Channel tidak tersedia", Toast.LENGTH_SHORT).show();
                            }

                            llChannelSelector.clearAnimation();
                            llChannelSelector.animate()
                                    .translationY(0)
                                    .alpha(0.0f)
                                    .setListener(new AnimatorListenerAdapter() {
                                        @Override
                                        public void onAnimationEnd(Animator animation) {
                                            super.onAnimationEnd(animation);
                                            llChannelSelector.setVisibility(View.GONE);
                                            tvChannelSelector.setText("");
                                        }
                                    });
                            onBackPressed();
                        }
                    });

                }
            }

        }, channelTime);
    }

    private void setVideoCOntainerScale1(final boolean reverse, final double scale)
    {

        scaleVideo = scale;
        float floatScale = (float) scale;
        isFullScreen = reverse;
        fullScreenVideo(LiveViewActivity.this, scale);
        if(reverse){
            scaleView(floatScale, 1);
        }else{
            scaleView( 1, floatScale);
        }
    }

    public void scaleView(float startScale, final float endScale) {

        final DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        final RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) rvScreenContainer.getLayoutParams();
        params.leftMargin = 0;

        if(isFullScreen){

            params.width = (int) (metrics.widthPixels * 1);
            params.height = (int) (metrics.heightPixels * 1);
            rvScreenContainer.setScaleX(endScale);
            rvScreenContainer.setScaleY(endScale);
            rvScreenContainer.setPivotX(0);
            rvScreenContainer.setPivotY(0);
        }

        Animation anim = new ScaleAnimation(
                startScale, endScale, // Start and end values for the X axis scaling
                startScale, endScale, // Start and end values for the Y axis scaling
                Animation.RELATIVE_TO_SELF, 0f, // Pivot point of X scaling
                Animation.RELATIVE_TO_SELF, 0f); // Pivot point of Y scaling
        anim.setFillAfter(true); // Needed to keep the result of the animation
        anim.setDuration(1000);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(!isFullScreen){
                            rvScreenContainer.clearAnimation();
                            rvScreenContainer.setScaleX(1);
                            rvScreenContainer.setScaleY(1);
                            rvScreenContainer.setPivotX(0);
                            rvScreenContainer.setPivotY(0);

                            params.width = (int) (metrics.widthPixels * endScale);
                            params.height = (int) (metrics.heightPixels * endScale);
                        }
                        fullScreenVideo(LiveViewActivity.this, endScale);
                    }
                });
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        rvScreenContainer.startAnimation(anim);
    }

    private void backNormalScale1(int Seconds, final double scale){
        cNormalAdsTimer =  new CountDownTimer(Seconds* 1000+1000, 1000) {

            public void onTick(long millisUntilFinished) {
                int seconds = (int) (millisUntilFinished / 1000);
                int minutes = seconds / 60;
                seconds = seconds % 60;
            }

            public void onFinish() {
                wvAds.setWebViewClient(new WebViewClient());
                wvAds.loadUrl("about:blank");
                setVideoCOntainerScale1(true,scale);
            }
        }.start();
    }

    private static void fullScreenVideo(Context context, double scale)
    {
        DisplayMetrics metrics = new DisplayMetrics();
        ((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(metrics);
        RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams) vvPlayVideo.getLayoutParams();

        if(isFullScreen){
            scale = 1;
        }
        double doubleWidth = metrics.widthPixels * scale;
        double doubleHeight = metrics.heightPixels * scale;
        RelativeLayout.LayoutParams newparams = new RelativeLayout.LayoutParams((int) doubleWidth,(int) doubleHeight);
        newparams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        newparams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        newparams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        newparams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);

        vvPlayVideo.setLayoutParams(newparams);
    }

    @Override
    public void onBackPressed() {
        if(rvListVideoContainer.getVisibility() == View.VISIBLE){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    rvListVideoContainer.clearAnimation();
                    rvListVideoContainer.animate()
                            .translationY(0)
                            .alpha(0.0f)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    rvListVideoContainer.setVisibility(View.GONE);
                                }
                            });
                }
            });
        }else{

            if(timerAds!= null) {
                timerAds.cancel();
                timerAds = null;
            }

            if(cAdsTimer != null){
                cAdsTimer.cancel();
                cAdsTimer = null;
            }

            if(cNormalAdsTimer != null){
                cNormalAdsTimer.cancel();
                cNormalAdsTimer = null;
            }
            //super.onBackPressed();
            Intent intent = new Intent(LiveViewActivity.this, MainActivity.class);
            Bundle b = new Bundle();
            b.putBoolean("back",true);
            intent.putExtras(b);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
    }


    private void getDataWithConnection() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
            new InternetCheck(LiveViewActivity.this).isInternetConnectionAvailable(new InternetCheck.InternetCheckListener() {

                @Override
                public void onComplete(boolean connected) {
                    if(connected){

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                getLogo();
                                getLinkRTSP();
                                /*getAds();
                                getAppearText();*/
                                getTimerTV();
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

    private void getTimerTV() {

        JSONObject jbody = new JSONObject();
        adsList = new ArrayList<>();

        try {
            jbody.put("fcm_id", sessionManager.getFcmid());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        new ApiVolley(LiveViewActivity.this, jbody, "POST", ServerURL.get_timertv, new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {
                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");

                    if(iv.parseNullInteger(status) == 200) {

                        String timerAds = response.getJSONObject("response").getString("timer_adv");
                        String timerAT = response.getJSONObject("response").getString("timer_running_text");

                        if(timerAds != null && timerAds.length() > 0){
                            timerAdsChecker = iv.parseNullInteger(timerAds);
                        }

                        if(timerAT != null && timerAT.length() > 0 ){
                            timerATChecker = iv.parseNullInteger(timerAT);
                        }

                        getAds();
                        getAppearText();
                    }else{

                        getAds();
                        getAppearText();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();

                    getAds();
                    getAppearText();
                }
            }

            @Override
            public void onError(String result) {

                getAds();
                getAppearText();
            }
        });
    }

    private void getAds() {

        JSONObject jbody = new JSONObject();
        adsList = new ArrayList<>();

        try {
            jbody.put("fcm_id", sessionManager.getFcmid());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley apiVolley = new ApiVolley(LiveViewActivity.this, jbody, "POST", ServerURL.get_advertisement,  new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {
                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");

                    if(iv.parseNullInteger(status) == 200) {

                        JSONArray jsonArray = response.getJSONArray("response");
                        for(int i = 0; i < jsonArray.length();i++){
                            JSONObject jo = jsonArray.getJSONObject(i);
                            adsList.add(new LiveItemModel(jo.getString("id"), jo.getString("nama"), jo.getString("link"), jo.getString("showing_duration"), jo.getString("scale_screen")));
                        }

                        setTimerAds();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String result) {

            }
        });
    }

    private void getAppearText() {

        JSONObject jbody = new JSONObject();
        appearText = "";

        try {
            jbody.put("fcm_id", sessionManager.getFcmid());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley apiVolley = new ApiVolley(LiveViewActivity.this, jbody, "POST", ServerURL.get_appear_text, new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {
                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");

                    if(iv.parseNullInteger(status) == 200) {

                        JSONArray jsonArray = response.getJSONArray("response");
                        for(int i = 0; i < jsonArray.length();i++){
                            JSONObject jo = jsonArray.getJSONObject(i);
                            appearText = jo.getString("text");
                            String duration = jo.getString("showing_duration");
                            setTimerAppearText(iv.parseNullInteger(duration));

                        }
                        tvUser.setVisibility(View.VISIBLE);
                    }else{
                        tvUser.setVisibility(View.GONE);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String result) {

            }
        });
    }

    private void getLogo() {

        JSONObject jbody = new JSONObject();
        adsList = new ArrayList<>();

        new ApiVolley(LiveViewActivity.this, jbody, "GET", ServerURL.get_logo, new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {
                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");

                    if(iv.parseNullInteger(status) == 200) {
                        String linkLogo = response.getJSONObject("response").getString("link");
                        if(linkLogo.length() > 0){
                            ImageUtils iu = new ImageUtils();
                            iu.LoadGIFImage(LiveViewActivity.this, linkLogo, ivLogoTV, R.drawable.ic_logo_fiberstar2);
                        }

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String result) {

            }
        });
    }

    private void setTimerAds(){

        if(isLoad){
            isLoad  = false;
            try {
            }catch (Exception e){
                e.printStackTrace();
            }
            timerAds = new Timer();
            timerAds.schedule(new TimerTask() {
                @Override
                public void run() {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            if(adsList != null && adsList.size() > 0 && !isTimerPaused){
                                isTimerPaused = true;
                                LiveItemModel item =  adsList.get(0);
                                if(item.getNama() != null && item.getLink().length() > 0){
                                    int duration = iv.parseNullInteger(item.getLink());
                                    double scale = iv.parseNullFloat(item.getIcon());
                                    setVideoCOntainerScale1(false, scale);
                                    playAdsTimer(duration,item.getNama(), scale);
                                }
                            }else if(!isTimerPaused){
                                getAds();
                            }
                        }
                    });
                }
            },(timerAdsChecker * 60 * 1000) , (timerAdsChecker * 60 * 1000));

        }
    }

    private void playAdsTimer(int Seconds, String url, final double scale){

        wvAds.setWebViewClient(new WebViewClient());
        wvAds.loadUrl(url);
        //Log.d(TAG, "Timer Ads: "+ url);
        /*if(cAdsTimer != null){
            cAdsTimer.cancel();
            cAdsTimer = null;
        }*/

        cAdsTimer =  new CountDownTimer(Seconds* 1000+1000, 1000) {

            public void onTick(long millisUntilFinished) {
                int seconds = (int) (millisUntilFinished / 1000);
                int minutes = seconds / 60;
                seconds = seconds % 60;
            }

            public void onFinish() {

                if(adsList != null){
                    adsList.remove(0);

                    if(adsList.size() > 0){
                        LiveItemModel nowAds = adsList.get(0);
                        int duration = iv.parseNullInteger(nowAds.getShowing_duration());
                        String adsURL = nowAds.getLink();
                        if(adsURL != null && adsURL.length() > 0){
                            playAdsTimer(duration, adsURL, scale);
                        }
                    }else{
                        backNormalScale1(1, scale);
                        isTimerPaused = false;
                    }
                }else{
                    isTimerPaused = false;
                }
            }
        }.start();
    }

    //region appear text
    private void setTimerAppearText(final int duration){

        if(isAppearTextLoad){
            isAppearTextLoad = false;
            Timer timerAT = new Timer();
            timerAT.schedule(new TimerTask() {
                @Override
                public void run() {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            if(appearText.length() > 0 && !isTimerATPaused){
                                isTimerATPaused = true;
                                playATTimer(duration, appearText);
                            }else if(!isTimerATPaused){
                                getAppearText();
                            }
                        }
                    });
                }
            },1000 , (timerATChecker * 60 * 1000));
        }
    }

    private void playATTimer(int Seconds, final String message){

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                String msg = message;
                for(int i = 0 ; i < repeateMarquee; i++){
                    msg = msg + " • " + message;
                }

                tvUser.setText(msg);
                tvUser.setSelected(true);
                tvUser.startScroll();
                tvUser.setVisibility(View.VISIBLE);
                tvUser.animate()
                        .translationY(0)
                        .alpha(1.0f)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                            }
                        });

            }
        });

        new CountDownTimer(Seconds* 1000+1000, 1000) {

            public void onTick(long millisUntilFinished) {
                int seconds = (int) (millisUntilFinished / 1000);
                int minutes = seconds / 60;
                seconds = seconds % 60;
            }

            public void onFinish() {

                isTimerATPaused = false;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        appearText = "";
                        tvUser.clearAnimation();
                        tvUser.animate()
                                .translationY(0)
                                .alpha(0.0f)
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        super.onAnimationEnd(animation);
                                        tvUser.setText("");
                                        tvUser.setVisibility(View.GONE);
                                    }
                                });
                    }
                });
            }
        }.start();
    }
    //endregion

    private void getLinkRTSP() {

        JSONObject jbody = new JSONObject();

        try {
            jbody.put("type","all");
            jbody.put("kategori","");
            jbody.put("fcm_id", sessionManager.getFcmid());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley apiVolley = new ApiVolley(LiveViewActivity.this, jbody, "POST", ServerURL.get_channel, new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                masterList = new ArrayList<>();
                int x = 0;
                int saveC = 0;
                try {
                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");

                    if(iv.parseNullInteger(status) == 200) {

                        JSONArray jsonArray = response.getJSONArray("response");
                        for(int i = 0; i < jsonArray.length();i++){
                            JSONObject jo = jsonArray.getJSONObject(i);
                            masterList.add(new LiveItemModel(jo.getString("id"), jo.getString("nama"), jo.getString("link"), jo.getString("icon")));

                            if(i == 0 && !savedChanel.isSaved()){
                                playVideo(LiveViewActivity.this, jo.getString("nama"), jo.getString("link"));
                                saveC = x;
                            }else if(savedChanel.isSaved() && jo.getString("link").trim().equals(savedChanel.getLink().trim())&& jo.getString("nama").trim().equals(savedChanel.getNama().trim())){
                                saveC = x;
                            }
                            x++;
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                setListChanel(masterList, saveC);
            }

            @Override
            public void onError(String result) {

                setListChanel(null, 0);
            }
        });
    }

    private void setListChanel(List<LiveItemModel> listItem, int saved){

        rvChannel.setAdapter(null);

        if(listItem != null && listItem.size() > 0){

            final ListChannelIconAdapter menuAdapter = new ListChannelIconAdapter(LiveViewActivity.this, listItem);

            final RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(LiveViewActivity.this, rowPerCard);
            rvChannel.setLayoutManager(mLayoutManager);
            rvChannel.setItemAnimator(new DefaultItemAnimator());
            rvChannel.setAdapter(menuAdapter);
            rvChannel.getItemAnimator().setChangeDuration(0);

            menuAdapter.selectedPosition = saved;
            menuAdapter.notifyDataSetChanged();
            rvChannel.smoothScrollToPosition(saved);
        }
    }


    private void getDevice(){
        JSONObject jbody = new JSONObject();

        new ApiVolley(LiveViewActivity.this, jbody, "POST", ServerURL.url_profile_device,
                new AppRequestCallback(new AppRequestCallback.ResponseListener() {
                    @Override
                    public void onSuccess(String response, String message) {
                        try{
                            JSONObject res = new JSONObject(response);
                            Toast.makeText(LiveViewActivity.this, "Device id "+res.getString("id_device"), Toast.LENGTH_SHORT).show();
                        }
                        catch (JSONException e){
                            Toast.makeText(LiveViewActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onEmpty(String message) {
                        Toast.makeText(LiveViewActivity.this, "Maaf device anda belum terdaftar", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFail(String message) {
                        Toast.makeText(LiveViewActivity.this, "Terjadi kesalahan data", Toast.LENGTH_SHORT).show();
                    }
                })
        );
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
        }

        @Override
        public void onServiceUnregistered(NsdServiceInfo serviceInfo) {
            Log.d(TAG,
                    "Service Unregistered : " + serviceInfo.getServiceName());
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

        getDataWithConnection();
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

    public static void playVideo(final Context context, final String id){

    }

    // Custom Class for youtube naviagation

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
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(LiveViewActivity.this, "Connected device " + ServiceUtils.lockedClient, Toast.LENGTH_LONG).show();
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

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, keyCode));
                dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, keyCode));
            }
        });
    }
}
