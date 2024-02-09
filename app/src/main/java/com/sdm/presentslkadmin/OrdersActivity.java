package com.sdm.presentslkadmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.sdm.presentslkadmin.adapter.OrderAdapter;
import com.sdm.presentslkadmin.adapter.UsersAdapter;
import com.sdm.presentslkadmin.model.Orders;
import com.sdm.presentslkadmin.model.ReadWriteUserDetails;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class OrdersActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "OrderActivity";
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private MaterialToolbar toolbar;
    private TextView sideBarUserName, sideBarUserEmail;
    private ImageView sideBarUserProfilePic;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private String fullName, email, orderStatus;
    private BottomNavigationView bottomNavigationView;
    private FirebaseStorage storage;
    private DatabaseReference databaseReference;
    private ArrayList<Orders> orders;
    private ProgressBar progressBar;
    private OrderAdapter orderAdapter;
    private Spinner orderStatusSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.blue));
        }

        setupViews();
        setupFireBase();
        setupActionBar();
        addStatusItems();
        getStatusItems();
//        setupOrderDetails();
    }

    private void setupOrderDetails() {
        progressBar.setVisibility(View.VISIBLE);
        databaseReference = FirebaseDatabase.getInstance().getReference("Registered Users");
        orders = new ArrayList<>();

        RecyclerView itemView = findViewById(R.id.recyclerOrderView_adminOrders);

        orderAdapter = new OrderAdapter(orders, OrdersActivity.this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        itemView.setLayoutManager(linearLayoutManager);
        itemView.setAdapter(orderAdapter);

        progressBar.setVisibility(View.VISIBLE);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                orders.clear();
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    Log.i(TAG, userSnapshot.getKey().toString());
                    if (userSnapshot.child("Orders").exists()) {
                        for (DataSnapshot orderSnapshot : userSnapshot.child("Orders").getChildren()) {
                            Log.i(TAG, orderSnapshot.getKey().toString());

                            if (orderSnapshot.hasChildren()) {
                                for (DataSnapshot subCollectionSnapshot : orderSnapshot.getChildren()) {
                                    Log.i(TAG, subCollectionSnapshot.getKey().toString());

                                    Orders orderDetails = new Orders();

                                    if (firebaseUser != null &&
                                            userSnapshot.child("address").exists() &&
                                            userSnapshot.child("mobile").exists() &&
                                            subCollectionSnapshot.child("productName").exists() &&
                                            subCollectionSnapshot.getKey() != null &&
                                            subCollectionSnapshot.child("price").exists() &&
                                            subCollectionSnapshot.child("totalPrice").exists() &&
                                            subCollectionSnapshot.child("quantity").exists() &&
                                            subCollectionSnapshot.child("imageUrl").exists() &&
                                            subCollectionSnapshot.child("date").exists() &&
                                            subCollectionSnapshot.child("time").exists() &&
                                            subCollectionSnapshot.child("status").exists() &&
                                            userSnapshot.getKey() != null &&
                                            orderSnapshot.getKey() != null) {

                                        orderDetails.setUserName(firebaseUser.getDisplayName());
                                        orderDetails.setUserAddress(userSnapshot.child("address").getValue(String.class));
                                        orderDetails.setUserMobile(userSnapshot.child("mobile").getValue(String.class));

                                        orderDetails.setProductName(subCollectionSnapshot.child("productName").getValue(String.class));
                                        orderDetails.setProductId(subCollectionSnapshot.getKey());
                                        orderDetails.setPrice(subCollectionSnapshot.child("price").getValue(String.class));
                                        orderDetails.setTotalPrice(subCollectionSnapshot.child("totalPrice").getValue(String.class));
                                        Integer quantityInteger = subCollectionSnapshot.child("quantity").getValue(Integer.class);
                                        int quantity = (quantityInteger != null) ? quantityInteger : 0;
                                        orderDetails.setQuantity(quantity);
                                        orderDetails.setImageUrl(subCollectionSnapshot.child("imageUrl").getValue(String.class));
                                        orderDetails.setDate(subCollectionSnapshot.child("date").getValue(String.class));
                                        orderDetails.setTime(subCollectionSnapshot.child("time").getValue(String.class));
                                        orderDetails.setStatus(subCollectionSnapshot.child("status").getValue(String.class));
                                        orderDetails.setKey(orderSnapshot.getKey());
                                        orderDetails.setUserId(userSnapshot.getKey());

                                        orders.add(orderDetails);
                                    }
                                }
                            } else {
                                Toast.makeText(OrdersActivity.this, "Details are Currently Unavailable.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        Toast.makeText(OrdersActivity.this, "Orders are Currently Unavailable.", Toast.LENGTH_SHORT).show();
                    }
                }
                orderAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, error.getMessage());
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void getStatusItems() {
        orderStatusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedItem = parentView.getItemAtPosition(position).toString();

                orderStatus = selectedItem;
                Log.i(TAG, orderStatus);

                fetchDataBasedOnOrderStatus(orderStatus);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {

            }
        });
    }

    private void addStatusItems() {
        List<String> categories = new ArrayList<>();
        categories.add("-Select Status-");
        categories.add("Order Placed");
        categories.add("Processing");
        categories.add("Shipped");
        categories.add("Delivered");
        categories.add("Rejected");
        categories.add("Finished");
        categories.add("Closed");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        orderStatusSpinner.setAdapter(adapter);

        orderStatusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedItem = parentView.getItemAtPosition(position).toString();

                orderStatus = selectedItem;

                fetchDataBasedOnOrderStatus(orderStatus);
                Log.i(TAG, orderStatus);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {

            }
        });
    }

    private void fetchDataBasedOnOrderStatus(String selectedOrderStatus) {
        progressBar.setVisibility(View.VISIBLE);
        databaseReference = FirebaseDatabase.getInstance().getReference("Registered Users");
        orders = new ArrayList<>();

        RecyclerView itemView = findViewById(R.id.recyclerOrderView_adminOrders);

        orderAdapter = new OrderAdapter(orders, OrdersActivity.this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        itemView.setLayoutManager(linearLayoutManager);
        itemView.setAdapter(orderAdapter);

        progressBar.setVisibility(View.VISIBLE);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                orders.clear();

                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    if (userSnapshot.child("Orders").exists()) {
                        for (DataSnapshot orderSnapshot : userSnapshot.child("Orders").getChildren()) {
                            if (orderSnapshot.hasChildren()) {
                                for (DataSnapshot subCollectionSnapshot : orderSnapshot.getChildren()) {
                                    if ("-Select Status-".equals(selectedOrderStatus) ||
                                            (subCollectionSnapshot.child("status").exists() &&
                                                    subCollectionSnapshot.child("status").getValue(String.class).equals(selectedOrderStatus))) {

                                        Orders orderDetails = new Orders();

                                        if (firebaseUser != null &&
                                                userSnapshot.child("address").exists() &&
                                                userSnapshot.child("mobile").exists() &&
                                                subCollectionSnapshot.child("productName").exists() &&
                                                subCollectionSnapshot.getKey() != null &&
                                                subCollectionSnapshot.child("price").exists() &&
                                                subCollectionSnapshot.child("totalPrice").exists() &&
                                                subCollectionSnapshot.child("quantity").exists() &&
                                                subCollectionSnapshot.child("imageUrl").exists() &&
                                                subCollectionSnapshot.child("date").exists() &&
                                                subCollectionSnapshot.child("time").exists() &&
                                                subCollectionSnapshot.child("status").exists() &&
                                                userSnapshot.getKey() != null &&
                                                orderSnapshot.getKey() != null) {

                                            orderDetails.setUserName(firebaseUser.getDisplayName());
                                            orderDetails.setUserAddress(userSnapshot.child("address").getValue(String.class));
                                            orderDetails.setUserMobile(userSnapshot.child("mobile").getValue(String.class));

                                            orderDetails.setProductName(subCollectionSnapshot.child("productName").getValue(String.class));
                                            orderDetails.setProductId(subCollectionSnapshot.getKey());
                                            orderDetails.setPrice(subCollectionSnapshot.child("price").getValue(String.class));
                                            orderDetails.setTotalPrice(subCollectionSnapshot.child("totalPrice").getValue(String.class));
                                            Integer quantityInteger = subCollectionSnapshot.child("quantity").getValue(Integer.class);
                                            int quantity = (quantityInteger != null) ? quantityInteger : 0;
                                            orderDetails.setQuantity(quantity);
                                            orderDetails.setImageUrl(subCollectionSnapshot.child("imageUrl").getValue(String.class));
                                            orderDetails.setDate(subCollectionSnapshot.child("date").getValue(String.class));
                                            orderDetails.setTime(subCollectionSnapshot.child("time").getValue(String.class));
                                            orderDetails.setStatus(subCollectionSnapshot.child("status").getValue(String.class));
                                            orderDetails.setKey(orderSnapshot.getKey());
                                            orderDetails.setUserId(userSnapshot.getKey());

                                            orders.add(orderDetails);
                                        }
                                    }
                                }
                            } else {
                                orders.clear();
                            }
                        }
                    } else {
                        orders.clear();
                    }
                }

                orderAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, error.getMessage());
                progressBar.setVisibility(View.GONE);
            }
        });
    }
    private void setupFireBase() {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser == null) {
            Toast.makeText(OrdersActivity.this, "User details are not available at the moment.", Toast.LENGTH_LONG).show();
        } else {
            showUserProfile(firebaseUser);
            Log.i("USER", firebaseUser.toString());
//            setupOrderDetails();
        }
    }
    private void setupActionBar() {
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle =
                new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        navigationView.setNavigationItemSelectedListener(this);

        // Initialize and assign variable
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        // Set home selector
        bottomNavigationView.setSelectedItemId(R.id.orders);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                // Handle navigation item selection here
                bottomNavigation(item.getItemId());
                return true;
            }
        });
    }
    private void setupViews() {
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        toolbar = findViewById(R.id.toolBar);

        // Inflate the header view and find the views within it
        View headerView = navigationView.getHeaderView(0);
        sideBarUserName = headerView.findViewById(R.id.sideBarUserName);
        sideBarUserEmail = headerView.findViewById(R.id.sideBarUserEmail);
        sideBarUserProfilePic = headerView.findViewById(R.id.sideBarUserProfilePic);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        storage = FirebaseStorage.getInstance();

        progressBar = findViewById(R.id.progressBar_users);

        orderStatusSpinner = findViewById(R.id.spinnerOrderStatus);

        ImageButton refreshButton = findViewById(R.id.imgBtnRefresh);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
                orderStatusSpinner.setSelection(0);
                setupOrderDetails();
                addStatusItems();

            }
        });
    }
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        View currentFocus = getCurrentFocus();
        if (currentFocus != null) {
            imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
        } else {
            View view = getWindow().getDecorView();
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
    private void showUserProfile(FirebaseUser firebaseUser) {
        String userID = firebaseUser.getUid();

        //Extracting User Reference from Database for "Registered Users"
        DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Admins");
        referenceProfile.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ReadWriteUserDetails readUserDetail = snapshot.getValue(ReadWriteUserDetails.class);
                if(readUserDetail != null){
                    fullName = firebaseUser.getDisplayName();
                    email = firebaseUser.getEmail();

                    if(fullName != null){
                        sideBarUserName.setText(fullName);
                    } else {
                        sideBarUserName.setText("");
                    }

                    if(email != null){
                        sideBarUserEmail.setText(email);
                    } else {
                        sideBarUserEmail.setText("");
                    }

                    //Set User DP (After user has Uploaded)
                    Uri uri = firebaseUser.getPhotoUrl();

                    Log.i("USER", fullName);
                    Log.i("USER", email);

                    if(uri != null){
                        Picasso.get().load(uri).into(sideBarUserProfilePic);
                    } else {
                        Picasso.get().load(R.drawable.no_profile_pic).into(sideBarUserProfilePic);
                    }

                } else {
                    Toast.makeText(OrdersActivity.this, "Something Went Wrong!", Toast.LENGTH_LONG).show();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(OrdersActivity.this, "Something Went Wrong!", Toast.LENGTH_LONG).show();
            }
        });
    }
    private void bottomNavigation(int itemId) {
        if (itemId == R.id.home) {
            startActivity(new Intent(OrdersActivity.this, HomeViewActivity.class));
            overridePendingTransition(0, 0);
        } else if (itemId == R.id.products) {
            startActivity(new Intent(OrdersActivity.this, ProductActivity.class));
            overridePendingTransition(0, 0);
        } else if (itemId == R.id.users) {
            startActivity(new Intent(OrdersActivity.this, UsersActivity.class));
            overridePendingTransition(0, 0);
        } else if (itemId == R.id.orders) {

        } else if (itemId == R.id.userprofile) {
            startActivity(new Intent(OrdersActivity.this, UserProfileActivity.class));
            overridePendingTransition(0, 0);
        }
    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation item selection here
        if (item.getItemId() == R.id.sideNavHome) {
            startActivity(new Intent(OrdersActivity.this, HomeViewActivity.class));
            overridePendingTransition(0, 0);
            bottomNavigationView.setSelectedItemId(R.id.sideNavHome);
        } else if (item.getItemId() == R.id.sideNavProducts) {
            startActivity(new Intent(OrdersActivity.this, ProductActivity.class));
            overridePendingTransition(0, 0);
            bottomNavigationView.setSelectedItemId(R.id.products);
        } else if (item.getItemId() == R.id.sideNavUsers) {
            startActivity(new Intent(OrdersActivity.this, UsersActivity.class));
            overridePendingTransition(0, 0);
            bottomNavigationView.setSelectedItemId(R.id.users);
        } else if (item.getItemId() == R.id.sideNavOrders) {

        }  else if (item.getItemId() == R.id.sideNavUserprofile) {
            startActivity(new Intent(OrdersActivity.this, UserProfileActivity.class));
            overridePendingTransition(0, 0);
            bottomNavigationView.setSelectedItemId(R.id.userprofile);
        }

        // Close the drawer
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}