package id.net.gmedia.zigistreamingbox;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AdapterMenuUtama extends RecyclerView.Adapter<AdapterMenuUtama.ViewHolder> {

    private List<MenuModel> menuModels;
    private Context context;
    public  static  int selectedPosition=0;
    private MenuAdapterCallback mAdapterCallback;

    public AdapterMenuUtama(Context context, List<MenuModel> moviesList, MenuAdapterCallback adapterCallback) {
        this.menuModels = moviesList;
        this.context =context;
        this.mAdapterCallback = adapterCallback;
    }
    public AdapterMenuUtama(Context context, List<MenuModel> moviesList) {
        this.menuModels = moviesList;
        this.context =context;
    }

    @NonNull
    @Override
    public AdapterMenuUtama.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_menu_utama, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        final MenuModel menuModel = menuModels.get(position);
        holder.imgLeft.setVisibility(View.INVISIBLE);

        Typeface typeface = ResourcesCompat.getFont(context, R.font.montserrat_medium);
        holder.tvTitle.setTypeface(typeface);

        holder.tvTitle.setTypeface(holder.tvTitle.getTypeface(), Typeface.NORMAL);

        holder.tvTitle.setText(menuModel.getMenu());
        if(selectedPosition==position) {
            holder.imgLeft.setVisibility(View.VISIBLE);
            holder.tvTitle.setTextColor(Color.parseColor("#FFFFFF"));
        }else {
            holder.tvTitle.setTextColor(Color.parseColor("#AEAEAE"));
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedPosition=position;
                notifyDataSetChanged();
//                mAdapterCallback.loadContent(menuModel.getId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return menuModels.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvTitle;
        public ImageView imgLeft;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_menu);
            imgLeft = itemView.findViewById(R.id.ic_left);
        }
    }

    public interface MenuAdapterCallback {
        void loadContent(int id);
    }
}
