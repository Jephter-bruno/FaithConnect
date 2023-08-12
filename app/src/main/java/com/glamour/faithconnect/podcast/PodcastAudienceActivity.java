package com.glamour.faithconnect.podcast;

import android.content.Intent;
import android.os.Bundle;


import com.glamour.faithconnect.live.activities.BaseActivity;

import io.agora.rtc.Constants;


@SuppressWarnings("ALL")
public class PodcastAudienceActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gotoLiveActivity(Constants.CLIENT_ROLE_AUDIENCE);

    }
    private void gotoLiveActivity(int role) {
        Intent intent = new Intent(getIntent());
        intent.putExtra(com.glamour.faithconnect.live.Constants.KEY_CLIENT_ROLE, role);
        intent.setClass(getApplicationContext(), PodcastActivity.class);
        intent.putExtra("type", "aud");
        startActivity(intent);
        finish();
    }
}