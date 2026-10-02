package com.example.clinico.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.clinico.Adapter.ChatAdapter;
import com.example.clinico.Domain.MessageModel;
import com.example.clinico.Helper.FirebaseHelper;
import com.example.clinico.databinding.ActivityChatBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends BaseActivity {
    private ActivityChatBinding binding;
    private String receiverId;
    private String receiverName;
    private ChatAdapter adapter;
    private List<MessageModel> messageList = new ArrayList<>();
    private String chatId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        receiverId = getIntent().getStringExtra("receiverId");
        receiverName = getIntent().getStringExtra("receiverName");

        if (receiverId == null) {
            finish();
            return;
        }

        initView();
        setupChatId();
        loadMessages();
    }

    private void initView() {
        binding.chatTitle.setText(receiverName);
        binding.backBtn.setOnClickListener(v -> finish());

        binding.chatRv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ChatAdapter(messageList);
        binding.chatRv.setAdapter(adapter);

        binding.sendBtn.setOnClickListener(v -> sendMessage());
    }

    private void setupChatId() {
        String senderId = FirebaseHelper.getUid();
        if (senderId.compareTo(receiverId) < 0) {
            chatId = senderId + "_" + receiverId;
        } else {
            chatId = receiverId + "_" + senderId;
        }
    }

    private void loadMessages() {
        FirebaseDatabase.getInstance().getReference("Chats").child(chatId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messageList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            MessageModel message = ds.getValue(MessageModel.class);
                            if (message != null) {
                                messageList.add(message);
                            }
                        }
                        adapter.notifyDataSetChanged();
                        if (!messageList.isEmpty()) {
                            binding.chatRv.smoothScrollToPosition(messageList.size() - 1);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void sendMessage() {
        String messageText = binding.messageEt.getText().toString().trim();
        if (messageText.isEmpty()) return;

        String senderId = FirebaseHelper.getUid();
        String messageId = FirebaseDatabase.getInstance().getReference("Chats").child(chatId).push().getKey();

        MessageModel message = new MessageModel(messageId, senderId, receiverId, messageText, System.currentTimeMillis());

        if (messageId != null) {
            FirebaseDatabase.getInstance().getReference("Chats").child(chatId).child(messageId).setValue(message)
                    .addOnSuccessListener(aVoid -> {
                        binding.messageEt.setText("");
                        updateRecentChats(messageText);
                    })
                    .addOnFailureListener(e -> Toast.makeText(ChatActivity.this, "Failed to send message", Toast.LENGTH_SHORT).show());
        }
    }

    private void updateRecentChats(String messageText) {
        String senderId = FirebaseHelper.getUid();
        long timestamp = System.currentTimeMillis();

        // Update for sender
        com.example.clinico.Domain.RecentChatModel senderUpdate = new com.example.clinico.Domain.RecentChatModel(
                receiverId, receiverName, messageText, timestamp, "");
        FirebaseDatabase.getInstance().getReference("RecentChats").child(senderId).child(receiverId).setValue(senderUpdate);

        // Update for receiver (need sender's name - usually you'd fetch it or have it)
        // For now, let's at least update the receiver's list with the sender's ID
        // In a real app, you'd fetch the sender's name from Users node
        database.getReference("Users").child(senderId).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String senderName = snapshot.exists() ? snapshot.getValue(String.class) : "User";
                com.example.clinico.Domain.RecentChatModel receiverUpdate = new com.example.clinico.Domain.RecentChatModel(
                        senderId, senderName, messageText, timestamp, "");
                FirebaseDatabase.getInstance().getReference("RecentChats").child(receiverId).child(senderId).setValue(receiverUpdate);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
