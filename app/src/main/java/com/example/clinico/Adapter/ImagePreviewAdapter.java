package com.example.clinico.Adapter;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.clinico.R;
import java.util.List;

public class ImagePreviewAdapter extends RecyclerView.Adapter<ImagePreviewAdapter.ViewHolder> {
    private List<Uri> uris;
    private OnRemoveListener listener;

    public interface OnRemoveListener {
        void onRemove(int position);
    }

    public ImagePreviewAdapter(List<Uri> uris, OnRemoveListener listener) {
        this.uris = uris;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image_preview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Uri uri = uris.get(position);
        holder.imageView.setImageURI(uri);
        holder.removeBtn.setOnClickListener(v -> listener.onRemove(position));
    }

    @Override
    public int getItemCount() {
        return uris.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView, removeBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.previewImg);
            removeBtn = itemView.findViewById(R.id.removeBtn);
        }
    }
}
