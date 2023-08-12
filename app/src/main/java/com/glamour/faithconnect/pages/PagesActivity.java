package com.glamour.faithconnect.pages;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.glamour.faithconnect.MainActivity;
import com.glamour.faithconnect.NightMode;
import com.glamour.faithconnect.R;
import com.glamour.faithconnect.adapter.AdapterPages;
import com.glamour.faithconnect.adapter.AdapterPagesPost;
import com.glamour.faithconnect.model.ModelPages;
import com.glamour.faithconnect.model.ModelPostGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PagesActivity extends AppCompatActivity {

    RecyclerView post;
    AdapterPagesPost adapterPost;
    List<ModelPostGroup> modelPosts;
    List<String> followingList;

    AdapterPages adapterPages;
    List<ModelPages> modelPages;
    RecyclerView pages;

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
        setContentView(R.layout.activity_pages);

        findViewById(R.id.imageView).setOnClickListener(view -> startActivity(new Intent(this, MainActivity.class)));
        findViewById(R.id.create).setOnClickListener(view -> startActivity(new Intent(this, CreatePageActivity.class)));

        pages = findViewById(R.id.pages);
        pages.setLayoutManager(new LinearLayoutManager(PagesActivity.this));
        modelPages = new ArrayList<>();

        post = findViewById(R.id.products);
        post.setLayoutManager(new LinearLayoutManager(PagesActivity.this));
        modelPosts = new ArrayList<>();
        checkFollowing();

        //TabLayout
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tabLayout.getSelectedTabPosition() == 1) {
                    pages.setVisibility(View.VISIBLE);
                    post.setVisibility(View.GONE);
                    getAllPages();
                } else if (tabLayout.getSelectedTabPosition() == 0) {
                    pages.setVisibility(View.GONE);
                    post.setVisibility(View.VISIBLE);
                    checkFollowing();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }

    private void checkFollowing(){
        followingList = new ArrayList<>();
        FirebaseDatabase.getInstance().getReference("FollowPages").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                followingList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    followingList.add(snapshot.getKey());
                }

                getAllPost();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    private void getAllPages() {
        FirebaseDatabase.getInstance().getReference("Pages").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                modelPages.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelPages modelPagess = ds.getValue(ModelPages.class);
                    if (ds.child("id").getValue().toString().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                        modelPages.add(modelPagess);
                    }
                    adapterPages = new AdapterPages(PagesActivity.this, modelPages);
                    pages.setAdapter(adapterPages);

                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                    if (adapterPages.getItemCount() == 0){
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                        findViewById(R.id.pages).setVisibility(View.GONE);
                        findViewById(R.id.nothing).setVisibility(View.VISIBLE);
                    }else {
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                        findViewById(R.id.pages).setVisibility(View.VISIBLE);
                        findViewById(R.id.nothing).setVisibility(View.GONE);
                    }
                }

                if (!dataSnapshot.exists()){

                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                    findViewById(R.id.pages).setVisibility(View.GONE);
                    findViewById(R.id.nothing).setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void getAllPost() {
        FirebaseDatabase.getInstance().getReference("Pages").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    if(followingList.contains(ds.getKey())){

                        FirebaseDatabase.getInstance().getReference("Pages").child(Objects.requireNonNull(ds.getKey())).child("Posts").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                for (DataSnapshot ds: snapshot.getChildren()){
                                    ModelPostGroup modelPost = ds.getValue(ModelPostGroup.class);
                                    modelPosts.add(modelPost);
                                }

                                adapterPost = new AdapterPagesPost(PagesActivity.this, modelPosts);
                                post.setAdapter(adapterPost);
                                findViewById(R.id.progressBar).setVisibility(View.GONE);
                                if (adapterPost.getItemCount() == 0){
                                    post.setVisibility(View.GONE);

                                    findViewById(R.id.nothing).setVisibility(View.VISIBLE);
                                }else {

                                    post.setVisibility(View.VISIBLE);
                                    findViewById(R.id.nothing).setVisibility(View.GONE);
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}