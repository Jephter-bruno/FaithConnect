package com.glamour.faithconnect.adapter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.glamour.faithconnect.nativetemplates.NativeTemplateStyle;
import com.glamour.faithconnect.nativetemplates.TemplateView;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.glamour.faithconnect.GetTimeAgo;
import com.glamour.faithconnect.R;
import com.glamour.faithconnect.model.ModelNotification;
import com.glamour.faithconnect.post.CommentActivity;
import com.glamour.faithconnect.profile.UserProfileActivity;
import com.glamour.faithconnect.reel.ViewReelActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterNotification extends RecyclerView.Adapter<AdapterNotification.Holder>  {

    private final Context context;
    private final ArrayList<ModelNotification> notifications;
    private String userId;

    public AdapterNotification(Context context, ArrayList<ModelNotification> notifications) {
        this.context = context;
        this.notifications = notifications;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.notification_list, parent, false);
        return new Holder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        if ((position+1) % 6 == 0) {
            holder.ad.setVisibility(View.VISIBLE);
        }
        ModelNotification modelNotification = notifications.get(position);
        String notification = modelNotification.getNotification();
        String timestamp = modelNotification.getTimestamp();
        String senderUid = modelNotification.getsUid();
        String postId = modelNotification.getpId();

        String lastSeenTime = GetTimeAgo.getTimeAgo(Long.parseLong(timestamp));
        holder.username.setText(notification+ " - "+ lastSeenTime);

        FirebaseDatabase.getInstance().getReference().child("Users").child(senderUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (Objects.requireNonNull(snapshot.child("verified").getValue()).toString().equals("yes"))  holder.verified.setVisibility(View.VISIBLE);
                holder.name.setText(Objects.requireNonNull(snapshot.child("name").getValue()).toString());
                if (!Objects.requireNonNull(snapshot.child("photo").getValue()).toString().isEmpty())  Picasso.get().load(Objects.requireNonNull(snapshot.child("photo").getValue()).toString()).into(holder.circleImageView);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (!postId.isEmpty()){
                FirebaseDatabase.getInstance().getReference("Users").child(modelNotification.getpUid()).child("Notifications").child(timestamp).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.hasChild("type")){
                            Intent intent3 = new Intent(context, ViewReelActivity.class);
                            intent3.putExtra("id", postId);
                            context.startActivity(intent3);
                        }else {
                            Intent intent = new Intent(context, CommentActivity.class);
                            intent.putExtra("postID", postId);
                            context.startActivity(intent);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }else {
                Intent intent = new Intent(context, UserProfileActivity.class);
                intent.putExtra("hisUID", senderUid);
                context.startActivity(intent);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
         AlertDialog.Builder builder = new AlertDialog.Builder(context);
         builder.setTitle("Delete");
         builder.setMessage("Are you sure to delete this notification?");
         builder.setPositiveButton("Delete", (dialog, which) -> {
             DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
             ref.child(userId).child("Notifications").child(timestamp).getRef().removeValue();
             Snackbar.make(v, "Deleted", Snackbar.LENGTH_SHORT).show();
         }).setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
         builder.create().show();
         return false;
     });

    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    static class Holder extends RecyclerView.ViewHolder{

        final CircleImageView circleImageView;
        final TextView username;
        final TextView name;
        final ImageView verified;
        final RelativeLayout ad;
        public Holder(@NonNull View itemView) {
            super(itemView);
            circleImageView = itemView.findViewById(R.id.dp);
            username = itemView.findViewById(R.id.username);
            name = itemView.findViewById(R.id.name);
            verified = itemView.findViewById(R.id.verified);

            ad = itemView.findViewById(R.id.ad);
            MobileAds.initialize(itemView.getContext());
            AdLoader adLoader = new AdLoader.Builder(itemView.getContext(), itemView.getContext().getString(R.string.native_ad_unit_id))
                    .forNativeAd(new NativeAd.OnNativeAdLoadedListener() {
                        @Override
                        public void onNativeAdLoaded(NativeAd nativeAd) {
                            NativeTemplateStyle styles = new
                                    NativeTemplateStyle.Builder().build();
                            TemplateView template = itemView.findViewById(R.id.my_template);
                            template.setStyles(styles);
                            template.setNativeAd(nativeAd);
                        }
                    })
                    .build();

            adLoader.loadAd(new AdRequest.Builder().build());
        }
    }
}
