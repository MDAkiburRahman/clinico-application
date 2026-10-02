package com.example.clinico.Helper;

import androidx.annotation.NonNull;

import com.example.clinico.Domain.CartItemModel;
import com.example.clinico.Domain.ProductModel;
import com.example.clinico.Utils.Constants;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class CartHelper {

    public interface CartActionListener {
        void onSuccess();
        void onError(String error);
    }

    public static void addToCart(ProductModel product, int quantity, CartActionListener listener) {
        String uid = FirebaseHelper.getUid();
        if (uid == null) {
            listener.onError("Please login first");
            return;
        }

        DatabaseReference cartRef = FirebaseHelper.getDatabase().getReference(Constants.NODE_MEDICINE_CARTS).child(uid);
        
        cartRef.child(product.getProductId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    CartItemModel existing = snapshot.getValue(CartItemModel.class);
                    if (existing != null) {
                        int newQty = existing.getQuantity() + quantity;
                        if (newQty > product.getStock()) newQty = product.getStock();
                        existing.setQuantity(newQty);
                        cartRef.child(product.getProductId()).setValue(existing)
                                .addOnSuccessListener(aVoid -> listener.onSuccess())
                                .addOnFailureListener(e -> listener.onError(e.getMessage()));
                    }
                } else {
                    CartItemModel item = new CartItemModel(
                            product.getProductId(),
                            product.getProductId(),
                            product.getName(),
                            product.getMainImage(),
                            product.getPrice(),
                            quantity
                    );
                    cartRef.child(product.getProductId()).setValue(item)
                            .addOnSuccessListener(aVoid -> listener.onSuccess())
                            .addOnFailureListener(e -> listener.onError(e.getMessage()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onError(error.getMessage());
            }
        });
    }

    public static void removeFromCart(String productId, CartActionListener listener) {
        String uid = FirebaseHelper.getUid();
        if (uid == null) return;

        FirebaseHelper.getDatabase().getReference(Constants.NODE_MEDICINE_CARTS).child(uid).child(productId)
                .removeValue()
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }
}
