package com.glamour.faithconnect.reel;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.glamour.faithconnect.NightMode;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.glamour.faithconnect.R;
import com.glamour.faithconnect.adapter.AdapterCommentReel;
import com.glamour.faithconnect.model.ModelCommentReel;
import com.glamour.faithconnect.model.ModelUser;
import com.glamour.faithconnect.notifications.Data;
import com.glamour.faithconnect.notifications.Sender;
import com.glamour.faithconnect.notifications.Token;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import timber.log.Timber;

@SuppressWarnings("ALL")
public class ReelCommentActivity extends AppCompatActivity {

    String position;
    String reelId;
    String type;

    //Comments
    List<ModelCommentReel> commentsList;
    AdapterCommentReel adapterComments;
    RecyclerView recyclerView;

    private RequestQueue requestQueue;
    private boolean notify = false;
    NightMode sharedPref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPref = new NightMode(this);
        if (sharedPref.loadNightModeState().equals("night")){
            setTheme(R.style.DarkTheme);
        }else if (sharedPref.loadNightModeState().equals("dim")){
            setTheme(R.style.DimTheme);
        }else setTheme(R.style.AppTheme);
        setContentView(R.layout.fragment_party_chat);

        requestQueue = Volley.newRequestQueue(ReelCommentActivity.this);

        recyclerView = findViewById(R.id.chat_rv);

        position = getIntent().getStringExtra("item");

        reelId = getIntent().getStringExtra("id");

        type = getIntent().getStringExtra("type");

        findViewById(R.id.imageView).setOnClickListener(v -> {
            if (type.equals("view")){

                Intent intent3 = new Intent(ReelCommentActivity.this, ViewReelActivity.class);
                intent3.putExtra("id", reelId);
                startActivity(intent3);
                finish();

            }else {
                Intent intent = new Intent(ReelCommentActivity.this, ReelActivity.class);
                intent.putExtra("position", position);
                intent.putExtra("type", type);
                startActivity(intent);
                finish();
            }
        });

        //Send
        EditText editText = findViewById(R.id.editText);
        findViewById(R.id.message_send).setOnClickListener(v -> {

            if (editText.getText().toString().isEmpty()){
                Snackbar.make(v, "Type a comment", Snackbar.LENGTH_SHORT).show();
            }else {
                String timeStamp = ""+System.currentTimeMillis();
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("cId",  timeStamp);
                hashMap.put("comment", editText.getText().toString());
                hashMap.put("timestamp",  timeStamp);
                hashMap.put("id", FirebaseAuth.getInstance().getCurrentUser().getUid());
                hashMap.put("pId", reelId);
                FirebaseDatabase.getInstance().getReference("Reels").child(reelId).child("Comment").child(timeStamp).setValue(hashMap);
                Snackbar.make(v, "Comment sent", Snackbar.LENGTH_SHORT).show();
                addToHisNotification(getIntent().getStringExtra("his"), "Commented on your reel");
                notify = true;
                FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ModelUser user = snapshot.getValue(ModelUser.class);
                        if (notify){
                            sendNotification(getIntent().getStringExtra("his"), Objects.requireNonNull(user).getName(), "Commented on your reel");
                        }
                        notify = false;
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
                editText.setText("");
            }

        });

        commentsList = new ArrayList<>();
        loadComments();

    }

    private void loadComments() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        commentsList = new ArrayList<>();
        FirebaseDatabase.getInstance().getReference("Reels").child(reelId).child("Comment").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                commentsList.clear();
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelCommentReel modelComments = ds.getValue(ModelCommentReel.class);
                    commentsList.add(modelComments);
                    adapterComments = new AdapterCommentReel(getApplicationContext(), commentsList);
                    recyclerView.setAdapter(adapterComments);
                    adapterComments.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (type.equals("view")){

            Intent intent3 = new Intent(ReelCommentActivity.this, ViewReelActivity.class);
            intent3.putExtra("id", reelId);
            startActivity(intent3);
            finish();

        }else {
            Intent intent = new Intent(ReelCommentActivity.this, ReelActivity.class);
            intent.putExtra("position", position);
            intent.putExtra("type", type);
            startActivity(intent);
            finish();
        }
    }

    private void addToHisNotification(String hisUid, String message){
        String timestamp = ""+System.currentTimeMillis();
        HashMap<Object, String> hashMap = new HashMap<>();
        hashMap.put("pId", reelId);
        hashMap.put("type", "reel");
        hashMap.put("timestamp", timestamp);
        hashMap.put("pUid", hisUid);
        hashMap.put("notification", message);
        hashMap.put("sUid", FirebaseAuth.getInstance().getCurrentUser().getUid());
        FirebaseDatabase.getInstance().getReference("Users").child(hisUid).child("Notifications").child(timestamp).setValue(hashMap);
        FirebaseDatabase.getInstance().getReference("Users").child(hisUid).child("Count").child(timestamp).setValue(true);
    }

    private void sendNotification(final String hisId, final String name,final String message){

       /* String username = getResources().getString(R.string.your_email);
        String password = getResources().getString(R.string.your_password);
        String messageToSend = message;
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        Session session = Session.getInstance(props,
                new javax.mail.Authenticator(){
            @Override
                    protected PasswordAuthentication getPasswordAuthentication(){
                return new PasswordAuthentication(username, password);
            }
        });

        FirebaseDatabase.getInstance().getReference().child("Users").child(hisId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.child("email").getValue().toString().isEmpty()){
                    String em = snapshot.child("email").getValue().toString();

                    try {
                        Message message1 = new MimeMessage(session);
                        message1.setFrom(new InternetAddress(username));
                        message1.setRecipients(Message.RecipientType.TO, InternetAddress.parse(em));
                        message1.setSubject("New Message - "+getResources().getString(R.string.app_name));
                        message1.setText(name + " " +messageToSend);
                        Transport.send(message1);
                    }catch (MessagingException e){
                        throw new RuntimeException(e);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        */

        DatabaseReference allToken = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = allToken.orderByKey().equalTo(hisId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    Token token = ds.getValue(Token.class);
                    Data data = new Data(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid(), name + " " + message, "New Comment", hisId, "profile", R.drawable.logo);
                    assert token != null;
                    Sender sender = new Sender(data, token.getToken());
                    try {
                        JSONObject jsonObject = new JSONObject(new Gson().toJson(sender));
                        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", jsonObject, response -> Timber.d("onResponse%s", response.toString()), error -> Timber.d("onResponse%s", error.toString())){
                            @Override
                            public Map<String, String> getHeaders() {
                                Map<String, String> headers = new HashMap<>();
                                headers.put("Content-Type", "application/json");
                                headers.put("Authorization", "key=AAAAfoG2x4A:APA91bFn9LOCNQXofZENtF0oLioSXaUHY3zkD_umFBnQhccXaEA7yWqUYZzylqd5_LhOYGmGuCrMmxVqKN0jpt9O9GIW19yTue6u-0f78sOXGtGYNpo3Dz7pjOa6cKellOQaOlPMp-nV");
                                return headers;
                            }
                        };
                        requestQueue.add(jsonObjectRequest);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

}