package com.sdm.presentslkadmin;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.AdapterView;
import android.widget.Toast;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sdm.presentslkadmin.model.Products;

import java.util.ArrayList;
import java.util.List;

public class AddProductActivity extends AppCompatActivity {
    public static final String TAG = "AddProductActivity";
    private EditText editTextProductName, editTextProductDescription, editTextProductQty, editTextProductPrice;
    private Spinner categorySpinner;
    private String productCategory;
    private Button btnAddProductImage, btnAddProduct;
    private ProgressBar progressBar;
    private ImageView productImage;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    private Uri uriImage;
    private String productName, productDescription, productQty, productPrice;
    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        //Set Status bar color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.blue));
        }

        //Set Spinner
        addCategoryItems();

        btnAddProductImage = findViewById(R.id.btnUploadProductImage);
        btnAddProduct = findViewById(R.id.btnSaveProduct);
        progressBar = findViewById(R.id.progressBar);
        productImage = findViewById(R.id.imageViewProductImage);

        editTextProductName = findViewById(R.id.editTextAddProductName);
        editTextProductDescription = findViewById(R.id.editTextAddProductDescription);
        editTextProductQty = findViewById(R.id.editTextAddProductQty);
        editTextProductPrice = findViewById(R.id.editTextAddProductPrice);
        productImage = findViewById(R.id.imageViewProductImage);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference();

        // Set an OnItemSelectedListener to listen for user selections
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Get the selected item from the spinner
                String selectedItem = parentView.getItemAtPosition(position).toString();

                productCategory = selectedItem;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {

            }
        });

        //Open File Chooser
        btnAddProductImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        btnAddProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Validate Details
                validateProductDetails();
            }
        });
    }

    private void validateProductDetails() {
        productName = editTextProductName.getText().toString().trim();
        productDescription = editTextProductDescription.getText().toString().trim();
        productQty = editTextProductQty.getText().toString().trim();
        productPrice = editTextProductPrice.getText().toString().trim();

        if(uriImage == null){
            Toast.makeText(AddProductActivity.this, "Please Select a Image", Toast.LENGTH_LONG).show();
        } else if(productCategory == null || productCategory.equals("-Select-")){
            Toast.makeText(AddProductActivity.this, "Please Select a Category", Toast.LENGTH_LONG).show();
        } else if (TextUtils.isEmpty(productName)){
            Toast.makeText(AddProductActivity.this, "Please Enter a Product Name", Toast.LENGTH_LONG).show();
            editTextProductName.setError("Product Name is Required");
            editTextProductName.requestFocus();
        } else if (TextUtils.isEmpty(productDescription)){
            Toast.makeText(AddProductActivity.this, "Please Enter a Product Description", Toast.LENGTH_LONG).show();
            editTextProductDescription.setError("Product Description is Required");
            editTextProductDescription.requestFocus();
        } else if (TextUtils.isEmpty(productQty)){
            Toast.makeText(AddProductActivity.this, "Please Enter a Product Quantity", Toast.LENGTH_LONG).show();
            editTextProductQty.setError("Product Quantity is Required");
            editTextProductQty.requestFocus();
        } else if (TextUtils.isEmpty(productPrice)){
            Toast.makeText(AddProductActivity.this, "Please Enter a Product Price", Toast.LENGTH_LONG).show();
            editTextProductPrice.setError("Product Price is Required");
            editTextProductPrice.requestFocus();
        } else {
            saveProduct();
        }

    }
    private void saveProduct() {
        // Generate a unique product ID
        String productId = generateUniqueID();

        progressBar.setVisibility(View.VISIBLE);

        // Upload product image to Storage
        uploadProductImage(productId);
    }
    private String generateUniqueID() {
        return String.valueOf(System.currentTimeMillis());
    }
    private void saveProductToDatabase(String productId, String productImageUri) {
        progressBar.setVisibility(View.VISIBLE);
        // Create a Products object with the entered details
        Products product = new Products(productId, productName, productDescription,
                productQty, productPrice,
                productCategory, productImageUri);

        // Create a "Products" directory if it doesn't exist
        DatabaseReference productsDirectory = databaseReference.child("Products");
        productsDirectory.keepSynced(true);

        // Save the product details in the category subdirectory with the generated product ID
        productsDirectory.child(productId).setValue(product)
                .addOnSuccessListener(aVoid -> {
                    // Product details saved successfully
                    Toast.makeText(AddProductActivity.this, "Product added successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(AddProductActivity.this, ProductActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Product save failed: " + e.getMessage());
                    Toast.makeText(AddProductActivity.this, "Failed to add product", Toast.LENGTH_SHORT).show();
                });
    }

    private void uploadProductImage(String productId) {
        StorageReference imageDirectory = storageReference.child("ProductImages");

        String fileExtension = getFileExtension(uriImage);
        StorageReference imageReference = imageDirectory.child(productId + "." + fileExtension);
        UploadTask uploadTask = imageReference.putFile(uriImage);

        uploadTask.addOnSuccessListener(taskSnapshot -> {

            String imageUrl = productId + "." + fileExtension;

            saveProductToDatabase(productId, imageUrl);

            Toast.makeText(AddProductActivity.this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Image upload failed: " + e.getMessage());
            Toast.makeText(AddProductActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
        });
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
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

    }
    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            uriImage = data.getData();
            productImage.setImageURI(uriImage);
        } else {
            Toast.makeText(AddProductActivity.this, "Image selection canceled", Toast.LENGTH_LONG).show();
        }


    }
}