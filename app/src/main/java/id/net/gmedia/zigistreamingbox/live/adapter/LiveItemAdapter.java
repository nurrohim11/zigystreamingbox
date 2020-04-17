package id.net.gmedia.zigistreamingbox.live.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import co.id.gmedia.coremodul.ApiVolley;
import co.id.gmedia.coremodul.AppRequestCallback;
import id.net.gmedia.zigistreamingbox.MainActivity;
import id.net.gmedia.zigistreamingbox.R;
import id.net.gmedia.zigistreamingbox.live.model.LiveItemModel;
import id.net.gmedia.zigistreamingbox.live.LiveViewActivity;
import id.net.gmedia.zigistreamingbox.utils.ServerURL;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class LiveItemAdapter extends RecyclerView.Adapter<LiveItemAdapter.ViewHolder>  {

    private Context mContext;
    private List<LiveItemModel> customItems;
    public static int selectedPosition =-1;

    public LiveItemAdapter(Context mContext, List<LiveItemModel> musicModels) {
        this.mContext = mContext;
        this.customItems = musicModels;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_live, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final LiveItemModel m= customItems.get(position);
        Glide.with(mContext)
                .load(m.getIcon())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .override(500, 500)
                .centerCrop()
                .transform(new RoundedCornersTransformation(30,0))
                .into(holder.imgMusic);
        Glide.with(mContext)
                .load(m.getIcon())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .override(500, 500)
                .centerCrop()
                .transform(new RoundedCornersTransformation(30,0))
                .into(holder.imgBigMusic);
        holder.tvTitle.setText(m.getNama());
        Typeface typeface = ResourcesCompat.getFont(mContext, R.font.montserrat_medium);
        holder.tvTitle.setTypeface(typeface);
        if(selectedPosition==position) {
            LiveItemAdapter.sendData(MainActivity.fcm_client,m.getLink(),mContext);
            holder.llKonten.setBackgroundResource(R.drawable.item_selected);
            holder.tvTitle.setTextColor(Color.parseColor("#FFD700"));
            holder.imgBigMusic.setVisibility(View.VISIBLE);
            holder.imgMusic.setVisibility(View.INVISIBLE);
        }else{
            holder.llKonten.setBackgroundResource(0);
            holder.tvTitle.setTextColor(Color.parseColor("#FFFFFF"));
            holder.imgBigMusic.setVisibility(View.INVISIBLE);
            holder.imgMusic.setVisibility(View.VISIBLE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                selectedPosition = position;
                notifyDataSetChanged();

                Intent intent = new Intent(mContext, LiveViewActivity.class);
                intent.putExtra("nama", m.getNama());
                intent.putExtra("link", m.getLink());
                mContext.startActivity(intent);
//                ((Activity) mContext).startActivityForResult(intent,2);
                ((Activity)mContext).overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });
    }

    @Override
    public int getItemCount() {
        return customItems.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imgMusic, imgBigMusic;
        TextView tvTitle;
        LinearLayout llKonten;

        public ViewHolder(View itemView) {
            super(itemView);
            imgMusic = itemView.findViewById(R.id.img_usic);
            tvTitle = itemView.findViewById(R.id.tv_title);
            llKonten = itemView.findViewById(R.id.ll_konten);
            imgBigMusic = itemView.findViewById(R.id.img_bg_usic);
        }
    }


    public static void sendData(String fcm_client, String link_tv, Context context){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("to",fcm_client);
            JSONObject obj1 = new JSONObject();
            obj1.put("jenis",link_tv);
            obj1.put("title","fiber");
            jsonObject.put("data",obj1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(">>>>>>", String.valueOf(jsonObject));
        new ApiVolley(context, jsonObject, "POST", ServerURL.base_url_fcm,"1",
                new AppRequestCallback(new AppRequestCallback.ResponseListener() {
                    @Override
                    public void onSuccess(String response, String message) {
                    }
                    @Override
                    public void onEmpty(String message) {
                    }

                    @Override
                    public void onFail(String message) {
                    }
                })
        );
    }

}