package com.glamour.faithconnect.adapter;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.facebook.ads.NativeAdLayout;
import com.facebook.ads.NativeBannerAdView;
import com.github.pgreze.reactions.ReactionPopup;
import com.github.pgreze.reactions.ReactionsConfig;
import com.github.pgreze.reactions.ReactionsConfigBuilder;
import com.glamour.faithconnect.facebookmetaads.MyNativeBannerAd;
import com.glamour.faithconnect.facebookmetaads.NativeBannerAds;
import com.glamour.faithconnect.nativetemplates.NativeTemplateStyle;
import com.glamour.faithconnect.nativetemplates.TemplateView;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.joooonho.SelectableRoundedImageView;
import com.nguyencse.URLEmbeddedData;
import com.nguyencse.URLEmbeddedView;
import com.glamour.faithconnect.GetTimeAgo;
import com.glamour.faithconnect.MediaViewActivity;
import com.glamour.faithconnect.R;
import com.glamour.faithconnect.model.ModelPostGroup;
import com.glamour.faithconnect.pages.CommentPagesActivity;
import com.glamour.faithconnect.pages.PagesProfileActivity;
import com.glamour.faithconnect.profile.UserProfileActivity;
import com.glamour.faithconnect.search.SearchActivity;
import com.glamour.faithconnect.who.LikedActivity;
import com.squareup.picasso.Picasso;
import com.tylersuehr.socialtextview.SocialTextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;
import me.jagar.chatvoiceplayerlibrary.VoicePlayerView;

@SuppressWarnings("ALL")
public class AdapterPagesPost extends RecyclerView.Adapter<AdapterPagesPost.MyHolder>{

    final Context context;
    final List<ModelPostGroup> modelPosts;
    String username_poster;

    private static final String TAG = "NativeAdActivity".getClass().getSimpleName();
    View containerView;

    public AdapterPagesPost(Context context, List<ModelPostGroup> modelPosts) {
        this.context = context;
        this.modelPosts = modelPosts;

    }
    MediaPlayer mp;

    public static List<String> extractUrls(String text)
    {
        List<String> containedUrls = new ArrayList<String>();
        String urlRegex = "((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
        Pattern pattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE);
        Matcher urlMatcher = pattern.matcher(text);

        while (urlMatcher.find())
        {
            containedUrls.add(text.substring(urlMatcher.start(0),
                    urlMatcher.end(0)));
        }

        return containedUrls;
    }


    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
