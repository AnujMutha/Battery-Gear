package com.muthadevelopers.batterygear.model;

import android.app.Activity;
import android.os.Build;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.muthadevelopers.batterygear.R;

import smartdevelop.ir.eram.showcaseviewlib.GuideView;

public class ShowCase extends AppCompatActivity {


    public static void showCase(Activity activity,String title, String text, int viewId, final int type) {

        if (Build.VERSION.SDK_INT >= 21) {
            activity.getWindow().setNavigationBarColor(activity.getResources().getColor(R.color.black_alpha));
            activity.getWindow().setStatusBarColor(activity.getResources().getColor(R.color.black_alpha));
        }

        new GuideView.Builder(activity)
                .setTitle(title)
                .setContentText(text)
                .setTargetView(activity.findViewById(viewId))
                .setContentTextSize(12)//optional
                .setTitleTextSize(14)//optional
                .setContentTypeFace(ResourcesCompat.getFont(activity, R.font.ps_regular))
                .setTitleTypeFace(ResourcesCompat.getFont(activity, R.font.ps_bold))
                .setDismissType(GuideView.DismissType.anywhere)
                .setGuideListener(new GuideView.GuideListener() {
                    @Override
                    public void onDismiss(View view) {
                        if (type == 2) {
                            showCase(activity,"Battery Level", "This Circular animation represents battery level and changes its color depending on the battery level (0->Red & 100->Green)", R.id.chargingAnimator, 3);
                        } else if (type == 3) {
                            activity.findViewById(R.id.mainScrollView).scrollTo(0, activity.findViewById(R.id.mainScrollView).getBottom());
                            showCase(activity,"About Developer", "Contains information about developer", R.id.infoButton, 4);
                        } else if (type == 4) {
                            showCase(activity,"System Information", "This section contains details about your device's firmware and some basic hardware info", R.id.systemInfoButton, 5);
                        } else if (type == 5) {
                            activity.findViewById(R.id.mainScrollView).scrollTo(100,activity.findViewById(R.id.mainScrollView).FOCUS_UP);
                            showCase(activity,"API Information", "This contains information about libraries, api's used and their licenses", R.id.licensesIcon, 6);
                        } else if (type == 6) {
                            if (Build.VERSION.SDK_INT >= 21) {
                                activity.getWindow().setNavigationBarColor(activity.getResources().getColor(R.color.dark_blue));
                                activity.getWindow().setStatusBarColor(activity.getResources().getColor(R.color.dark_blue));
                            }
                        }
                    }
                })
                .build()
                .show();
    }
}
