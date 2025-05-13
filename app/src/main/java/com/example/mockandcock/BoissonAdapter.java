package com.example.mockandcock;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class BoissonAdapter extends RecyclerView.Adapter<BoissonAdapter.BoissonViewHolder> {

    private final Context context;
    private final List<Boisson> boissonList;
    private final onClickBoissonListener clickListener;

    // Interface pour gérer les clics
    public interface onClickBoissonListener {
        void onBoissonClick(String boissonId);
    }

    // Constructeur avec le listener (suppression de l'autre constructeur)
    public BoissonAdapter(Context context, List<Boisson> boissonList, onClickBoissonListener clickListener) {
        this.context = context;
        this.boissonList = boissonList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public BoissonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_boisson, parent, false);
        return new BoissonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BoissonViewHolder holder, int position) {
        Boisson boisson = boissonList.get(position);
        holder.textView.setText(boisson.getNom());
        Picasso.get().load(boisson.getUrlImage()).into(holder.imageView);
        holder.bind(boisson.getId(), clickListener);
    }

    @Override
    public int getItemCount() {
        return boissonList.size();
    }

    public static class BoissonViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textView;

        public BoissonViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            textView = itemView.findViewById(R.id.textView);
        }

        public void bind(final String boissonId, final onClickBoissonListener listener) {
            // Gestion du clic sur l'ensemble de l'élément (itemView)
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onBoissonClick(boissonId);
                }
            });

            // Gestion du clic sur l'image uniquement
            imageView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onBoissonClick(boissonId);
                }
            });
        }
    }
}