/*
        containerView = LayoutInflater.from(context).inflate(R.layout.native_ad_container, parent, false);
*/

        View view = LayoutInflater.from(context).inflate(R.layout.post_list, parent, false);
        return new MyHolder(view);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        mp = MediaPlayer.create(context, R.raw.like);
        if (position>1 && (position+1) % 6 == 0) {
            holder.ad.setVisibility(View.VISIBLE);
        }


        else if (position>1 && (position+1) % 4 == 0) {
            holder.nativeAdLayout.removeAllViews();
            MyNativeBannerAd nativeBannerAd = new MyNativeBannerAd((Activity) context);
            nativeBannerAd.loadNativeBannerAd(
                    holder.nativeAdLayout,
                    NativeBannerAdView.Type.HEIGHT_120,
                    true,
                    "102713349600103_116057628265675"
            );
        }
        else if (position>1 && (position+1) % 10 == 0) {
            holder.nativeAdLayout.removeAllViews();
            NativeBannerAds myNativeAdss = new NativeBannerAds((Activity) context);
            myNativeAdss.loadNativeAd(holder.nativeAdLayout, false, "CAROUSEL_IMG_SQUARE_APP_INSTALL#102713349600103_116050314933073");

        }
        else if (position>1 && (position+1) % 13 == 0) {
            holder.nativeAdLayout.removeAllViews();
            NativeBannerAds myNativeAdsss = new NativeBannerAds((Activity) context);
            myNativeAdsss.loadNativeAd(holder.nativeAdLayout, false, "PLAYABLE#102713349600103_102714542933317");

        }



        FirebaseDatabase.getInstance().getReference().child("Pages").child(modelPosts.get(position).getGroup()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){

                    String mDp = Objects.requireNonNull(snapshot.child("photo").getValue()).toString();
                    if (!mDp.isEmpty()) Picasso.get().load(mDp).into(holder.dp);

                    //Name
                    holder.name.setText(Objects.requireNonNull(snapshot.child("name").getValue()).toString());
                    holder.username.setText(Objects.requireNonNull(snapshot.child("username").getValue()).toString());

                    //SetOnClick
                    holder.dp.setOnClickListener(v -> {
                        Intent intent = new Intent(context, PagesProfileActivity.class);
                        intent.putExtra("id", modelPosts.get(position).getGroup());
                        context.startActivity(intent);
                    });
                    holder.name.setOnClickListener(v -> {
                        Intent intent = new Intent(context, PagesProfileActivity.class);
                        intent.putExtra("id", modelPosts.get(position).getGroup());
                        context.startActivity(intent);
                    });

                    holder.username.setOnClickListener(v -> {
                        Intent intent = new Intent(context, PagesProfileActivity.class);
                        intent.putExtra("id", modelPosts.get(position).getGroup());
                        context.startActivity(intent);
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

      
        String type = modelPosts.get(position).getType();

        holder.urlEmbeddedView.setOnClickListener(v -> {

            List<String> extractedUrls = extractUrls(modelPosts.get(position).getText());

            for (String s : extractedUrls)
            {
                if (!s.startsWith("https://") && !s.startsWith("http://")){
                    s = "http://" + s;
                }
                Intent openUrlIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(s));
                context.startActivity(openUrlIntent);
            }


        });

        if (!modelPosts.get(position).getText().isEmpty()) {

            List<String> extractedUrls = extractUrls(modelPosts.get(position).getText());

            for (String url : extractedUrls)
            {
                holder.urlEmbeddedView.setVisibility(View.VISIBLE);

                holder.urlEmbeddedView.setURL(url, new URLEmbeddedView.OnLoadURLListener() {
                    @Override
                    public void onLoadURLCompleted(URLEmbeddedData data) {
                        holder.urlEmbeddedView.title(data.getTitle());
                        holder.urlEmbeddedView.description(data.getDescription());
                        holder.urlEmbeddedView.host(data.getHost());
                        holder.urlEmbeddedView.thumbnail(data.getThumbnailURL());
                        holder.urlEmbeddedView.favor(data.getFavorURL());
                    }
                });
            }

        }
        
        //Time
        long lastTime = Long.parseLong(modelPosts.get(position).getpTime());
        holder.time.setText(GetTimeAgo.getTimeAgo(lastTime));

        //Extra
        FirebaseDatabase.getInstance().getReference("postExtra").child(modelPosts.get(position).getpId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){

                    if (!snapshot.child("location").getValue().toString().isEmpty()) holder.location.setText(" . " + snapshot.child("location").getValue().toString());

                    holder.location.setOnClickListener(v -> {
                        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.co.in/maps?q=" + snapshot.child("location").getValue().toString()));
                        context.startActivity(i);
                    });

                    if (!snapshot.child("feeling").getValue().toString().isEmpty()) holder.feeling.setText(" - " + snapshot.child("feeling").getValue().toString());

                    if(!snapshot.child("feeling").getValue().toString().isEmpty()){
                        String mFeeling = snapshot.child("feeling").getValue().toString();
                        if (mFeeling.contains("Traveling")){
                            holder.activity.setImageResource(R.drawable.airplane);
                        }else if (mFeeling.contains("Watching")){
                            holder.activity.setImageResource(R.drawable.watching);
                        }else if (mFeeling.contains("Listening")){
                            holder.activity.setImageResource(R.drawable.listening);
                        }else if (mFeeling.contains("Thinking")){
                            holder.activity.setImageResource(R.drawable.thinking);
                        }else if (mFeeling.contains("Celebrating")){
                            holder.activity.setImageResource(R.drawable.celebration);
                        }else if (mFeeling.contains("Looking")){
                            holder.activity.setImageResource(R.drawable.looking);
                        }else if (mFeeling.contains("Playing")){
                            holder.activity.setImageResource(R.drawable.playing);
                        }else if (mFeeling.contains("happy")){
                            holder.activity.setImageResource(R.drawable.smiling);
                        } else if (mFeeling.contains("loved")){
                            holder.activity.setImageResource(R.drawable.love);
                        } else if (mFeeling.contains("sad")){
                            holder.activity.setImageResource(R.drawable.sad);
                        }else if (mFeeling.contains("crying")){
                            holder.activity.setImageResource(R.drawable.crying);
                        }else if (mFeeling.contains("angry")){
                            holder.activity.setImageResource(R.drawable.angry);
                        }else if (mFeeling.contains("confused")){
                            holder.activity.setImageResource(R.drawable.confused);
                        }else if (mFeeling.contains("broken")){
                            holder.activity.setImageResource(R.drawable.broken);
                        }else if (mFeeling.contains("cool")){
                            holder.activity.setImageResource(R.drawable.cool);
                        }else if (mFeeling.contains("funny")){
                            holder.activity.setImageResource(R.drawable.joy);
                        }else if (mFeeling.contains("tired")){
                            holder.activity.setImageResource(R.drawable.tired);
                        }else if (mFeeling.contains("shock")){
                            holder.activity.setImageResource(R.drawable.shocked);
                        }else if (mFeeling.contains("love")){
                            holder.activity.setImageResource(R.drawable.heart);
                        }else if (mFeeling.contains("sleepy")){
                            holder.activity.setImageResource(R.drawable.sleeping);
                        }else if (mFeeling.contains("expressionless")){
                            holder.activity.setImageResource(R.drawable.muted);
                        }else if (mFeeling.contains("blessed")){
                            holder.activity.setImageResource(R.drawable.angel);
                        }
                    }

                    if (snapshot.child("privacy").getValue().toString().equals("No one")){
                        if (!modelPosts.get(position).getId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                            ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
                            params.height = 0;
                            holder.itemView.setLayoutParams(params);
                        }

                    }else if (snapshot.child("privacy").getValue().toString().equals("Followers")){
                        FirebaseDatabase.getInstance().getReference().child("Follow").child(modelPosts.get(position).getId())
                                .child("Followers").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (!snapshot.hasChild(FirebaseAuth.getInstance().getCurrentUser().getUid()) &&  !modelPosts.get(position).getId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                                    ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
                                    params.height = 0;
                                    holder.itemView.setLayoutParams(params);
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
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //PostDetails
        if (!type.equals("bg")){
            holder.text.setLinkText(modelPosts.get(position).getText());
            holder.text.setOnLinkClickListener((i, s) -> {
                if (i == 1){

                    Intent intent = new Intent(context, SearchActivity.class);
                    intent.putExtra("hashtag", s);
                    context.startActivity(intent);

                }else
                if (i == 2){
                    String username = s.replaceFirst("@","");
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                    Query query = ref.orderByChild("username").equalTo(username.trim());
                    query.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                for (DataSnapshot ds : snapshot.getChildren()){
                                    String id = ds.child("id").getValue().toString();
                                    if (id.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                                        Snackbar.make(holder.itemView,"It's you", Snackbar.LENGTH_LONG).show();
                                    }else {
                                        Intent intent = new Intent(context, UserProfileActivity.class);
                                        intent.putExtra("hisUID", id);
                                        context.startActivity(intent);
                                    }
                                }
                            }else {
                                Snackbar.make(holder.itemView,"Invalid username, can't find user with this username", Snackbar.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Snackbar.make(holder.itemView,error.getMessage(), Snackbar.LENGTH_LONG).show();
                        }
                    });
                }
                else if (i == 16){
                    if (!s.startsWith("https://") && !s.startsWith("http://")){
                        s = "http://" + s;
                    }
                    Intent openUrlIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(s));
                    context.startActivity(openUrlIntent);
                }else if (i == 4){
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", s, null));
                    context.startActivity(intent);
                }else if (i == 8){
                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                    intent.setData(Uri.parse("mailto:"));
                    intent.putExtra(Intent.EXTRA_EMAIL, s);
                    intent.putExtra(Intent.EXTRA_SUBJECT, "");
                    context.startActivity(intent);

                }
            });
        }
if(type.equals("text")){
    holder.mediaView.setVisibility(View.GONE);
}
        if (type.equals("image")){
            holder.media_layout.setVisibility(View.VISIBLE);
            holder.mediaView.setVisibility(View.VISIBLE);
            Glide.with(context).asBitmap().load(modelPosts.get(position).getMeme()).fitCenter().into(holder.mediaView);
        }
        if (type.equals("gif")){
            holder.media_layout.setVisibility(View.VISIBLE);
            holder.mediaView.setVisibility(View.VISIBLE);
            Glide.with(context).load(modelPosts.get(position).getMeme()).thumbnail(0.1f).into(holder.mediaView);
        }
        if (type.equals("video")){
            holder.mediaView.setVisibility(View.VISIBLE);
            holder.media_layout.setVisibility(View.VISIBLE);
            holder.play.setVisibility(View.VISIBLE);
            Glide.with(context).asBitmap().load(modelPosts.get(position).getVine()).thumbnail(0.1f).into(holder.mediaView);
        }
        if (type.equals("bg")){
            holder.media_layout.setVisibility(View.VISIBLE);
            Picasso.get().load(modelPosts.get(position).getMeme()).into(holder.mediaView);
            holder.bg_text.setLinkText(modelPosts.get(position).getText());
            holder.bg_text.setOnLinkClickListener((i, s) -> {
                if (i == 1){

                    Intent intent = new Intent(context, SearchActivity.class);
                    intent.putExtra("hashtag", s);
                    context.startActivity(intent);

                }else
                if (i == 2){
                    String username = s.replaceFirst("@","");
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                    Query query = ref.orderByChild("username").equalTo(username.trim());
                    query.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                for (DataSnapshot ds : snapshot.getChildren()){
                                    String id = ds.child("id").getValue().toString();
                                    if (id.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                                        Snackbar.make(holder.itemView,"It's you", Snackbar.LENGTH_LONG).show();
                                    }else {
                                        Intent intent = new Intent(context, UserProfileActivity.class);
                                        intent.putExtra("hisUID", id);
                                        context.startActivity(intent);
                                    }
                                }
                            }else {
                                Snackbar.make(holder.itemView,"Invalid username, can't find user with this username", Snackbar.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Snackbar.make(holder.itemView,error.getMessage(), Snackbar.LENGTH_LONG).show();
                        }
                    });
                }
                else if (i == 16){
                    if (!s.startsWith("https://") && !s.startsWith("http://")){
                        s = "http://" + s;
                    }
                    Intent openUrlIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(s));
                    context.startActivity(openUrlIntent);
                }else if (i == 4){
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", s, null));
                    context.startActivity(intent);
                }else if (i == 8){
                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                    intent.setData(Uri.parse("mailto:"));
                    intent.putExtra(Intent.EXTRA_EMAIL, s);
                    intent.putExtra(Intent.EXTRA_SUBJECT, "");
                    context.startActivity(intent);

                }
            });
            holder.bg_text.setVisibility(View.VISIBLE);
            holder.text.setVisibility(View.GONE);
            holder.mediaView.setVisibility(View.VISIBLE);
        }
        if (type.equals("audio")){
            holder.mediaView.setVisibility(View.GONE);
            holder.voicePlayerView.setVisibility(View.VISIBLE);
            holder.voicePlayerView.setAudio(String.valueOf(modelPosts.get(position).getMeme()));
        }

        //CheckComments
        FirebaseDatabase.getInstance().getReference("Pages").child(modelPosts.get(position).getGroup()).child("Posts").child(modelPosts.get(position).getpId()).child("Comments").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    holder.layout.setVisibility(View.VISIBLE);
                    holder.commentLayout.setVisibility(View.VISIBLE);
                    holder.noComments.setText(String.valueOf(snapshot.getChildrenCount()));
                }else {
                    holder.commentLayout.setVisibility(View.GONE);
                    holder.noComments.setText("");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //CheckViews
        FirebaseDatabase.getInstance().getReference().child("Views").child(modelPosts.get(position).getpId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    holder.layout.setVisibility(View.VISIBLE);
                    holder.viewsLayout.setVisibility(View.VISIBLE);
                    holder.noViews.setText(String.valueOf(snapshot.getChildrenCount()));
                }else {
                    holder.viewsLayout.setVisibility(View.GONE);
                    holder.noViews.setText("");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //CheckLikes
        FirebaseDatabase.getInstance().getReference().child("Likes").child(modelPosts.get(position).getpId()).addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    holder.likeLayout.setVisibility(View.VISIBLE);
                    holder.line.setVisibility(View.VISIBLE);
                    holder.noLikes.setText(String.valueOf(snapshot.getChildrenCount()));
                    if (snapshot.hasChild(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                        //CheckNew
                        FirebaseDatabase.getInstance().getReference().child("Reaction").child(modelPosts.get(position).getpId()).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()){

                                    String react = snapshot.child("type").getValue().toString();
                                    if (react.equals("like")){
                                        holder.like_img.setImageResource(R.drawable.ic_thumb);
                                        holder.like_text.setText("Like");
                                    }
                                    if (react.equals("love")){
                                        holder.like_img.setImageResource(R.drawable.ic_love);
                                        holder.like_text.setText("Love");
                                    }
                                    if (react.equals("laugh")){
                                        holder.like_img.setImageResource(R.drawable.ic_laugh);
                                        holder.like_text.setText("Haha");
                                    }
                                    if (react.equals("wow")){
                                        holder.like_img.setImageResource(R.drawable.ic_wow);
                                        holder.like_text.setText("Wow");
                                    }
                                    if (react.equals("sad")){
                                        holder.like_img.setImageResource(R.drawable.ic_sad);
                                        holder.like_text.setText("Sad");
                                    }
                                    if (react.equals("angry")){
                                        holder.like_img.setImageResource(R.drawable.ic_angry);
                                        holder.like_text.setText("Angry");
                                    }

                                }else {
                                    FirebaseDatabase.getInstance().getReference().child("Likes").child(modelPosts.get(position).getpId()).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.exists()){
                                                holder.like_img.setImageResource(R.drawable.ic_thumb);
                                                holder.like_text.setText("Like");
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }else if (!snapshot.hasChild(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                        holder.like_img.setImageResource(R.drawable.ic_like);
                        holder.like_text.setText("Like");
                    }
                    //QuickShow
                    FirebaseDatabase.getInstance().getReference().child("Reaction").child(modelPosts.get(position).getpId()).orderByChild("type").equalTo("like").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.getChildrenCount()>0) {
                                holder.thumb.setVisibility(View.VISIBLE);
                            }else {
                                FirebaseDatabase.getInstance().getReference().child("Likes").child(modelPosts.get(position).getpId()).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()){
                                            holder.thumb.setVisibility(View.VISIBLE);
                                        }else {
                                            holder.thumb.setVisibility(View.GONE);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                    FirebaseDatabase.getInstance().getReference().child("Reaction").child(modelPosts.get(position).getpId()).orderByChild("type").equalTo("love").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.getChildrenCount()>0){
                                holder.love.setVisibility(View.VISIBLE);
                            }else {
                                holder.love.setVisibility(View.GONE);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                    FirebaseDatabase.getInstance().getReference().child("Reaction").child(modelPosts.get(position).getpId()).orderByChild("type").equalTo("wow").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.getChildrenCount()>0){
                                holder.wow.setVisibility(View.VISIBLE);
                            }else {
                                holder.wow.setVisibility(View.GONE);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                    FirebaseDatabase.getInstance().getReference().child("Reaction").child(modelPosts.get(position).getpId()).orderByChild("type").equalTo("angry").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.getChildrenCount()>0){
                                holder.angry.setVisibility(View.VISIBLE);
                            }else {
                                holder.angry.setVisibility(View.GONE);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                    FirebaseDatabase.getInstance().getReference().child("Reaction").child(modelPosts.get(position).getpId()).orderByChild("type").equalTo("laugh").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.getChildrenCount()>0){
                                holder.laugh.setVisibility(View.VISIBLE);
                            }else {
                                holder.laugh.setVisibility(View.GONE);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                    FirebaseDatabase.getInstance().getReference().child("Reaction").child(modelPosts.get(position).getpId()).orderByChild("type").equalTo("sad").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.getChildrenCount()>0){
                                holder.sad.setVisibility(View.VISIBLE);
                            }else {
                                holder.sad.setVisibility(View.GONE);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }else {
                    holder.likeLayout.setVisibility(View.GONE);
                    holder.line.setVisibility(View.GONE);
                    holder.like_img.setImageResource(R.drawable.ic_like);
                    holder.like_text.setText("Like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //Like
        ReactionsConfig config = new ReactionsConfigBuilder(context)
                .withReactions(new int[]{
                        R.drawable.ic_thumb,
                        R.drawable.ic_love,
                        R.drawable.ic_laugh,
                        R.drawable.ic_wow,
                        R.drawable.ic_sad,
                        R.drawable.ic_angry
                })
                .withPopupAlpha(1)
                .build();

        ReactionPopup popup = new ReactionPopup(context, config, (position1) -> {

            mp.start();

            if (position1 == 0) {
                FirebaseDatabase.getInstance().getReference().child("Likes").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child(modelPosts.get(position).getpId()).hasChild(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                            FirebaseDatabase.getInstance().getReference().child("Likes").child(modelPosts.get(position).getpId()).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).getRef().removeValue();
                            FirebaseDatabase.getInstance().getReference().child("Reaction").child(modelPosts.get(position).getpId()).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()){
                                        snapshot.getRef().removeValue();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        } else {
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("type", "like");
                            FirebaseDatabase.getInstance().getReference().child("Reaction").child(modelPosts.get(position).getpId()).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).getRef().setValue(hashMap);
                            FirebaseDatabase.getInstance().getReference().child("Likes").child(modelPosts.get(position).getpId()).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(true);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                return true;
            }else if (position1 == 1) {
                FirebaseDatabase.getInstance().getReference().child("Likes").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child(modelPosts.get(position).getpId()).hasChild(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                            FirebaseDatabase.getInstance().getReference().child("Likes").child(modelPosts.get(position).getpId()).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).getRef().removeValue();
                            FirebaseDatabase.getInstance().getReference().child("Reaction").child(modelPosts.get(position).getpId()).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        snapshot.getRef().removeValue();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        } else {
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("type", "love");
                            FirebaseDatabase.getInstance().getReference().child("Reaction").child(modelPosts.get(position).getpId()).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).getRef().setValue(hashMap);
                            FirebaseDatabase.getInstance().getReference().child("Likes").child(modelPosts.get(position).getpId()).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(true);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                return true;
            }
            else if (position1 == 2) {
                FirebaseDatabase.getInstance().getReference().child("Likes").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child(modelPosts.get(position).getpId()).hasChild(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                            FirebaseDatabase.getInstance().getReference().child("Likes").child(modelPosts.get(position).getpId()).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).getRef().removeValue();
                            FirebaseDatabase.getInstance().getReference().child("Reaction").child(modelPosts.get(position).getpId()).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        snapshot.getRef().removeValue();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        } else {
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("type", "laugh");
                            FirebaseDatabase.getInstance().getReference().child("Reaction").child(modelPosts.get(position).getpId()).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).getRef().setValue(hashMap);
                            FirebaseDatabase.getInstance().getReference().child("Likes").child(modelPosts.get(position).getpId()).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(true);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                return true;
            }      else if (position1 == 3) {
                FirebaseDatabase.getInstance().getReference().child("Likes").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child(modelPosts.get(position).getpId()).hasChild(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                            FirebaseDatabase.getInstance().getReference().child("Likes").child(modelPosts.get(position).getpId()).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).getRef().removeValue();
                            FirebaseDatabase.getInstance().getReference().child("Reaction").child(modelPosts.get(position).getpId()).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        snapshot.getRef().removeValue();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        } else {
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("type", "wow");
                            FirebaseDatabase.getInstance().getReference().child("Reaction").child(modelPosts.get(position).getpId()).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).getRef().setValue(hashMap);
                            FirebaseDatabase.getInstance().getReference().child("Likes").child(modelPosts.get(position).getpId()).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(true);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                return true;
            }
            else if (position1 == 4) {
                FirebaseDatabase.getInstance().getReference().child("Likes").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child(modelPosts.get(position).getpId()).hasChild(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                            FirebaseDatabase.getInstance().getReference().child("Likes").child(modelPosts.get(position).getpId()).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).getRef().removeValue();
                            FirebaseDatabase.getInstance().getReference().child("Reaction").child(modelPosts.get(position).getpId()).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        snapshot.getRef().removeValue();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        } else {
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("type", "sad");
                            FirebaseDatabase.getInstance().getReference().child("Reaction").child(modelPosts.get(position).getpId()).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).getRef().setValue(hashMap);
                            FirebaseDatabase.getInstance().getReference().child("Likes").child(modelPosts.get(position).getpId()).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(true);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                return true;
            }
            else if (position1 == 5) {
                FirebaseDatabase.getInstance().getReference().child("Likes").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child(modelPosts.get(position).getpId()).hasChild(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                            FirebaseDatabase.getInstance().getReference().child("Likes").child(modelPosts.get(position).getpId()).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).getRef().removeValue();
                            FirebaseDatabase.getInstance().getReference().child("Reaction").child(modelPosts.get(position).getpId()).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        snapshot.getRef().removeValue();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        } else {
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("type", "angry");
                            FirebaseDatabase.getInstance().getReference().child("Reaction").child(modelPosts.get(position).getpId()).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).getRef().setValue(hashMap);
                            FirebaseDatabase.getInstance().getReference().child("Likes").child(modelPosts.get(position).getpId()).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(true);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                return true;
            }

            return true;
        });

        //LikeFunctions
        holder.likeButtonTwo.setOnTouchListener(popup);
        FirebaseDatabase.getInstance().getReference().child("Likes").child(modelPosts.get(position).getpId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
             if (snapshot.exists()){
                 if (snapshot.hasChild(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                     holder.likeButtonTwo.setVisibility(View.GONE);
                     holder.likeButton.setVisibility(View.VISIBLE);
                 }else {
                     holder.likeButton.setVisibility(View.GONE);
                     holder.likeButtonTwo.setVisibility(View.VISIBLE);
                 }
             }else {
                 holder.likeButton.setVisibility(View.GONE);
                 holder.likeButtonTwo.setVisibility(View.VISIBLE);
             }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        holder.likeButton.setOnClickListener(v -> FirebaseDatabase.getInstance().getReference().child("Likes").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(modelPosts.get(position).getpId()).hasChild(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                    FirebaseDatabase.getInstance().getReference().child("Likes").child(modelPosts.get(position).getpId()).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).getRef().removeValue();
                    FirebaseDatabase.getInstance().getReference().child("Reaction").child(modelPosts.get(position).getpId()).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                snapshot.getRef().removeValue();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        }));

        //Share
        holder.share.setOnClickListener(v -> {
            if (type.equals("text")){
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/*");
                intent.putExtra(Intent.EXTRA_SUBJECT,"Subject Here");
                intent.putExtra(Intent.EXTRA_TEXT, modelPosts.get(position).getText());
                context.startActivity(Intent.createChooser(intent, "Share Via"));
            }else if (type.equals("image")){
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/*");
                intent.putExtra(Intent.EXTRA_SUBJECT,"Subject Here");
                intent.putExtra(Intent.EXTRA_TEXT, modelPosts.get(position).getText() + " " + modelPosts.get(position).getMeme());
                context.startActivity(Intent.createChooser(intent, "Share Via"));
            }else if (type.equals("audio")){
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/*");
                intent.putExtra(Intent.EXTRA_SUBJECT,"Subject Here");
                intent.putExtra(Intent.EXTRA_TEXT, modelPosts.get(position).getText() + " " + modelPosts.get(position).getMeme());
                context.startActivity(Intent.createChooser(intent, "Share Via"));
            }else if (type.equals("gif")){
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/*");
                intent.putExtra(Intent.EXTRA_SUBJECT,"Subject Here");
                intent.putExtra(Intent.EXTRA_TEXT, modelPosts.get(position).getText() + " " + modelPosts.get(position).getMeme());
                context.startActivity(Intent.createChooser(intent, "Share Via"));
            }else if (type.equals("video")){
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/*");
                intent.putExtra(Intent.EXTRA_SUBJECT,"Subject Here");
                intent.putExtra(Intent.EXTRA_TEXT, modelPosts.get(position).getText() + " " + modelPosts.get(position).getVine());
                context.startActivity(Intent.createChooser(intent, "Share Via"));
            }else {
                Snackbar.make(holder.itemView,"This type of post can't be shared", Snackbar.LENGTH_LONG).show();
            }
        });

        //More
        Context moreWrapper = new ContextThemeWrapper(context, R.style.popupMenuStyle);
        PopupMenu morePop = new PopupMenu(moreWrapper, holder.more);
        morePop.getMenu().add(Menu.NONE,1,1, "Save");
        morePop.getMenu().add(Menu.NONE,2,2, "Download");
        morePop.getMenu().add(Menu.NONE,4,4, "Copy");
        morePop.getMenu().add(Menu.NONE,9,9, "Liked by");
        if (modelPosts.get(position).getId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
            morePop.getMenu().add(Menu.NONE,7,7, "Delete");
        }
        if (modelPosts.get(position).getType().equals("image") || modelPosts.get(position).getType().equals("video")){
            morePop.getMenu().add(Menu.NONE,8,8, "Fullscreen");
        }
        morePop.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 9){
                Intent intent = new Intent(context, LikedActivity.class);
                intent.putExtra("id", modelPosts.get(position).getpId());
                context.startActivity(intent);
            }

            if (item.getItemId() == 1){
                FirebaseDatabase.getInstance().getReference().child("Saves").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(modelPosts.get(position).getpId()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            FirebaseDatabase.getInstance().getReference().child("Saves").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .child(modelPosts.get(position).getpId()).removeValue();
                            Snackbar.make(holder.itemView,"Unsaved", Snackbar.LENGTH_LONG).show();
                        }else{
                            FirebaseDatabase.getInstance().getReference().child("Saves").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .child(modelPosts.get(position).getpId()).setValue(true);
                            Snackbar.make(holder.itemView,"Saved", Snackbar.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Snackbar.make(holder.itemView,error.getMessage(), Snackbar.LENGTH_LONG).show();
                    }
                });

            }
            if (item.getItemId() == 2){
               if (type.equals("text") || type.equals("bg") || type.equals("gif")){
                   Snackbar.make(holder.itemView,"This type of post can't be downloaded", Snackbar.LENGTH_LONG).show();
               }else if (type.equals("video")){
                   Snackbar.make(holder.itemView,"Please wait downloading", Snackbar.LENGTH_LONG).show();
                   DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                   DownloadManager.Request request = new DownloadManager.Request(Uri.parse(modelPosts.get(position).getVine()));
                   request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                   request.setDestinationInExternalFilesDir(context, DIRECTORY_DOWNLOADS, String.valueOf(System.currentTimeMillis()) + ".mp4");
                   Objects.requireNonNull(downloadManager).enqueue(request);
               }else if (type.equals("image")){
                   Snackbar.make(holder.itemView,"Please wait downloading", Snackbar.LENGTH_LONG).show();
                   DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                   DownloadManager.Request request = new DownloadManager.Request(Uri.parse(modelPosts.get(position).getMeme()));
                   request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                   request.setDestinationInExternalFilesDir(context, DIRECTORY_DOWNLOADS, String.valueOf(System.currentTimeMillis()) + ".png");
                   Objects.requireNonNull(downloadManager).enqueue(request);
               }else if (type.equals("audio")){
                   Snackbar.make(holder.itemView,"Please wait downloading", Snackbar.LENGTH_LONG).show();
                   DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                   DownloadManager.Request request = new DownloadManager.Request(Uri.parse(modelPosts.get(position).getMeme()));
                   request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                   request.setDestinationInExternalFilesDir(context, DIRECTORY_DOWNLOADS, String.valueOf(System.currentTimeMillis()) + ".mp3");
                   Objects.requireNonNull(downloadManager).enqueue(request);
               }
            }else if (item.getItemId() == 4){
                Snackbar.make(holder.itemView,"Copied", Snackbar.LENGTH_LONG).show();
                if (type.equals("text")){
                    ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("text", modelPosts.get(position).getText());
                    clipboard.setPrimaryClip(clip);
                }else if (type.equals("image")){

                    ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("text", modelPosts.get(position).getText() + " " + modelPosts.get(position).getMeme());
                    clipboard.setPrimaryClip(clip);

                }else if (type.equals("audio")){

                    ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("text", modelPosts.get(position).getText() + " " + modelPosts.get(position).getMeme());
                    clipboard.setPrimaryClip(clip);

                }else if (type.equals("gif")){

                    ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("text", modelPosts.get(position).getText() + " " + modelPosts.get(position).getMeme());
                    clipboard.setPrimaryClip(clip);

                }else if (type.equals("video")){

                    ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("text", modelPosts.get(position).getText() + " " + modelPosts.get(position).getVine());
                    clipboard.setPrimaryClip(clip);

                }else {
                    ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("text", modelPosts.get(position).getText() + " " + modelPosts.get(position).getMeme());
                    clipboard.setPrimaryClip(clip);

                }
            }else  if (item.getItemId() == 7){
                if (type.equals("text")){
                    FirebaseDatabase.getInstance().getReference().child("Pages").child(modelPosts.get(position).getGroup()).child("Posts").child(modelPosts.get(position).getpId()).getRef().removeValue();
                    Snackbar.make(holder.itemView,"Deleted", Snackbar.LENGTH_LONG).show();
                }else if (type.equals("video")){
                    FirebaseStorage.getInstance().getReferenceFromUrl(modelPosts.get(position).getVine()).delete().addOnCompleteListener(task -> {
                        FirebaseDatabase.getInstance().getReference().child("Pages").child(modelPosts.get(position).getGroup()).child("Posts").child(modelPosts.get(position).getpId()).getRef().removeValue();
                        Snackbar.make(holder.itemView,"Deleted", Snackbar.LENGTH_LONG).show();
                    });

                }else if (type.equals("bg")){
                    FirebaseDatabase.getInstance().getReference().child("Pages").child(modelPosts.get(position).getGroup()).child("Posts").child(modelPosts.get(position).getpId()).getRef().removeValue();
                    Snackbar.make(holder.itemView,"Deleted", Snackbar.LENGTH_LONG).show();
                }
                else {
                    FirebaseStorage.getInstance().getReferenceFromUrl(modelPosts.get(position).getMeme()).delete().addOnCompleteListener(task -> {
                        FirebaseDatabase.getInstance().getReference().child("Pages").child(modelPosts.get(position).getGroup()).child("Posts").child(modelPosts.get(position).getpId()).getRef().removeValue();
                        Snackbar.make(holder.itemView,"Deleted", Snackbar.LENGTH_LONG).show();
                    });
                }
            }
            else if (item.getItemId() == 8){
                switch (modelPosts.get(position).getType()) {
                    case "image":

                        Intent intent = new Intent(context, MediaViewActivity.class);
                        intent.putExtra("type", "image");
                        intent.putExtra("uri", modelPosts.get(position).getMeme());
                        context.startActivity(intent);

                        break;
                    case "video":

                        Intent intent1 = new Intent(context, MediaViewActivity.class);
                        intent1.putExtra("type", "video");
                        intent1.putExtra("uri", modelPosts.get(position).getVine());
                        context.startActivity(intent1);

                        break;
                }
            }
            return false;
        });
        holder.more.setOnClickListener(v -> morePop.show());

        holder.comment.setOnClickListener(v -> {

            if (modelPosts.get(position).getType().equals("video")){
                FirebaseDatabase.getInstance().getReference().child("Views").child(modelPosts.get(position).getpId()).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(true);
                Intent intent = new Intent(context, CommentPagesActivity.class);
                intent.putExtra("postID", modelPosts.get(position).getpId());
                intent.putExtra("id", modelPosts.get(position).getGroup());
                context.startActivity(intent);
            }else {
                Intent intent = new Intent(context, CommentPagesActivity.class);
                intent.putExtra("postID", modelPosts.get(position).getpId());
                intent.putExtra("id", modelPosts.get(position).getGroup());
                context.startActivity(intent);
            }

        });

        holder.itemView.setOnClickListener(v -> {
            if (modelPosts.get(position).getType().equals("video")){
                FirebaseDatabase.getInstance().getReference().child("Views").child(modelPosts.get(position).getpId()).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(true);
                Intent intent = new Intent(context, CommentPagesActivity.class);
                intent.putExtra("postID", modelPosts.get(position).getpId());
                intent.putExtra("id", modelPosts.get(position).getGroup());
                context.startActivity(intent);
            }else {
                Intent intent = new Intent(context, CommentPagesActivity.class);
                intent.putExtra("postID", modelPosts.get(position).getpId());
                intent.putExtra("id", modelPosts.get(position).getGroup());
                context.startActivity(intent);
            }
        });


    }


    @Override
    public int getItemCount() {
        return modelPosts.size();
    }

    static class MyHolder extends RecyclerView.ViewHolder{

        final CircleImageView dp;
        final ImageView verified;
        final ImageView activity;
        final SelectableRoundedImageView mediaView;
        final ImageView play;
        final ImageView like_img;
        final ImageView more;
        final TextView name;
        final TextView username;
        final TextView time;
        final TextView feeling;
        final TextView location;
        final TextView like_text;
        final SocialTextView text;
        final SocialTextView bg_text;
        final VoicePlayerView voicePlayerView;
        final LinearLayout likeLayout;
        final LinearLayout commentLayout;
        final LinearLayout viewsLayout;
        final LinearLayout layout;
        final LinearLayout share;
        final TextView noLikes;
        final TextView noComments;
        final TextView noViews;
        final ImageView thumb;
        final ImageView love;
        final ImageView laugh;
        final ImageView wow;
        final ImageView angry;
        final ImageView sad;
        final LinearLayout likeButton;
        final LinearLayout likeButtonTwo;
        final LinearLayout comment;
        final RelativeLayout line;
        final RelativeLayout ad,media_layout;
        URLEmbeddedView urlEmbeddedView;
        final NativeAdLayout nativeAdLayout;


        public MyHolder(@NonNull View itemView) {
            super(itemView);
            media_layout = itemView.findViewById(R.id.media_layout);
            urlEmbeddedView = itemView.findViewById(R.id.uev);
            dp = itemView.findViewById(R.id.dp);
            verified = itemView.findViewById(R.id.verified);
            name = itemView.findViewById(R.id.name);
            ad = itemView.findViewById(R.id.ad);
            username = itemView.findViewById(R.id.username);
            comment = itemView.findViewById(R.id.comment);
            time = itemView.findViewById(R.id.time);
            activity = itemView.findViewById(R.id.activity);
            feeling = itemView.findViewById(R.id.feeling);
            location = itemView.findViewById(R.id.location);
            text = itemView.findViewById(R.id.text);
            mediaView = itemView.findViewById(R.id.mediaView);
            bg_text = itemView.findViewById(R.id.bg_text);
            share = itemView.findViewById(R.id.share);
            play = itemView.findViewById(R.id.play);
            voicePlayerView = itemView.findViewById(R.id.voicePlayerView);
            likeLayout = itemView.findViewById(R.id.likeLayout);
            commentLayout = itemView.findViewById(R.id.commentLayout);
            viewsLayout = itemView.findViewById(R.id.viewsLayout);
            layout = itemView.findViewById(R.id.layout);
            noLikes =  itemView.findViewById(R.id.noLikes);
            noComments  =  itemView.findViewById(R.id.noComments);
            noViews  =  itemView.findViewById(R.id.noViews);
            like_text =  itemView.findViewById(R.id.like_text);
            like_img  =  itemView.findViewById(R.id.like_img);
            thumb  =  itemView.findViewById(R.id.thumb);
            love  =  itemView.findViewById(R.id.love);
            laugh  =  itemView.findViewById(R.id.laugh);
            wow  =  itemView.findViewById(R.id.wow);
            angry  =  itemView.findViewById(R.id.angry);
            likeButton  =  itemView.findViewById(R.id.likeButton);
            sad =  itemView.findViewById(R.id.sad);
            likeButtonTwo =  itemView.findViewById(R.id.likeButtonTwo);
            line = itemView.findViewById(R.id.line);
            more = itemView.findViewById(R.id.more);
            nativeAdLayout= itemView.findViewById(R.id.nativeBannerAd);

            AdLoader adLoader = new AdLoader.Builder(itemView.getContext(), itemView.getContext().getString(R.string.native_ad_unit_id_groups))
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
