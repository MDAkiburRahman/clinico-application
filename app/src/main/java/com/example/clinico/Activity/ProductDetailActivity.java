package com.example.clinico.Activity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.clinico.Domain.ProductModel;
import com.example.clinico.Helper.CartHelper;
import com.example.clinico.R;
import com.example.clinico.databinding.ActivityProductDetailBinding;

public class ProductDetailActivity extends BaseActivity {
    private ActivityProductDetailBinding binding;
    private ProductModel product;
    private int quantity = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        product = (ProductModel) getIntent().getSerializableExtra("product");
        if (product == null) {
            finish();
            return;
        }

        initView();
    }

    private void initView() {
        binding.backBtn.setOnClickListener(v -> finish());

        binding.nameTxt.setText(product.getName());
        binding.categoryTxt.setText(product.getCategory());
        binding.priceTxt.setText("৳" + product.getPrice());
        binding.descriptionTxt.setText(product.getDescription());
        binding.usageTxt.setText(product.getUsage());
        binding.warningsTxt.setText(product.getWarnings());

        if (product.getStock() > 0) {
            binding.stockStatusTxt.setText("In Stock (" + product.getStock() + ")");
            binding.stockStatusTxt.setTextColor(Color.parseColor("#4CAF50"));
        } else {
            binding.stockStatusTxt.setText("Out of Stock");
            binding.stockStatusTxt.setTextColor(Color.RED);
            binding.addToCartBtn.setEnabled(false);
            binding.addToCartBtn.setAlpha(0.5f);
        }

        Glide.with(this)
                .load(product.getMainImage())
                .placeholder(R.drawable.empty_box)
                .into(binding.mainImg);

        binding.plusBtn.setOnClickListener(v -> {
            if (quantity < product.getStock()) {
                quantity++;
                binding.qtyTxt.setText(String.valueOf(quantity));
            } else {
                Toast.makeText(this, "Maximum stock reached", Toast.LENGTH_SHORT).show();
            }
        });

        binding.minusBtn.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                binding.qtyTxt.setText(String.valueOf(quantity));
            }
        });

        binding.addToCartBtn.setOnClickListener(v -> {
            CartHelper.addToCart(product, quantity, new CartHelper.CartActionListener() {
                @Override
                public void onSuccess() {
                    Toast.makeText(ProductDetailActivity.this, "Added to cart", Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(ProductDetailActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
