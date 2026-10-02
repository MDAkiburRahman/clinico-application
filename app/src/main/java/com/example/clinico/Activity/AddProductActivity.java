package com.example.clinico.Activity;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.example.clinico.Domain.ProductModel;
import com.example.clinico.Helper.FirebaseHelper;
import com.example.clinico.Utils.Constants;
import com.example.clinico.databinding.ActivityAddProductBinding;
import com.google.firebase.storage.StorageReference;

public class AddProductActivity extends BaseActivity {
    private ActivityAddProductBinding binding;
    private ProductModel editProduct;
    private Uri selectedImageUri;

    private final ActivityResultLauncher<PickVisualMediaRequest> pickImage = registerForActivityResult(
            new ActivityResultContracts.PickVisualMedia(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    binding.productImg.setImageURI(uri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddProductBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        editProduct = (ProductModel) getIntent().getSerializableExtra("product");
        if (editProduct != null) {
            setupEditMode();
        }

        initView();
    }

    private void setupEditMode() {
        binding.titleTxt.setText("Edit Product");
        binding.nameEt.setText(editProduct.getName());
        binding.genericNameEt.setText(editProduct.getGenericName());
        binding.priceEt.setText(String.valueOf(editProduct.getPrice()));
        binding.stockEt.setText(String.valueOf(editProduct.getStock()));
        binding.categoryEt.setText(editProduct.getCategory());
        binding.descriptionEt.setText(editProduct.getDescription());
        binding.usageEt.setText(editProduct.getUsage());
        binding.warningsEt.setText(editProduct.getWarnings());
        binding.prescriptionCb.setChecked(editProduct.isPrescriptionRequired());

        Glide.with(this).load(editProduct.getMainImage()).into(binding.productImg);
    }

    private void initView() {
        binding.backBtn.setOnClickListener(v -> finish());
        binding.productImg.setOnClickListener(v -> pickImage.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build()));

        binding.saveBtn.setOnClickListener(v -> saveProduct());
    }

    private void saveProduct() {
        String name = binding.nameEt.getText().toString().trim();
        String priceStr = binding.priceEt.getText().toString().trim();
        String stockStr = binding.stockEt.getText().toString().trim();

        if (name.isEmpty() || priceStr.isEmpty() || stockStr.isEmpty()) {
            Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedImageUri == null && editProduct == null) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.saveBtn.setEnabled(false);

        if (selectedImageUri != null) {
            uploadImageAndSave(name, Double.parseDouble(priceStr), Integer.parseInt(stockStr));
        } else {
            saveToDatabase(editProduct.getMainImage(), name, Double.parseDouble(priceStr), Integer.parseInt(stockStr));
        }
    }

    private void uploadImageAndSave(String name, double price, int stock) {
        String productId = editProduct != null ? editProduct.getProductId() : database.getReference(Constants.NODE_MEDICAL_PRODUCTS).push().getKey();
        StorageReference ref = FirebaseHelper.getStorage().getReference().child("medical_products/" + productId + ".jpg");

        ref.putFile(selectedImageUri).addOnSuccessListener(taskSnapshot -> {
            ref.getDownloadUrl().addOnSuccessListener(uri -> {
                saveToDatabase(uri.toString(), name, price, stock);
            });
        }).addOnFailureListener(e -> {
            binding.progressBar.setVisibility(View.GONE);
            binding.saveBtn.setEnabled(true);
            Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void saveToDatabase(String imageUrl, String name, double price, int stock) {
        String productId = editProduct != null ? editProduct.getProductId() : database.getReference(Constants.NODE_MEDICAL_PRODUCTS).push().getKey();
        
        ProductModel product = editProduct != null ? editProduct : new ProductModel();
        product.setProductId(productId);
        product.setName(name);
        product.setGenericName(binding.genericNameEt.getText().toString());
        product.setPrice(price);
        product.setStock(stock);
        product.setCategory(binding.categoryEt.getText().toString());
        product.setDescription(binding.descriptionEt.getText().toString());
        product.setUsage(binding.usageEt.getText().toString());
        product.setWarnings(binding.warningsEt.getText().toString());
        product.setPrescriptionRequired(binding.prescriptionCb.isChecked());
        product.setMainImage(imageUrl);
        product.setAvailable(stock > 0);
        product.setUpdatedAt(System.currentTimeMillis());
        if (editProduct == null) product.setCreatedAt(System.currentTimeMillis());

        database.getReference(Constants.NODE_MEDICAL_PRODUCTS).child(productId).setValue(product)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Product saved", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.saveBtn.setEnabled(true);
                    Toast.makeText(this, "Save failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
