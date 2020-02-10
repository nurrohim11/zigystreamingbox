package id.net.gmedia.zigistreamingbox.streaming;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import id.net.gmedia.zigistreamingbox.R;

public class KategoriAdapter extends RecyclerView.Adapter<KategoriAdapter.ViewHolder> {
    private List<KategoriModel> kategoriModels;
    private Context context;
    public static int selectedPosition=0;
    private KategoriAdapterCallback mAdapterCallback;

    public KategoriAdapter(Context context, List<KategoriModel> kategoriModels, KategoriAdapterCallback adapterCallback){
        this.context= context;
        this.mAdapterCallback = adapterCallback;
        this.kategoriModels = kategoriModels;
    }

    public KategoriAdapter(Context context, List<KategoriModel> kategoriModels){
        this.context= context;
        this.kategoriModels = kategoriModels;
    }

    @NonNull
    @Override
    public KategoriAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_kategori,parent,false);
        return  new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final KategoriAdapter.ViewHolder holder, final int position) {
        final String[] kat = {kategoriModels.get(0).getId()};
        holder.tv_kategori.setText(kategoriModels.get(position).getNama());
        if(selectedPosition==position) {
            holder.tv_kategori.setTextColor(Color.WHITE);
            holder.tv_kategori.setTextColor(Color.parseColor("#FFD700"));
        }
        else {
            holder.tv_kategori.setTextColor(Color.parseColor("#FFFFFF"));
            holder.itemView.setBackground(null);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedPosition=position;
                notifyDataSetChanged();
                kat[0] = kategoriModels.get(position).getId();
                mAdapterCallback.onRowKategoriCallback(kategoriModels.get(position).getId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return kategoriModels.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_kategori;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_kategori = itemView.findViewById(R.id.tv_kategori);
        }
    }
    public interface KategoriAdapterCallback {
        void onRowKategoriCallback(String id_kategori);
    }


}
