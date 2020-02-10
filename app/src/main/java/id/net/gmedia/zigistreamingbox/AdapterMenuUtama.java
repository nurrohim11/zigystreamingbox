package id.net.gmedia.zigistreamingbox;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.util.ArrayList;
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

    @NonNull
    @Override
    public AdapterMenuUtama.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_menu_utama, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        final MenuModel menuModel = menuModels.get(position);
        holder.imgLeft.setVisibility(View.INVISIBLE);
        holder.tvTitle.setTypeface(holder.tvTitle.getTypeface(), Typeface.NORMAL);


//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            holder.tvTitle.setAutoSizeTextTypeUniformWithConfiguration(
//                    1, 17, 1, TypedValue.COMPLEX_UNIT_DIP);
//        }

        holder.tvTitle.setText(menuModel.getMenu());
        if(selectedPosition==position) {
            holder.imgLeft.setVisibility(View.VISIBLE);
//            holder.tvTitle.setTypeface(holder.tvTitle.getTypeface(), Typeface.BOLD_ITALIC);
//            Toast.makeText(context, "selected", Toast.LENGTH_SHORT).show();
        }else {
//            holder.tvTitle.setTypeface(holder.tvTitle.getTypeface(), Typeface.NORMAL);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedPosition=position;
                notifyDataSetChanged();
                mAdapterCallback.loadContent(menuModel.getId());
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
