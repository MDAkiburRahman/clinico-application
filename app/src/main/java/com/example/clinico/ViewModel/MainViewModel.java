package com.example.clinico.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.clinico.Domain.BannerModel;
import com.example.clinico.Domain.CategoryModel;
import com.example.clinico.Domain.DoctorsModel;
import com.example.clinico.Repository.MainRepository;

import java.util.List;

public class MainViewModel extends ViewModel {
    private final MainRepository repository;

    public MainViewModel() {
        this.repository = new MainRepository();
    }

    public LiveData<List<CategoryModel>> loadCategories() {
        return repository.loadCategories();
    }

    public LiveData<List<DoctorsModel>> loadDoctors() {
        return repository.loadDoctor();
    }

    public LiveData<List<BannerModel>> loadBanners() {
        return repository.loadBanners();
    }

    public LiveData<List<String>> loadHealthTips() {
        return repository.loadHealthTips();
    }

}
