package com.sdm.presentslkadmin.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.sdm.presentslkadmin.R;
import com.sdm.presentslkadmin.UpdateProductActivity;
import com.sdm.presentslkadmin.model.Products;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdminItemAdapter extends RecyclerView.Adapter<AdminItemAdapter.ViewHolder> {
    public static final String TAG = "AdminItemAdapter";
    private ArrayList<Products> products;
    private FirebaseStorage storage;
    private Context context;
    private String category, id;
    public AdminItemAdapter(ArrayList<Products> products, Context context) {
        this.products = products;
        this.context = context;
        this.storage = FirebaseStorage.getInstance();
    }

    @NonNull
    @Override
    public AdminItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.layout_admin_product_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminItemAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Products pro = products.get(position);
        holder.textProductName.setText(pro.getProductName());
        holder.textProductPrice.setText("Price : Rs. "+pro.getProductPrice()+"/=");
        holder.textProductQty.setText("Qty "+pro.getProductQty());

        category = pro.getProductCategory();
        id = pro.getProductId();

        storage.getReference("ProductImages/"+pro.getImageUrl())
                .getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get()
                                .load(uri)
                                .resize(200,200)
                                .centerCrop()
                                .into(holder.productImage);
                        Log.i(TAG, uri.toString());
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, e.getMessage());
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });

        //Update Product Button
        holder.btnUpdateProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Products currentProduct = products.get(position);
                String productId = currentProduct.getProductId();

                Intent intent = new Intent(context, UpdateProductActivity.class);
                intent.putExtra("productId", productId);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView textProductName, textProductPrice, textProductQty;
        ImageView productImage;
        Button btnUpdateProduct;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textProductName = itemView.findViewById(R.id.textViewProductName_admin);
            textProductPrice = itemView.findViewById(R.id.textViewProductPrice_admin);
            textProductQty = itemView.findViewById(R.id.textViewProductQty_admin);
            productImage = itemView.findViewById(R.id.imageViewProductImage_admin);
            btnUpdateProduct = itemView.findViewById(R.id.buttonUpdateItem);
        }
    }
}
