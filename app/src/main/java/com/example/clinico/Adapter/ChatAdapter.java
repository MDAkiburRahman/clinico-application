package com.example.clinico.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.clinico.Domain.MessageModel;
import com.example.clinico.Helper.FirebaseHelper;
import com.example.clinico.R;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<MessageModel> messages;
    private static final int MSG_TYPE_SENT = 1;
    private static final int MSG_TYPE_RECEIVED = 2;

    public ChatAdapter(List<MessageModel> messages) {
        this.messages = messages;
    }

    @Override
    public int getItemViewType(int position) {
        if (messages.get(position).getSenderId().equals(FirebaseHelper.getUid())) {
            return MSG_TYPE_SENT;
        } else {
            return MSG_TYPE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_SENT) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_sent, parent, false);
            return new SentViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_received, parent, false);
            return new ReceivedViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MessageModel message = messages.get(position);
        String time = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date(message.getTimestamp()));

        if (holder instanceof SentViewHolder) {
            ((SentViewHolder) holder).messageTxt.setText(message.getMessage());
            ((SentViewHolder) holder).timeTxt.setText(time);
        } else {
            ((ReceivedViewHolder) holder).messageTxt.setText(message.getMessage());
            ((ReceivedViewHolder) holder).timeTxt.setText(time);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class SentViewHolder extends RecyclerView.ViewHolder {
        TextView messageTxt, timeTxt;
        SentViewHolder(View itemView) {
            super(itemView);
            messageTxt = itemView.findViewById(R.id.messageTxt);
            timeTxt = itemView.findViewById(R.id.timeTxt);
        }
    }

    static class ReceivedViewHolder extends RecyclerView.ViewHolder {
        TextView messageTxt, timeTxt;
        ReceivedViewHolder(View itemView) {
            super(itemView);
            messageTxt = itemView.findViewById(R.id.messageTxt);
            timeTxt = itemView.findViewById(R.id.timeTxt);
        }
    }
}
