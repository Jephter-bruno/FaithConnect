package com.glamour.faithconnect.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
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
import com.glamour.faithconnect.group.GroupProfileActivity;
import com.glamour.faithconnect.model.ModelGroups;
import com.glamour.faithconnect.model.ModelUser;
import com.glamour.faithconnect.notifications.Data;
import com.glamour.faithconnect.notifications.Sender;
import com.glamour.faithconnect.notifications.Token;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import timber.log.Timber;

@SuppressWarnings("ALL")
public class AdapterWarnGroups extends RecyclerView.Adapter<AdapterWarnGroups.MyHolder>{

    final Context context;
    final List<ModelGroups> userList;

    private RequestQueue requestQueue;
    private boolean notify = false;

    public AdapterWarnGroups(Context context, List<ModelGroups> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view = LayoutInflater.from(context).inflate(R.layout.group_list, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        requestQueue = Volley.newRequestQueue(context);

        holder.name.setText(userList.get(position).getgName());

        holder.username.setText(userList.get(position).getgUsername());

        if (userList.get(position).getgIcon().isEmpty()){
            Picasso.get().load(R.drawable.group).into(holder.dp);
        }else {
            Picasso.get().load(userList.get(position).getgIcon()).into(holder.dp);
        }

        holder.itemView.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(context, v, Gravity.END);

            popupMenu.getMenu().add(Menu.NONE,1,0, "Send warning to group");
            popupMenu.getMenu().add(Menu.NONE,2,0, "Remove from warning");
            popupMenu.getMenu().add(Menu.NONE,3,0, "View group profile");
            popupMenu.getMenu().add(Menu.NONE,4,0, "Delete group");

            popupMenu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();

                if (id == 1) {
                    notify = true;
                    FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            ModelUser user = snapshot.getValue(ModelUser.class);
                            if (notify){
                                FirebaseDatabase.getInstance().getReference("Groups").child(userList.get(position).getGroupId()).child("Participants").addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for (DataSnapshot ds : snapshot.getChildren()){
                                            sendNotification(ds.getKey(), Objects.requireNonNull(user).getName(), "Your group got a warning by the admin");
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                            notify = false;
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
                    FirebaseDatabase.getInstance().getReference("warn").child("group").child(userList.get(position).getGroupId()).setValue(true);
                    Snackbar.make(v, "Warning sent", Snackbar.LENGTH_LONG).show();
                }

                if (id == 2) {
                    FirebaseDatabase.getInstance().getReference("warn").child("group").child(userList.get(position).getGroupId()).getRef().removeValue();
                    Snackbar.make(v, "Removed", Snackbar.LENGTH_LONG).show();
                    ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
                    params.height = 0;
                    holder.itemView.setLayoutParams(params);
                }

                if (id == 3) {
                    Intent intent = new Intent(context, GroupProfileActivity.class);
                    intent.putExtra("group", userList.get(position).getGroupId());
                    intent.putExtra("type", "");
                    context.startActivity(intent);
                }

                if (id == 4) {

                    FirebaseDatabase.getInstance().getReference().child("Groups").child(userList.get(position).getGroupId()).getRef().removeValue();
                    Snackbar.make(v, "Removed", Snackbar.LENGTH_LONG).show();
                    ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
                    params.height = 0;
                    holder.itemView.setLayoutParams(params);

                    notify = true;
                    FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            ModelUser user = snapshot.getValue(ModelUser.class);
                            if (notify){
                                FirebaseDatabase.getInstance().getReference("Groups").child(userList.get(position).getGroupId()).child("Participants").addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for (DataSnapshot ds : snapshot.getChildren()){
                                            sendNotification(ds.getKey(), Objects.requireNonNull(user).getName(), "Your group has been deleted");
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                            notify = false;
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
                }

                return false;
            });
            popupMenu.show();
        });


    }


    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class MyHolder extends RecyclerView.ViewHolder{

        final CircleImageView dp;
        final TextView name;
        final TextView username;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            dp = itemView.findViewById(R.id.dp);
            name = itemView.findViewById(R.id.name);
            username = itemView.findViewById(R.id.username);
        }

    }

    private void sendNotification(final String hisId, final String name,final String message){

        /*if (!FirebaseAuth.getInstance().getCurrentUser().getEmail().isEmpty()){
            String username = context.getResources().getString(R.string.your_email);
            String password = context.getResources().getString(R.string.your_password);
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
                        String name = snapshot.child("name").getValue().toString();

                        try {
                            Message message1 = new MimeMessage(session);
                            message1.setFrom(new InternetAddress(username));
                            message1.setRecipients(Message.RecipientType.TO, InternetAddress.parse(em));
                            message1.setSubject("New Message - "+ context.getResources().getString(R.string.app_name));
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

        }

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

         */

        DatabaseReference allToken = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = allToken.orderByKey().equalTo(hisId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    Token token = ds.getValue(Token.class);
                    Data data = new Data(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid(), name + " " + message, "Warning", hisId, "group", R.drawable.logo);
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
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

}
