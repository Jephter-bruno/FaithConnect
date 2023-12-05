package com.glamour.faithconnect.live;

import android.content.SharedPreferences;


import com.facebook.ads.AdSettings;
import com.facebook.ads.AudienceNetworkAds;
import com.glamour.faithconnect.R;
import com.glamour.faithconnect.groupVideoCall.openvcall.AGApplicationVideo;
import com.glamour.faithconnect.live.rtc.AgoraEventHandler;
import com.glamour.faithconnect.live.rtc.EngineConfig;
import com.glamour.faithconnect.live.rtc.EventHandler;
import com.glamour.faithconnect.live.stats.StatsManager;
import com.glamour.faithconnect.live.utils.FileUtil;
import com.glamour.faithconnect.live.utils.PrefManager;
import com.google.android.gms.ads.MobileAds;

import io.agora.rtc.RtcEngine;

@SuppressWarnings("ALL")
public class AgoraApplication extends AGApplicationVideo {
    private RtcEngine mRtcEngine;
    private EngineConfig mGlobalConfig = new EngineConfig();
    private AgoraEventHandler mHandler = new AgoraEventHandler();
    private StatsManager mStatsManager = new StatsManager();

    @Override
    public void onCreate() {
        super.onCreate();
       AudienceNetworkAds.initialize(this);
        MobileAds.initialize(this);
        // if app release change to false
       AdSettings.setTestMode(true);
        // Example for setting the SDK to crash when in debug mode*/
        try {
            mRtcEngine = RtcEngine.create(getApplicationContext(), getString(R.string.private_app_id), mHandler);
            mRtcEngine.setLogFile(FileUtil.initializeLogFile(this));
        } catch (Exception e) {
            e.printStackTrace();
        }

        initConfig();
    }

    private void initConfig() {
        SharedPreferences pref = PrefManager.getPreferences(getApplicationContext());
        mGlobalConfig.setVideoDimenIndex(pref.getInt(
                Constants.PREF_RESOLUTION_IDX, Constants.DEFAULT_PROFILE_IDX));

        boolean showStats = pref.getBoolean(Constants.PREF_ENABLE_STATS, false);
        mGlobalConfig.setIfShowVideoStats(showStats);
        mStatsManager.enableStats(showStats);

        mGlobalConfig.setMirrorLocalIndex(pref.getInt(Constants.PREF_MIRROR_LOCAL, 0));
        mGlobalConfig.setMirrorRemoteIndex(pref.getInt(Constants.PREF_MIRROR_REMOTE, 0));
        mGlobalConfig.setMirrorEncodeIndex(pref.getInt(Constants.PREF_MIRROR_ENCODE, 0));
    }

    public EngineConfig engineConfig() {
        return mGlobalConfig;
    }

    public RtcEngine rtcEngine() {
        return mRtcEngine;
    }

    public StatsManager statsManager() {
        return mStatsManager;
    }

    public void registerEventHandler(EventHandler handler) {
        mHandler.addHandler(handler);
    }

    public void removeEventHandler(EventHandler handler) {
        mHandler.removeHandler(handler);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        RtcEngine.destroy();
    }
}
