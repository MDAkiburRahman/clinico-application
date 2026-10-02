package com.example.clinico.Activity;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.clinico.Adapter.MessagesAdapter;
import com.example.clinico.Domain.RecentChatModel;
import com.example.clinico.Helper.FirebaseHelper;
import com.example.clinico.databinding.ActivityMessagesBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MessagesActivity extends BaseActivity {
    private ActivityMessagesBinding binding;
    private List<RecentChatModel> chatList = new ArrayList<>();
    private MessagesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMessagesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initView();
        loadRecentChats();
    }

    private void initView() {
        binding.backBtn.setOnClickListener(v -> finish());
        binding.messagesRv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MessagesAdapter(chatList);
        binding.messagesRv.setAdapter(adapter);
    }

    private void loadRecentChats() {
        String uid = FirebaseHelper.getUid();
        if (uid == null) return;

        binding.progressBar.setVisibility(View.VISIBLE);
        database.getReference("RecentChats").child(uid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        chatList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            RecentChatModel model = ds.getValue(RecentChatModel.class);
                            if (model != null) {
                                model.setId(ds.getKey());
                                chatList.add(model);
                            }
                        }
                        
                        // Sort by timestamp descending
                        Collections.sort(chatList, (c1, c2) -> Long.compare(c2.getTimestamp(), c1.getTimestamp()));
                        
                        adapter.notifyDataSetChanged();
                        binding.progressBar.setVisibility(View.GONE);
                        binding.emptyTxt.setVisibility(chatList.isEmpty() ? View.VISIBLE : View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        binding.progressBar.setVisibility(View.GONE);
                    }
                });
    }
}
