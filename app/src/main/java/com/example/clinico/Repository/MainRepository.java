package com.example.clinico.Repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.clinico.Domain.BannerModel;
import com.example.clinico.Domain.CategoryModel;
import com.example.clinico.Domain.DoctorsModel;
import com.example.clinico.Helper.FirebaseHelper;
import com.example.clinico.Utils.Constants;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainRepository {

    public LiveData<List<CategoryModel>> loadCategories() {
        final MutableLiveData<List<CategoryModel>> listData = new MutableLiveData<>();
        DatabaseReference ref = FirebaseHelper.getDatabase().getReference(Constants.NODE_CATEGORIES);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<CategoryModel> lists = new ArrayList<>();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    CategoryModel item = childSnapshot.getValue(CategoryModel.class);
                    if (item != null) {
                        lists.add(item);
                    }
                }
                listData.setValue(lists);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        return listData;
    }

    public LiveData<List<DoctorsModel>> loadDoctor() {
        final MutableLiveData<List<DoctorsModel>> listData = new MutableLiveData<>();
        DatabaseReference ref = FirebaseHelper.getDoctorsRef();
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<DoctorsModel> lists = new ArrayList<>();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    DoctorsModel item = childSnapshot.getValue(DoctorsModel.class);
                    if (item != null) {
                        lists.add(item);
                    }
                }
                listData.setValue(lists);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        return listData;
    }

    public LiveData<List<BannerModel>> loadBanners() {
        final MutableLiveData<List<BannerModel>> listData = new MutableLiveData<>();
        DatabaseReference ref = FirebaseHelper.getDatabase().getReference(Constants.NODE_BANNERS);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<BannerModel> lists = new ArrayList<>();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    BannerModel item = childSnapshot.getValue(BannerModel.class);
                    if (item != null && item.getUrl() != null) {
                        item.setKey(childSnapshot.getKey());
                        lists.add(item);
                    }
                }
                listData.setValue(lists);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        return listData;
    }

    public LiveData<List<String>> loadHealthTips() {
        final MutableLiveData<List<String>> listData = new MutableLiveData<>();
        DatabaseReference ref = FirebaseHelper.getDatabase().getReference(Constants.NODE_HEALTH_TIPS);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> tips = new ArrayList<>();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    String tip = childSnapshot.getValue(String.class);
                    if (tip != null) {
                        tips.add(tip);
                    }
                }
                listData.setValue(tips);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        return listData;
    }

}
