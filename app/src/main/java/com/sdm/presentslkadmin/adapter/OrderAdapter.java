package com.sdm.presentslkadmin.adapter;

import android.content.Context;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sdm.presentslkadmin.R;
import com.sdm.presentslkadmin.model.Orders;
import com.sdm.presentslkadmin.model.ReadWriteUserDetails;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder>{

    public static final String TAG = "OrderAdapter";
    private ArrayList<Orders> orders;
    private FirebaseStorage storage;
    private Context context;
    private String productId;
    private ArrayAdapter<String> adapter;
    private DatabaseReference databaseReference;
    private FirebaseUser firebaseUser;
    public OrderAdapter(ArrayList<Orders> orders, Context context) {
        this.orders = orders;
        this.context = context;
        this.storage = FirebaseStorage.getInstance();
        this.databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.layout_admin_orders_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        Orders orderDetails = orders.get(position);

        productId = orderDetails.getProductId();

        holder.textUserAddress.setText(formatText("Address : ", orderDetails.getUserAddress()));
        holder.textUserName.setText(formatText("Name : ", firebaseUser.getDisplayName()));
        holder.textUserMobile.setText(formatText("Mobile No : ", orderDetails.getUserMobile()));
        holder.textProductName.setText(formatText("", orderDetails.getProductName()));
        holder.textProductQty.setText(formatText("Qty : ", String.valueOf(orderDetails.getQuantity())));
        holder.textProductPrice.setText(formatText("Price : ", orderDetails.getPrice()));
        holder.textProductTotal.setText(formatText("Total : ", orderDetails.getTotalPrice()));
        holder.textProductShipping.setText(formatText("Shipping : ", "Rs. 2500.00/="));
        holder.textDate.setText(formatText("Date : ", orderDetails.getDate()));
        holder.textTime.setText(formatText("Time : ", orderDetails.getTime()));
        holder.textStatus.setText(orderDetails.getStatus());

        String imagePath = "ProductImages/" + orderDetails.getImageUrl();

        StorageReference storageReference = FirebaseStorage.getInstance().getReference(imagePath);

        storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
            Picasso.get()
                    .load(uri)
                    .resize(200, 280)
                    .centerCrop()
                    .into(holder.productImage);
        }).addOnFailureListener(exception -> {
            Log.e(TAG, "Error getting download URL", exception);
        });


        addStatusItems();
        int statusIndex = adapter.getPosition(orderDetails.getStatus());
        holder.spinnerStatus.setAdapter(adapter);
        holder.spinnerStatus.setSelection(statusIndex);

        holder.spinnerStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int selectedItemPosition, long id) {
                String selectedStatus = parentView.getItemAtPosition(selectedItemPosition).toString();

                updateOrderStatus(orderDetails.getUserId(), orderDetails.getKey(), selectedStatus, orderDetails.getProductId());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
    }

    private void updateOrderStatus(String userId, String orderKey, String newStatus, String productId) {
        DatabaseReference ordersRef = databaseReference.child("Registered Users");

        DatabaseReference userOrdersRef = ordersRef.child(userId).child("Orders").child(orderKey);

        DatabaseReference specificOrderRef = userOrdersRef.child(productId);

        specificOrderRef.child("status").setValue(newStatus);
    }

    @Override
    public int getItemCount() {
        Log.i("SIZE", String.valueOf(orders.size()));
        return orders.size();
    }

    private SpannableString formatText(String prefix, String value) {
        String text = prefix + value;
        SpannableString spannableString = new SpannableString(text);
        spannableString.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, prefix.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        return spannableString;
    }

    private void addStatusItems(){
        List<String> categories = new ArrayList<>();
        categories.add("Order Placed");
        categories.add("Processing");
        categories.add("Shipped");
        categories.add("Delivered");
        categories.add("Rejected");
        categories.add("Finished");
        categories.add("Closed");

        adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textUserName, textUserMobile, textUserAddress, textProductName, textProductQty, textProductPrice, textProductTotal, textProductShipping, textDate, textTime, textStatus;
        ImageButton productImage;
        Spinner spinnerStatus;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textUserName = itemView.findViewById(R.id.textViewUsername_orders);
            textUserMobile = itemView.findViewById(R.id.textViewMobile_orders);
            textUserAddress = itemView.findViewById(R.id.textViewAddress_orders);
            productImage = itemView.findViewById(R.id.imageButtonProductImage_orders);
            textProductName = itemView.findViewById(R.id.textViewProductName_orders);
            textProductQty = itemView.findViewById(R.id.textViewProductQty_orders);
            textProductPrice = itemView.findViewById(R.id.textViewProductPrice_orders);
            textProductTotal = itemView.findViewById(R.id.textViewProductTotal_orders);
            textProductShipping = itemView.findViewById(R.id.textViewProductShipping_orders);
            textDate = itemView.findViewById(R.id.textViewDate_orders);
            textTime = itemView.findViewById(R.id.textViewTime_orders);
            spinnerStatus = itemView.findViewById(R.id.spinnerChangeOrderStatus);
            textStatus = itemView.findViewById(R.id.textViewStatus_orders);

        }
    }
}
