package com.sdm.presentslkadmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import com.sdm.presentslkadmin.model.ReadWriteUserDetails;
import com.squareup.picasso.Picasso;

public class HomeViewActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "HomeViewActivity";
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private MaterialToolbar toolbar;
    private TextView sideBarUserName, sideBarUserEmail, textUserCount, textAdminCount, textProductsCount, textOrderCount, textLowQtyProducts;
    private ImageView sideBarUserProfilePic;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private String fullName, email;
    private BottomNavigationView bottomNavigationView;
    private DatabaseReference userReference, adminReference, productReference, orderReference, lowQtyProductReference;
    private ProgressBar progressBarOrders, progressBarUsers, progressBarAdmins, progressBarProducts, progressBarLowQtyProducts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_view);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.blue));
        }

        setupViews();
        setupFirebase();
        setupNavigation();

        getUsersCount();
        getAdminCount();
        getOrdersCount();
        getProductsCount();
        getLowQuantityProducts();

    }

    private void getLowQuantityProducts() {
        progressBarLowQtyProducts.setVisibility(View.VISIBLE);
        lowQtyProductReference = FirebaseDatabase.getInstance().getReference("Products");
        lowQtyProductReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                StringBuilder lowQuantityProducts = new StringBuilder();

                for (DataSnapshot productSnapshot : dataSnapshot.getChildren()) {
                    String qtyString = productSnapshot.child("productQty").getValue(String.class);

                    try {
                        int qty = Integer.parseInt(qtyString);

                        if (qty < 5) {
                            String productName = productSnapshot.child("productName").getValue(String.class);
                            Log.d(TAG, "Low quantity product: " + productName);

                            lowQuantityProducts.append(productName).append("\n");
                        }
                    } catch (NumberFormatException e) {
                        Log.e(TAG, "Error parsing quantity to integer: " + qtyString);
                    }
                }

                textLowQtyProducts.setText(lowQuantityProducts.toString());

                progressBarLowQtyProducts.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, error.getMessage());
                progressBarLowQtyProducts.setVisibility(View.GONE);
            }
        });
    }


    private void getOrdersCount() {
        progressBarOrders.setVisibility(View.VISIBLE);

        orderReference = FirebaseDatabase.getInstance().getReference("Registered Users");

        orderReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int totalOrderCount = 0;

                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    DataSnapshot ordersSnapshot = userSnapshot.child("Orders");

                    int userOrderCount = (int) ordersSnapshot.getChildrenCount();

                    totalOrderCount += userOrderCount;
                }

                textOrderCount.setText(String.valueOf(totalOrderCount));

                progressBarOrders.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, error.getMessage());

                progressBarOrders.setVisibility(View.GONE);
            }
        });
    }


    private void getProductsCount() {
        progressBarProducts.setVisibility(View.VISIBLE);
        productReference = FirebaseDatabase.getInstance().getReference("Products");
        productReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int count = (int) dataSnapshot.getChildrenCount();
                textProductsCount.setText(String.valueOf(count));
                progressBarProducts.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, error.getMessage());
                progressBarProducts.setVisibility(View.GONE);
            }
        });
    }

    private void getAdminCount() {
        progressBarAdmins.setVisibility(View.VISIBLE);
        adminReference = FirebaseDatabase.getInstance().getReference("Registered Admins");
        adminReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int count = (int) dataSnapshot.getChildrenCount();
                textAdminCount.setText(String.valueOf(count));
                progressBarAdmins.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, error.getMessage());
                progressBarAdmins.setVisibility(View.GONE);
            }
        });
    }

    private void getUsersCount() {
        progressBarUsers.setVisibility(View.VISIBLE);
        userReference = FirebaseDatabase.getInstance().getReference("Registered Users");
        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int count = (int) dataSnapshot.getChildrenCount();
                textUserCount.setText(String.valueOf(count));
                progressBarUsers.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, error.getMessage());
                progressBarUsers.setVisibility(View.GONE);
            }
        });
    }
    private void setupNavigation() {
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
            bottomNavigationView.setSelectedItemId(R.id.home);
            bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    // Handle navigation item selection here
                    bottomNavigation(item.getItemId());
                    return true;
                }
            });

        }

    private void setupFirebase() {
        if(firebaseUser == null){
            Toast.makeText(HomeViewActivity.this, "Something Went Wrong! User's details are not available at the moment.", Toast.LENGTH_LONG).show();
        } else {
            checkIfEmailVerified(firebaseUser);
            showUserProfile(firebaseUser);
            Log.i("USER", firebaseUser.toString());
        }
    }

    private void setupViews() {
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        toolbar = findViewById(R.id.toolBar);

        View headerView = navigationView.getHeaderView(0);
        sideBarUserName = headerView.findViewById(R.id.sideBarUserName);
        sideBarUserEmail = headerView.findViewById(R.id.sideBarUserEmail);
        sideBarUserProfilePic = headerView.findViewById(R.id.sideBarUserProfilePic);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        textUserCount = findViewById(R.id.textViewUserCountDetails);
        textAdminCount = findViewById(R.id.textViewAdminCountDetails);
        textProductsCount = findViewById(R.id.textViewProductsCountDetails);
        textOrderCount = findViewById(R.id.textViewOrdersCountDetails);
        textLowQtyProducts = findViewById(R.id.textViewLowQtyProductsDetails);

        progressBarUsers = findViewById(R.id.progressBarUserCount);
        progressBarAdmins = findViewById(R.id.progressBarAdminCount);
        progressBarProducts = findViewById(R.id.progressBarProductCount);
        progressBarOrders = findViewById(R.id.progressBarOrderCount);
        progressBarLowQtyProducts = findViewById(R.id.progressBarLowQty);
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
                        //ImageViewer setImageURi() should not be used with regular URis. So we are using Picasso
                        Picasso.get().load(uri).into(sideBarUserProfilePic);
                    } else {
                        //ImageViewer setImageURi() should not be used with regular URis. So we are using Picasso
                        Picasso.get().load(R.drawable.no_profile_pic).into(sideBarUserProfilePic);
                    }

                } else {
                    Toast.makeText(HomeViewActivity.this, "Something Went Wrong!", Toast.LENGTH_LONG).show();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomeViewActivity.this, "Something Went Wrong!", Toast.LENGTH_LONG).show();
            }
        });
    }
    private void bottomNavigation(int itemId) {
        if (itemId == R.id.home) {
            // Handle the home menu item if needed
        } else if (itemId == R.id.products) {
            startActivity(new Intent(HomeViewActivity.this, ProductActivity.class));
            overridePendingTransition(0, 0);
        } else if (itemId == R.id.users) {
            startActivity(new Intent(HomeViewActivity.this, UsersActivity.class));
            overridePendingTransition(0, 0);
        } else if (itemId == R.id.orders) {
            startActivity(new Intent(HomeViewActivity.this, OrdersActivity.class));
            overridePendingTransition(0, 0);
        }else if (itemId == R.id.userprofile) {
            startActivity(new Intent(HomeViewActivity.this, UserProfileActivity.class));
            overridePendingTransition(0, 0);
        }
    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation item selection here
        if (item.getItemId() == R.id.sideNavHome) {
            // Handle the home menu item
        } else if (item.getItemId() == R.id.sideNavProducts) {
            startActivity(new Intent(HomeViewActivity.this, ProductActivity.class));
            overridePendingTransition(0, 0);
            bottomNavigationView.setSelectedItemId(R.id.products);
        } else if (item.getItemId() == R.id.sideNavUsers) {
            startActivity(new Intent(HomeViewActivity.this, UsersActivity.class));
            overridePendingTransition(0, 0);
            bottomNavigationView.setSelectedItemId(R.id.users);
        } else if (item.getItemId() == R.id.sideNavOrders) {
            startActivity(new Intent(HomeViewActivity.this, OrdersActivity.class));
            overridePendingTransition(0, 0);
            bottomNavigationView.setSelectedItemId(R.id.orders);
        }   else if (item.getItemId() == R.id.sideNavUserprofile) {
            startActivity(new Intent(HomeViewActivity.this, UserProfileActivity.class));
            overridePendingTransition(0, 0);
            bottomNavigationView.setSelectedItemId(R.id.userprofile);
        }

        // Close the drawer
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
    private void checkIfEmailVerified(FirebaseUser firebaseUser) {
        if(!firebaseUser.isEmailVerified()){
            showAlertDialog();
        }
    }
    private void showAlertDialog() {
        //Setup Alert Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(HomeViewActivity.this);
        builder.setTitle("Email Not Verified");
        builder.setMessage("Please verify your email now. You can not login without email verification next time.");

        //Open Email Apps if user click continue
        builder.setPositiveButton("Continue", (dialog, which) -> {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_APP_EMAIL);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);     //To email app in new window and not within our app
            startActivity(intent);
        });

        //Create the Alert Dialog
        AlertDialog alertDialog = builder.create();

        //Show the AlertDialog
        alertDialog.show();
    }

}
