package com.glamour.faithconnect.Paging;

import androidx.annotation.NonNull;
import androidx.paging.DataSource;

import com.glamour.faithconnect.model.ModelPost;
import com.google.firebase.database.DatabaseReference;

public class FirebaseDataSourceFactory extends DataSource.Factory<Integer, ModelPost> {
    private DatabaseReference databaseReference;

    public FirebaseDataSourceFactory(DatabaseReference reference) {
        this.databaseReference = reference;
    }

    @NonNull
    @Override
    public DataSource<Integer, ModelPost> create() {
        return new FirebaseDataSource(databaseReference);
    }
}
