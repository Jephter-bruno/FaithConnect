package com.glamour.faithconnect.send;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.glamour.faithconnect.NightMode;
import com.glamour.faithconnect.R;
import com.glamour.faithconnect.adapter.AdapterSendUsers;
import com.glamour.faithconnect.model.ModelUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SendToUserActivity extends AppCompatActivity {

    private static String type;
    private static String uri;
    public static String getType() {
        return type;
    }
    public static String getUri() {
        return uri;
    }
    public SendToUserActivity(){

    }

    //User
    AdapterSendUsers adapterUsers;
    List<ModelUser> userList;
    RecyclerView users_rv;

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
        setContentView(R.layout.activity_create_chat);

        //Strings
         type =getIntent().getStringExtra("type");
         uri =getIntent().getStringExtra("uri");

        //User
        users_rv = findViewById(R.id.users);
        users_rv.setLayoutManager(new LinearLayoutManager(SendToUserActivity.this));
        userList = new ArrayList<>();
        getAllUsers();

        findViewById(R.id.back).setOnClickListener(v -> onBackPressed());

        EditText editText = findViewById(R.id.editText);
        editText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                filter(editText.getText().toString());
                return true;
            }
            return false;
        });

        
    }

    private void filter(final String query) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){

                    if (ds.hasChild("name")){
                        ModelUser modelUser = ds.getValue(ModelUser.class);
                        if (!Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid().equals(Objects.requireNonNull(modelUser).getId())){
                            if (modelUser.getName().toLowerCase().contains(query.toLowerCase()) ||
                                    modelUser.getUsername().toLowerCase().contains(query.toLowerCase())){
                                userList.add(modelUser);
                                findViewById(R.id.progressBar).setVisibility(View.GONE);
                            }
                        }
                    }

                    adapterUsers = new AdapterSendUsers(SendToUserActivity.this, userList);
                    adapterUsers.notifyDataSetChanged();
                    users_rv.setAdapter(adapterUsers);
                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                    if (adapterUsers.getItemCount() == 0){
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                        findViewById(R.id.users).setVisibility(View.GONE);
                        findViewById(R.id.nothing).setVisibility(View.VISIBLE);
                    }else {
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                        findViewById(R.id.users).setVisibility(View.VISIBLE);
                        findViewById(R.id.nothing).setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void getAllUsers() {
        FirebaseDatabase.getInstance().getReference("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){

                    if (ds.hasChild("name")){
                        ModelUser modelUser = ds.getValue(ModelUser.class);
                        if (!Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid().equals(Objects.requireNonNull(modelUser).getId())){
                            userList.add(modelUser);
                            findViewById(R.id.progressBar).setVisibility(View.GONE);
                        }
                    }
                    adapterUsers = new AdapterSendUsers(SendToUserActivity.this, userList);
                    users_rv.setAdapter(adapterUsers);

                    if (adapterUsers.getItemCount() == 0){
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                        findViewById(R.id.users).setVisibility(View.GONE);
                        findViewById(R.id.nothing).setVisibility(View.VISIBLE);
                    }else {
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                        findViewById(R.id.users).setVisibility(View.VISIBLE);
                        findViewById(R.id.nothing).setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}