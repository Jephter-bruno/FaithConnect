package com.glamour.faithconnect.Paging;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.glamour.faithconnect.model.ModelPost;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class PostViewModel extends ViewModel {
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Posts");
    private LiveData<PagedList<ModelPost>> postsLiveData;

    public PostViewModel() {
        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPageSize(20) // Number of items loaded at once from DataSource
                .build();

        postsLiveData = new LivePagedListBuilder<>(new FirebaseDataSourceFactory(databaseReference), config).build();
    }

    public LiveData<PagedList<ModelPost>> getPostsLiveData() {
        return postsLiveData;
    }
}
