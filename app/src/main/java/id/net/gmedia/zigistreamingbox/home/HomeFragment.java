package id.net.gmedia.zigistreamingbox.home;


import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import com.bumptech.glide.Glide;

import id.net.gmedia.zigistreamingbox.R;

import static androidx.constraintlayout.widget.Constraints.TAG;
import static id.net.gmedia.zigistreamingbox.utils.Screen.getScreenHeight;
import static id.net.gmedia.zigistreamingbox.utils.Screen.getScreenWidth;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    VideoView vvHome;
    ImageView imgHome,imgKonten;
    LinearLayout llVideo;
    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =inflater.inflate(R.layout.fragment_home, container, false);
//        vvHome = view.findViewById(R.id.vv_home);
        imgHome = view.findViewById(R.id.img_home);
//        imgKonten = view.findViewById(R.id.img_konten);
//        llVideo = view.findViewById(R.id.ll_vide);

//        vvHome.setVideoPath("http://gmedia.bz/tv/assets/video/data_center.mp4");
//        MediaController mediaController = new
//                MediaController(getContext());
//        mediaController.setAnchorView(vvHome);
//        vvHome.setMediaController(mediaController);
//        vvHome.setOnPreparedListener(new
//            MediaPlayer.OnPreparedListener()  {
//                @Override
//                public void onPrepared(MediaPlayer mp) {
//                    mp.setLooping(true);
//                    Log.i(TAG, "Duration = " +
//                            vvHome.getDuration());
//                }
//            });
//        vvHome.start();

        Glide
            .with(getActivity())
            .load(R.drawable.sd)
            .centerCrop()
            .placeholder(R.drawable.placeholder)
            .into(imgHome);

        return  view;
    }

}
