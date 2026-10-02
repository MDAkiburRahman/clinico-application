package com.example.clinico.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.clinico.Activity.ChatActivity;
import com.example.clinico.Domain.RecentChatModel;
import com.example.clinico.R;
import com.example.clinico.databinding.ViewholderRecentChatBinding;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.ViewHolder> {
    private List<RecentChatModel> items;
    private Context context;

    public MessagesAdapter(List<RecentChatModel> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        ViewholderRecentChatBinding binding = ViewholderRecentChatBinding.inflate(
                LayoutInflater.from(context), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecentChatModel item = items.get(position);
        holder.binding.nameTxt.setText(item.getName());
        holder.binding.lastMsgTxt.setText(item.getLastMessage());
        
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        holder.binding.timeTxt.setText(sdf.format(new Date(item.getTimestamp())));

        Glide.with(context)
                .load(item.getPicture())
                .placeholder(R.drawable.ic_person)
                .into(holder.binding.profileImg);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("receiverId", item.getId());
            intent.putExtra("receiverName", item.getName());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ViewholderRecentChatBinding binding;
        public ViewHolder(ViewholderRecentChatBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
