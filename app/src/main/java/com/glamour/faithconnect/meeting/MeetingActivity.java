package com.glamour.faithconnect.meeting;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.facebook.ads.NativeBannerAdView;
import com.glamour.faithconnect.NightMode;
import com.glamour.faithconnect.R;
import com.glamour.faithconnect.facebookmetaads.MyNativeBannerAd;
import com.glamour.faithconnect.nativetemplates.NativeTemplateStyle;
import com.glamour.faithconnect.nativetemplates.TemplateView;
import com.glamour.faithconnect.send.SendToGroupActivity;
import com.glamour.faithconnect.send.SendToUserActivity;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.tabs.TabLayout;
import org.jitsi.meet.sdk.JitsiMeet;
import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;

import java.net.MalformedURLException;
import java.net.URL;

public class MeetingActivity extends AppCompatActivity {

    BottomSheetDialog share_options;
    LinearLayout app,groups,users;
    EditText meetingCreate;
     TemplateView template;
    @SuppressLint("SetTextI18n")
    NightMode sharedPref;

    @SuppressLint("SetTextI18n")

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPref = new NightMode(this);
        if (sharedPref.loadNightModeState().equals("night")){
            setTheme(R.style.DarkTheme);
        }else if (sharedPref.loadNightModeState().equals("dim")){
            setTheme(R.style.DimTheme);
        }else setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting);
        // init Facebook Ads
        MyNativeBannerAd nativeBannerAd = new MyNativeBannerAd(this);
        //back
        findViewById(R.id.imageView).setOnClickListener(v -> onBackPressed());

        //Edit
        EditText meeting = findViewById(R.id.meeting);
         meetingCreate = findViewById(R.id.meetingCreate);

        meetingCreate.setText(""+System.currentTimeMillis());

        if (getIntent().hasExtra("meet")){
            meeting.setText(getIntent().getStringExtra("meet"));
            findViewById(R.id.progressBarJ).setVisibility(View.VISIBLE);
            JitsiMeetConferenceOptions options = new JitsiMeetConferenceOptions.Builder()
                    .setRoom(getIntent().getStringExtra("meet"))
                    .build();
            JitsiMeetActivity.launch(MeetingActivity.this, options);
            findViewById(R.id.progressBarJ).setVisibility(View.GONE);
        }

        //TabLayout
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tabLayout.getSelectedTabPosition() == 0) {
                    findViewById(R.id.join).setVisibility(View.VISIBLE);
                    // Facebook Audience Ads
                    nativeBannerAd.loadNativeBannerAd(
                            findViewById(R.id.nativeBannerAd),
                            NativeBannerAdView.Type.HEIGHT_120,
                            true,
                            "102713349600103_116057628265675"
                    );

                    findViewById(R.id.create).setVisibility(View.GONE);
                } else if (tabLayout.getSelectedTabPosition() == 1) {
                    findViewById(R.id.join).setVisibility(View.GONE);
                    findViewById(R.id.create).setVisibility(View.VISIBLE);
                    //admob Ads
                    template = findViewById(R.id.my_template);
                    AdLoader adLoader = new AdLoader.Builder(MeetingActivity.this, getApplicationContext().getString(R.string.native_ad_unit_id))
                            .forNativeAd(new NativeAd.OnNativeAdLoadedListener() {
                                @Override
                                public void onNativeAdLoaded(NativeAd nativeAd) {
                                    NativeTemplateStyle styles = new
                                            NativeTemplateStyle.Builder().build();
                                    template.setStyles(styles);
                                    template.setNativeAd(nativeAd);
                                }
                            })
                            .build();

                    adLoader.loadAd(new AdRequest.Builder().build());
                    meetingCreate.setText(""+System.currentTimeMillis());
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        //Meet
        try {
            URL server = new URL("https://meet.jit.si");
            JitsiMeetConferenceOptions options = new JitsiMeetConferenceOptions.Builder()
                    .setServerURL(server)

                    .build();
            JitsiMeet.setDefaultConferenceOptions(options);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        //Join
        findViewById(R.id.nextMeet).setOnClickListener(v -> {
            findViewById(R.id.progressBarJ).setVisibility(View.VISIBLE);
            JitsiMeetConferenceOptions options = new JitsiMeetConferenceOptions.Builder()
                    .setRoom(meeting.getText().toString())

                    .build();
            JitsiMeetActivity.launch(MeetingActivity.this, options);
            findViewById(R.id.progressBarJ).setVisibility(View.GONE);
        });

        //Create
        findViewById(R.id.createMeet).setOnClickListener(v -> {
            findViewById(R.id.progressBarC).setVisibility(View.VISIBLE);
            JitsiMeetConferenceOptions options = new JitsiMeetConferenceOptions.Builder()
                    .setRoom(meetingCreate.getText().toString())

                    .build();
            JitsiMeetActivity.launch(MeetingActivity.this, options);
            findViewById(R.id.progressBarC).setVisibility(View.GONE);
        });

        findViewById(R.id.shareId).setOnClickListener(v -> share_options.show());

        share_bottom();

    }

    private void share_bottom() {
        if (share_options == null){
            @SuppressLint("InflateParams") View view = LayoutInflater.from(MeetingActivity.this).inflate(R.layout.share_options, null);
            app = view.findViewById(R.id.app);
            groups = view.findViewById(R.id.groups);
            users = view.findViewById(R.id.users);

            app.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/*");
                intent.putExtra(Intent.EXTRA_SUBJECT,"Meeting Id");
                intent.putExtra(Intent.EXTRA_TEXT, "Meeting Id is " + meetingCreate.getText().toString());
                startActivity(Intent.createChooser(intent, "Share Via"));
            });

            groups.setOnClickListener(v -> {
                Intent intent = new Intent(MeetingActivity.this, SendToGroupActivity.class);
                intent.putExtra("type", "meet");
                intent.putExtra("uri", meetingCreate.getText().toString());
                startActivity(intent);
            });

            users.setOnClickListener(v -> {
                Intent intent = new Intent(MeetingActivity.this, SendToUserActivity.class);
                intent.putExtra("type", "meet");
                intent.putExtra("uri", meetingCreate.getText().toString());
                startActivity(intent);
            });

            share_options = new BottomSheetDialog(MeetingActivity.this);
            share_options.setContentView(view);
        }
    }

}