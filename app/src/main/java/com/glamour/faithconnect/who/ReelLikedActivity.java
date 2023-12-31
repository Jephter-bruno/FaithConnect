package com.glamour.faithconnect.who;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.glamour.faithconnect.NightMode;
import com.glamour.faithconnect.R;
import com.glamour.faithconnect.adapter.AdapterUsers;
import com.glamour.faithconnect.model.ModelUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("ALL")
public class ReelLikedActivity extends AppCompatActivity {

    //User
    String reelId;
    private RecyclerView users_rv;
    private List<ModelUser> userList;
    private AdapterUsers adapterUsers;

    List<String> followingList;

    NightMode sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPref = new NightMode(this);
        if (sharedPref.loadNightModeState().equals("night")){
            setTheme(R.style.DarkTheme);
        }else if (sharedPref.loadNightModeState().equals("dim")){
            setTheme(R.style.DimTheme);
        }else setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_who);

        reelId = getIntent().getStringExtra("reelId");

        //Back
        findViewById(R.id.back).setOnClickListener(v -> onBackPressed());

        MobileAds.initialize(getApplicationContext(), initializationStatus -> {
        });
        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        FirebaseDatabase.getInstance().getReference("Ads").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (Objects.requireNonNull(snapshot.child("type").getValue()).toString().equals("on")){
                    mAdView.setVisibility(View.VISIBLE);
                }else {
                    mAdView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //User
        users_rv = findViewById(R.id.list);
        users_rv.setLayoutManager(new LinearLayoutManager(ReelLikedActivity.this));
        userList = new ArrayList<>();

        FirebaseDatabase.getInstance().getReference("ReelsLike").child(reelId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                followingList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    followingList.add(ds.getKey());
                }
                getAllUsers();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //EdiText
        EditText editText = findViewById(R.id.editText);
        editText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                filter(editText.getText().toString());

                return true;
            }
            return false;
        });

    }

    private void filter(String toString) {
        FirebaseDatabase.getInstance().getReference("Users")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        userList.clear();
                        for (DataSnapshot ds: snapshot.getChildren()){


                            if (ds.hasChild("name")){
                                ModelUser modelUser = ds.getValue(ModelUser.class);

                                for (String id : followingList){
                                    if (Objects.requireNonNull(modelUser).getId().equals(id)){
                                        if (Objects.requireNonNull(modelUser).getName().toLowerCase().contains(toString.toLowerCase()) ||
                                                modelUser.getUsername().toLowerCase().contains(toString.toLowerCase())){
                                            userList.add(modelUser);
                                        }
                                    }
                                }
                            }



                        }
                        adapterUsers = new AdapterUsers(ReelLikedActivity.this, userList);
                        users_rv.setAdapter(adapterUsers);
                        adapterUsers.notifyDataSetChanged();
                        if (adapterUsers.getItemCount() == 0){
                            findViewById(R.id.progressBar).setVisibility(View.GONE);
                            users_rv.setVisibility(View.GONE);
                            findViewById(R.id.nothing).setVisibility(View.VISIBLE);
                        }else {
                            findViewById(R.id.progressBar).setVisibility(View.GONE);
                            users_rv.setVisibility(View.VISIBLE);
                            findViewById(R.id.nothing).setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void getAllUsers() {
        FirebaseDatabase.getInstance().getReference("Users")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        userList.clear();
                        for (DataSnapshot ds: snapshot.getChildren()){

                            if (ds.hasChild("name")){
                                ModelUser modelUser = ds.getValue(ModelUser.class);

                                for (String id : followingList){
                                    if (Objects.requireNonNull(modelUser).getId().equals(id)){
                                        userList.add(modelUser);
                                    }
                                }
                            }


                        }
                        adapterUsers = new AdapterUsers(ReelLikedActivity.this, userList);
                        users_rv.setAdapter(adapterUsers);
                        if (adapterUsers.getItemCount() == 0){
                            findViewById(R.id.progressBar).setVisibility(View.GONE);
                            users_rv.setVisibility(View.GONE);
                            findViewById(R.id.nothing).setVisibility(View.VISIBLE);
                        }else {
                            findViewById(R.id.progressBar).setVisibility(View.GONE);
                            users_rv.setVisibility(View.VISIBLE);
                            findViewById(R.id.nothing).setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}