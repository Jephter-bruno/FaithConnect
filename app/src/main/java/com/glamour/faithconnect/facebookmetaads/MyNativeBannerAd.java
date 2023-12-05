package com.glamour.faithconnect.facebookmetaads;

import android.app.Activity;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.facebook.ads.*;

import java.util.ArrayList;
import java.util.List;
import com.glamour.faithconnect.R;
public class MyNativeBannerAd {

    private final Activity activity;

    public MyNativeBannerAd(Activity activity) {
        this.activity = activity;
    }

    public void loadNativeBannerAd(
            NativeAdLayout nativeAdLayout,
            NativeBannerAdView.Type type,
            boolean isCustomLayout,
            String placementId
    ) {
        NativeBannerAd nativeBannerAd = new NativeBannerAd(activity, placementId);
        NativeAdListener nativeAdListener = new NativeAdListener() {
            @Override
            public void onError(Ad ad, AdError adError) {
                // Handle ad load error
            }

            @Override
            public void onAdLoaded(Ad ad) {
                if (isCustomLayout) {
                    customNativeBannerLayout(nativeBannerAd, nativeAdLayout);
                } else {
                    TypedValue typedValue = new TypedValue();
                    activity.getTheme().resolveAttribute(R.attr.layoutcolor, typedValue, true);
                    int backgroundColor = typedValue.data;

                    activity.getTheme().resolveAttribute(R.attr.textcolor, typedValue, true);
                    int titleTextColor = typedValue.data;
                    NativeAdViewAttributes viewAttributes = new NativeAdViewAttributes(activity)
                            .setBackgroundColor(backgroundColor)
                            .setTitleTextColor(titleTextColor)
                            .setDescriptionTextColor(Color.LTGRAY)
                            .setButtonColor(Color.WHITE)
                            .setButtonTextColor(Color.BLACK);

                    View adView = NativeBannerAdView.render(activity, nativeBannerAd, type, viewAttributes);
                    nativeAdLayout.addView(adView);
                }
            }

            @Override
            public void onAdClicked(Ad ad) {
                // Handle ad click
            }

            @Override
            public void onLoggingImpression(Ad ad) {
                // Handle logging impression of the ad
            }

            @Override
            public void onMediaDownloaded(Ad ad) {
                // Indicates that the media content is downloaded
            }
        };

        nativeBannerAd.loadAd(
                nativeBannerAd.buildLoadAdConfig()
                        .withAdListener(nativeAdListener)
                        .build()
        );
    }

    private void customNativeBannerLayout(NativeBannerAd nativeBannerAd, NativeAdLayout nativeAdLayout) {
        nativeBannerAd.unregisterView();

        // Inflate the Ad view.
        View adView = LayoutInflater.from(activity).inflate(R.layout.native_banner_ad_container, nativeAdLayout, false);
        nativeAdLayout.addView(adView);

        // Add the AdChoices icon
        RelativeLayout adChoicesContainer = adView.findViewById(R.id.ad_choices_container);
        AdOptionsView adOptionsView = new AdOptionsView(activity, nativeBannerAd, nativeAdLayout);
        adChoicesContainer.removeAllViews();
        adChoicesContainer.addView(adOptionsView, 0);

        TextView nativeAdTitle = adView.findViewById(R.id.native_ad_title);
        TextView nativeAdSocialContext = adView.findViewById(R.id.native_ad_social_context);
        TextView sponsoredLabel = adView.findViewById(R.id.native_ad_sponsored_label);
        MediaView nativeAdIconView = adView.findViewById(R.id.native_icon_view);
        Button nativeAdCallToAction = adView.findViewById(R.id.native_ad_call_to_action);

        nativeAdCallToAction.setText(nativeBannerAd.getAdCallToAction());
        nativeAdCallToAction.setVisibility(nativeBannerAd.hasCallToAction() ? View.VISIBLE : View.INVISIBLE);
        nativeAdTitle.setText(nativeBannerAd.getAdvertiserName());
        nativeAdSocialContext.setText(nativeBannerAd.getAdSocialContext());
        sponsoredLabel.setText(nativeBannerAd.getSponsoredTranslation());

        List<View> clickableViews = new ArrayList<>();
        clickableViews.add(nativeAdTitle);
        clickableViews.add(nativeAdCallToAction);
        clickableViews.add(nativeAdIconView);

        nativeBannerAd.registerViewForInteraction(adView, nativeAdIconView, clickableViews);
    }
    }

