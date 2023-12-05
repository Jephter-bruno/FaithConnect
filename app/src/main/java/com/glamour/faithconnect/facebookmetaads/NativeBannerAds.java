package com.glamour.faithconnect.facebookmetaads;

import android.app.Activity;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.facebook.ads.*;

import java.util.ArrayList;
import java.util.List;
import com.glamour.faithconnect.R;

public class NativeBannerAds {

    private Activity activity;

    public NativeBannerAds(Activity activity) {
        this.activity = activity;
    }

    public void loadNativeAd(
            NativeAdLayout nativeAdLayout,
            boolean isCustomLayout,
            String placementId
    ) {
        NativeAd nativeAd = new NativeAd(activity, placementId);
        NativeAdListener nativeAdListener = new NativeAdListener() {
            @Override
            public void onError(Ad ad, AdError adError) {
                //activity.longToastShow(adError != null ? adError.getErrorMessage() : "");
            }

            @Override
            public void onAdLoaded(Ad ad) {
                if (isCustomLayout) {
                    customNativeLayout(nativeAd, nativeAdLayout);
                } else {
                    TypedValue typedValue = new TypedValue();
                    activity.getTheme().resolveAttribute(R.attr.layoutcolor, typedValue, true);
                    int backgroundColor = typedValue.data;

                    activity.getTheme().resolveAttribute(R.attr.textcolor, typedValue, true);
                    int titleTextColor = typedValue.data;

                    NativeAdViewAttributes viewAttributes = new NativeAdViewAttributes(activity)
                            .setBackgroundColor(backgroundColor)
                            .setTitleTextColor(titleTextColor);

                    View adView = NativeAdView.render(activity, nativeAd,viewAttributes);
                    nativeAdLayout.addView(adView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 800));
                }
            }

            @Override
            public void onAdClicked(Ad ad) {
            }

            @Override
            public void onLoggingImpression(Ad ad) {
            }

            @Override
            public void onMediaDownloaded(Ad ad) {
            }
        };

        nativeAd.loadAd(
                nativeAd.buildLoadAdConfig()
                        .withMediaCacheFlag(NativeAdBase.MediaCacheFlag.ALL)
                        .withAdListener(nativeAdListener)
                        .build()
        );
    }

    private void customNativeLayout(NativeAd nativeAd, NativeAdLayout nativeAdLayout) {
        nativeAd.unregisterView();

        View adView = LayoutInflater.from(activity)
                .inflate(R.layout.native_ad_layout, nativeAdLayout, false);
        nativeAdLayout.addView(adView);

        LinearLayout adChoicesContainer = activity.findViewById(R.id.ad_choices_container);
        AdOptionsView adOptionsView = new AdOptionsView(activity, nativeAd, nativeAdLayout);
        adChoicesContainer.removeAllViews();
        adChoicesContainer.addView(adOptionsView, 0);

        MediaView nativeAdIcon = adView.findViewById(R.id.native_ad_icon);
        TextView nativeAdTitle = adView.findViewById(R.id.native_ad_title);
        MediaView nativeAdMedia = adView.findViewById(R.id.native_ad_media);
        TextView nativeAdSocialContext = adView.findViewById(R.id.native_ad_social_context);
        TextView nativeAdBody = adView.findViewById(R.id.native_ad_body);
        TextView sponsoredLabel = adView.findViewById(R.id.native_ad_sponsored_label);
        Button nativeAdCallToAction = adView.findViewById(R.id.native_ad_call_to_action);

        nativeAdTitle.setText(nativeAd.getAdvertiserName());
        nativeAdBody.setText(nativeAd.getAdBodyText());
        nativeAdSocialContext.setText(nativeAd.getAdSocialContext());
        nativeAdCallToAction.setVisibility(nativeAd.hasCallToAction() ? View.VISIBLE : View.INVISIBLE);
        nativeAdCallToAction.setText(nativeAd.getAdCallToAction());
        sponsoredLabel.setText(nativeAd.getSponsoredTranslation());

        List<View> clickableViews = new ArrayList<>();
        clickableViews.add(nativeAdTitle);
        clickableViews.add(nativeAdCallToAction);
        clickableViews.add(nativeAdIcon);

        nativeAd.registerViewForInteraction(
                adView, nativeAdMedia, nativeAdIcon, clickableViews
        );
    }
}
