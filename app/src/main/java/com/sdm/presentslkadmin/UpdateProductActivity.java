package com.sdm.presentslkadmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.sdm.presentslkadmin.model.Products;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpdateProductActivity extends AppCompatActivity {
    private static final String TAG = "UpdateProductActivity";
    private EditText editTextProductName, editTextProductDescription, editTextProductQty, editTextProductPrice;
    private Spinner categorySpinner;
    private String productCategory;
    private Button btnUpdateProduct;
    private ProgressBar progressBar;
    private ImageView productImage;
    private Task<Uri> storageReference;
    private DatabaseReference databaseReference;
    private Uri uriImage;
    private String productName, productDescription, productQty, productPrice, productId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_product);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.purple));
        }

        productId = getIntent().getStringExtra("productId");

        if(productId != null){
            setupViews();
            addCategoryItems();
            showProductData(productId);

            btnUpdateProduct.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateProducts(productId);
                }
            });
        } else {
            Toast.makeText(UpdateProductActivity.this, "Something Went Wrong!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(UpdateProductActivity.this, ProductActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void updateProducts(String productId) {
        productQty = editTextProductQty.getText().toString().trim();
        productPrice = editTextProductPrice.getText().toString().trim();
        if(productId == null){
            Toast.makeText(UpdateProductActivity.this, "Something Went Wrong!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(UpdateProductActivity.this, ProductActivity.class);
            startActivity(intent);
            finish();
        } else if (TextUtils.isEmpty(productQty)) {
            Toast.makeText(UpdateProductActivity.this, "Please Enter Product QTY", Toast.LENGTH_SHORT).show();
            editTextProductQty.setError("Product Qty is Required.");
            editTextProductQty.requestFocus();
        } else if (TextUtils.isEmpty(productPrice)) {
            Toast.makeText(UpdateProductActivity.this, "Please Enter Product Price", Toast.LENGTH_SHORT).show();
            editTextProductPrice.setError("Product Price is Required.");
            editTextProductPrice.requestFocus();
        } else {

            Map<String, Object> updateFields = new HashMap<>();
            updateFields.put("productQty", productQty);
            updateFields.put("productPrice", productPrice);

            DatabaseReference productRef = FirebaseDatabase.getInstance().getReference("Products").child(productId);

            productRef.updateChildren(updateFields)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(UpdateProductActivity.this, "Product updated successfully", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(UpdateProductActivity.this, ProductActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(UpdateProductActivity.this, "Failed to update product", Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Error updating product: " + e.getMessage());
                            Intent intent = new Intent(UpdateProductActivity.this, ProductActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });

        }
    }

    private void showProductData(String productId) {
        databaseReference = FirebaseDatabase.getInstance().getReference("Products");

        progressBar.setVisibility(View.VISIBLE);

        databaseReference.child(productId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Products products = snapshot.getValue(Products.class);
                if (products != null){
                    productName = products.getProductName();
                    productDescription = products.getProductDescription();
                    productQty = products.getProductQty();
                    productPrice = products.getProductPrice();
                    productCategory = products.getProductCategory();

                    editTextProductName.setText(productName);
                    editTextProductDescription.setText(productDescription);
                    editTextProductPrice.setText(productPrice);
                    editTextProductQty.setText(productQty);

                    setSpinnerValue(productCategory);

                    storageReference = FirebaseStorage.getInstance().getReference("ProductImages/"+products.getImageUrl())
                            .getDownloadUrl()
                            .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Picasso.get()
                                            .load(uri)
                                            .resize(200,200)
                                            .centerCrop()
                                            .into(productImage);
                                    Log.i(TAG, uri.toString());
                                }
                            });

                } else {
                    Toast.makeText(UpdateProductActivity.this, "Something Went Wrong!", Toast.LENGTH_LONG).show();
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UpdateProductActivity.this, "Something Went Wrong!", Toast.LENGTH_LONG).show();
                Log.e(TAG, error.getMessage());
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void setupViews() {
        btnUpdateProduct = findViewById(R.id.btnUpdateProduct_update);
        progressBar = findViewById(R.id.progressBar);
        productImage = findViewById(R.id.imageViewProductImage_update);

        editTextProductName = findViewById(R.id.editTextAddProductName_update);
        editTextProductDescription = findViewById(R.id.editTextAddProductDescription_update);
        editTextProductQty = findViewById(R.id.editTextAddProductQty_update);
        editTextProductPrice = findViewById(R.id.editTextAddProductPrice_update);
        productImage = findViewById(R.id.imageViewProductImage_update);

        editTextProductName.setEnabled(false);
        editTextProductDescription.setEnabled(false);
    }

    private void addCategoryItems(){
        List<String> categories = new ArrayList<>();
        categories.add("-Select-");
        categories.add("Foods");
        categories.add("Electronics");
        categories.add("Fashion");
        categories.add("Outdoor");
        categories.add("Other");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        categorySpinner = findViewById(R.id.spinner);
        categorySpinner.setAdapter(adapter);
        categorySpinner.setEnabled(false);

    }

    private void setSpinnerValue(String category) {
        if (category != null) {
            int index = getCategoryIndex(category);
            if (index != -1) {
                categorySpinner.setSelection(index);
            }
        }
    }

    private int getCategoryIndex(String category) {
        List<String> categories = Arrays.asList("-Select-", "Foods", "Electronics", "Fashion", "Outdoor", "Other");

        return categories.indexOf(category);
    }
}