package com.glamour.faithconnect.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.glamour.faithconnect.menu.MenuActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.glamour.faithconnect.adapter.AdapterLive;
import com.glamour.faithconnect.adapter.AdapterPodcast;
import com.glamour.faithconnect.adapter.AdapterPost;
import com.glamour.faithconnect.adapter.AdapterStory;
import com.glamour.faithconnect.group.GroupFragment;
import com.glamour.faithconnect.model.ModelLive;
import com.glamour.faithconnect.model.ModelPost;
import com.glamour.faithconnect.model.ModelStory;
import com.glamour.faithconnect.notifications.NotificationScreen;
import com.glamour.faithconnect.post.CreatePostActivity;
import com.glamour.faithconnect.R;
import com.glamour.faithconnect.search.TrendingActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeFragment extends Fragment {

    //Post
    AdapterPost postsAdapter;
    ArrayList<ModelPost> modelPosts;
    RecyclerView postsRecyclerView;

    //Live
    private AdapterLive live;
    private List<ModelLive> modelLives;
    RecyclerView liveView;

    //Pod
    private AdapterPodcast podcast;
    private List<ModelLive> modelLiveList;
    RecyclerView podView;

    //Story
    private AdapterStory adapterStory;
    private List<ModelStory> modelStories;
    RecyclerView storyView;

    //Follow
    List<String> followingList;

    //OtherId;
    ProgressBar progressBar;
    TextView nothing;
    NestedScrollView homeScrollview;
    ImageView bell;
    TextView count;
    CircleImageView circleImageView;
    TextView postInfo;

    private static final int TOTAL_ITEM_EACH_LOAD = 22;
    private int currentPage = 1;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private String lastPostKey = "";

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        //User
        circleImageView = v.findViewById(R.id.circleImageView);
        getCurrentUser();

        //Posts
        postsRecyclerView = v.findViewById(R.id.post);
        progressBar = v.findViewById(R.id.progressBar);
        nothing = v.findViewById(R.id.nothing);
        postInfo = v.findViewById(R.id.post_list_info);

        modelPosts = new ArrayList<>();

        getAllPosts(TOTAL_ITEM_EACH_LOAD, lastPostKey);

        checkFollowing();

        //PostIntent
        v.findViewById(R.id.create_post).setOnClickListener(v1 -> startActivity(new Intent(getActivity(), CreatePostActivity.class)));

        //Search
        v.findViewById(R.id.search).setOnClickListener(v1 -> startActivity(new Intent(getActivity(), TrendingActivity.class)));

        //Groups
        v.findViewById(R.id.add).setOnClickListener(v1 -> startActivity(new Intent(getActivity(), GroupFragment.class)));

        //Camera
        v.findViewById(R.id.camera).setOnClickListener(v1 -> startActivity(new Intent(getActivity(), MenuActivity.class)));

        //Notification
        bell = v.findViewById(R.id.bell);
        count = v.findViewById(R.id.count);

        handleNotifications();

        bell.setOnClickListener(v1 -> {
            startActivity(new Intent(getActivity(), NotificationScreen.class));
            bell.setVisibility(View.VISIBLE);
            count.setVisibility(View.GONE);
        });
        count.setOnClickListener(v1 -> {
            startActivity(new Intent(getActivity(), NotificationScreen.class));
            bell.setVisibility(View.VISIBLE);
            count.setVisibility(View.GONE);
        });

        //Live
        liveView = v.findViewById(R.id.live_list);
        LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(getContext(),
                LinearLayoutManager.HORIZONTAL, false);
        liveView.setLayoutManager(linearLayoutManager2);
        modelLives = new ArrayList<>();

        //Pod
        podView = v.findViewById(R.id.pod_list);
        LinearLayoutManager linearLayoutManager3 = new LinearLayoutManager(getContext(),
                LinearLayoutManager.HORIZONTAL, false);
        podView.setLayoutManager(linearLayoutManager3);
        modelLiveList = new ArrayList<>();

        //Story
        storyView = v.findViewById(R.id.story_list);
        LinearLayoutManager linearLayoutManager5 = new LinearLayoutManager(getContext(),
                LinearLayoutManager.HORIZONTAL, false);
        storyView.setLayoutManager(linearLayoutManager5);
        modelStories = new ArrayList<>();

        //Home ScrollView
        homeScrollview = v.findViewById(R.id.home_scrollview);
        homeScrollview.setOnTouchListener((v12, event) -> false);

        homeScrollview.getViewTreeObserver().addOnScrollChangedListener(() -> {
            View view = homeScrollview.getChildAt(homeScrollview.getChildCount() - 1);
            int bottomDetector = view.getBottom() - (homeScrollview.getHeight() + homeScrollview.getScrollY());
            if (bottomDetector == 0) {
                if (!isLastPage){
                    getAllPosts(TOTAL_ITEM_EACH_LOAD, lastPostKey);
                }

            }
        });

        return v;
    }

    private void handleNotifications() {
        FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .child("Count")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            bell.setVisibility(View.GONE);
                            count.setVisibility(View.VISIBLE);
                            count.setText(String.valueOf(snapshot.getChildrenCount()));
                        } else {
                            bell.setVisibility(View.VISIBLE);
                            count.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    private void getCurrentUser() {
        FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!Objects.requireNonNull(snapshot.child("photo").getValue()).toString().isEmpty()) {
                    Picasso.get().load(Objects.requireNonNull(snapshot.child("photo").getValue()).toString()).into(circleImageView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getAllPosts(int pageSize, String lastPostId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");

        if (lastPostId != null) {
            reference.startAt(lastPostId).limitToFirst(pageSize + 1);
        } else {
            reference.limitToFirst(pageSize);
        }

        reference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                modelPosts.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelPost modelPost = ds.getValue(ModelPost.class);
                    modelPosts.add(modelPost);
                    lastPostKey = modelPost.getId();
                }
                if (modelPosts.size() == snapshot.getChildrenCount()) {
                    isLastPage = true;
                    postInfo.setVisibility(View.VISIBLE);
                    postInfo.setText("Come back later for more posts, or create a new post!");
                }

                // Remove the last item if it was included for reference
                if (lastPostId != null) {
                    if (!modelPosts.isEmpty()) {
                        modelPosts.remove(modelPosts.size() - 1);
                    }
                }
                Collections.reverse(modelPosts);
                initializePostsAdapter(modelPosts);
                toggleProgressbarVisibility();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.println(error.getMessage());
            }
        });
    }

    private void initializePostsAdapter(ArrayList<ModelPost> posts) {
        postsAdapter = new AdapterPost(getActivity(), posts);
        postsRecyclerView.setAdapter(postsAdapter);
        postsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void toggleProgressbarVisibility() {
        if (postsAdapter.getItemCount() == 0) {
            progressBar.setVisibility(View.GONE);
            postsRecyclerView.setVisibility(View.GONE);
            nothing.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
            postsRecyclerView.setVisibility(View.VISIBLE);
            nothing.setVisibility(View.GONE);
        }
    }

    private void checkFollowing() {
        followingList = new ArrayList<>();
        FirebaseDatabase.getInstance().getReference("Follow")
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .child("1Following").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                followingList.clear();
                followingList.add(FirebaseAuth.getInstance().getCurrentUser().getUid());
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    followingList.add(snapshot.getKey());
                }
                readLive();
                readPod();
                readStory();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void readStory() {
        FirebaseDatabase.getInstance().getReference("Story").addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long timecurrent = System.currentTimeMillis();
                modelStories.clear();
                for (String id : followingList) {
                    int countStory = 0;
                    ModelStory modelStory = null;
                    for (DataSnapshot snapshot1 : snapshot.child(id).getChildren()) {
                        modelStory = snapshot1.getValue(ModelStory.class);
                        if (timecurrent > Objects.requireNonNull(modelStory).getTimestart() && timecurrent < modelStory.getTimeend()) {
                            countStory++;
                        }
                    }
                    if (countStory > 0) {
                        modelStories.add(modelStory);
                    }
                }
                adapterStory = new AdapterStory(getContext(), modelStories);
                storyView.setAdapter(adapterStory);
                adapterStory.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void readPod() {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Podcast");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                modelLiveList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    if (ds.hasChild("userid")) {
                        ModelLive modelLive = ds.getValue(ModelLive.class);
                        for (String id : followingList) {
                            if (!Objects.requireNonNull(firebaseUser).getUid().equals(Objects.requireNonNull(modelLive).getUserid()) && Objects.requireNonNull(modelLive).getUserid().equals(id)) {
                                modelLiveList.add(modelLive);
                            }
                        }
                    }
                    podcast = new AdapterPodcast(getActivity(), modelLiveList);
                    podView.setAdapter(podcast);
                    podcast.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void readLive() {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Live");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                modelLives.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    if (ds.hasChild("userid")) {
                        ModelLive modelLive = ds.getValue(ModelLive.class);
                        for (String id : followingList) {
                            if (!Objects.requireNonNull(firebaseUser).getUid().equals(Objects.requireNonNull(modelLive).getUserid()) && Objects.requireNonNull(modelLive).getUserid().equals(id)) {
                                modelLives.add(modelLive);
                            }
                        }
                    }
                    live = new AdapterLive(getActivity(), modelLives);
                    liveView.setAdapter(live);
                    live.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
}
