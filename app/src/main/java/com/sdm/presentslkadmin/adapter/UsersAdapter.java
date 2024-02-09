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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.sdm.presentslkadmin.R;
import com.sdm.presentslkadmin.model.Products;
import com.sdm.presentslkadmin.model.ReadWriteUserDetails;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {

    public static final String TAG = "UsersAdapter";
    private ArrayList<ReadWriteUserDetails> users;
    private FirebaseStorage storage;
    private Context context;

    public UsersAdapter(ArrayList<ReadWriteUserDetails> users, Context context) {
        this.users = users;
        this.context = context;
        this.storage = FirebaseStorage.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.layout_admin_users_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        ReadWriteUserDetails userDetails = users.get(position);

        String userId = userDetails.getKey();

        holder.textUserAddress.setText(formatText("Address : ", userDetails.getEmail()));
        holder.textUserDob.setText(formatText("DOB : ", userDetails.getDoB()));
        holder.textUserEmail.setText(formatText("Email : ", userDetails.getEmail()));
        holder.textUserId.setText(formatText("User ID : ", userDetails.getKey()));
        holder.textUserName.setText(formatText("", firebaseUser.getDisplayName()));
        holder.textUserMobile.setText(formatText("Mobile No : ", userDetails.getMobile()));



        String imagePath = "DisplayPics/" + userId;

        StorageReference storageReference = FirebaseStorage.getInstance().getReference(imagePath);

        storageReference.listAll()
                .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                    @Override
                    public void onSuccess(ListResult listResult) {
                        if (!listResult.getItems().isEmpty()) {
                            StorageReference firstItemReference = listResult.getItems().get(0);

                            Log.d(TAG, "Item name: " + firstItemReference.getName());
                            Log.d(TAG, "Item path: " + firstItemReference.getPath());

                            firstItemReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Picasso.get()
                                            .load(uri)
                                            .resize(200, 280)
                                            .centerCrop()
                                            .into(holder.userProfileImage);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    Log.e(TAG, "Error loading image: " + exception.getMessage());
                                    holder.userProfileImage.setImageResource(R.drawable.no_profile_pic);
                                }
                            });
                        } else {
                            holder.userProfileImage.setImageResource(R.drawable.no_profile_pic);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.e(TAG, "Error listing items in the directory: " + exception.getMessage());
                        holder.userProfileImage.setImageResource(R.drawable.no_profile_pic);
                    }
                });


    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    private SpannableString formatText(String prefix, String value) {
        String text = prefix + value;
        SpannableString spannableString = new SpannableString(text);
        spannableString.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, prefix.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        return spannableString;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView textUserName, textUserDob, textUserMobile, textUserAddress, textUserId, textUserEmail;
        ImageView userProfileImage;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textUserName = itemView.findViewById(R.id.textViewUserName_adminUser);
            textUserDob = itemView.findViewById(R.id.textViewUserDob_adminUser);
            textUserMobile = itemView.findViewById(R.id.textViewUserMobile_adminUser);
            textUserAddress = itemView.findViewById(R.id.textViewUserAddress_adminUser);
            textUserId = itemView.findViewById(R.id.textViewUserId_adminUser);
            textUserEmail = itemView.findViewById(R.id.textViewUserEmail_adminUser);
            userProfileImage = itemView.findViewById(R.id.imageViewUserImage_adminUser);
        }
    }
}
