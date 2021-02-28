package com.muthadevelopers.batterygear;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.muthadevelopers.batterygear.model.AndroidVersion;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.analytics.FirebaseAnalytics;

import me.itangqi.waveloadingview.WaveLoadingView;

import static com.muthadevelopers.batterygear.model.ShowCase.showCase;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    TextView phoneDetails;

    TextView cStatus, cPlugged, cHealth, cTech, cVoltage, cTemperature, cCapacity, cAmpere;
    TextView textValManufacturer, textValModel, textValAndroidVersion, textValCPU, textValBuild, textValSecurityPatch, textValBaseBandVersion, textValBootloader, textVersion;
    RelativeLayout rootPullUpBarLayout, actionBar;
    LinearLayout aboutRootLayout, phoneInfoRootLayout, rateUsButton;

    LottieAnimationView chargingParticleAnimation, flashAnimation;
    ImageView infoIcon, closeArrow, infoButton, systemInfoButton;
    WaveLoadingView cAnimator;

    private final int ANIM_DURATION = 30000;
    private int status, plugged, health, level, voltage, temp = 0;
    private float tempConverted, voltageConverted;
    private String tech;
    private Object mPowerProfile = null;
    private String POWER_PROFILE_CLASS = "com.android.internal.os.PowerProfile";
    private Double capacity;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor sharedEditor;

    private FirebaseAnalytics mFirebaseAnalytics;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getApplicationContext());

        viewFinder();

        try {
            PackageInfo pInfo = MainActivity.this.getPackageManager().getPackageInfo(MainActivity.this.getPackageName(), 0);
            textVersion.setText("Version : "+pInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if (isItFirstTime()) {
            showCase(this, "Battery Information", "This section provide detailed info regarding battery\nRemember,\nIn order to read current information a special inbuild hardware is required known as fuel gauge. So, if the hardware isn't present in the device the current statistics will not be recorded", R.id.widgetLayout, 2);
        }

        Handler handler = new Handler();

        final Runnable r = new Runnable() {
            public void run() {
                BatteryManager mBatteryManager = (BatteryManager) getSystemService(Context.BATTERY_SERVICE);
                Long currentNow = null;
                currentNow = mBatteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW);
                if (Build.VERSION.SDK_INT >= 28) {
                    if ((currentNow / 1000) == 1 || (currentNow / 1000) == -1 || (currentNow / 1000) == 0) {
                        cAmpere.setText("Unknown");
                    } else {
                        if ((currentNow / 1000) >= 1000) {
                            cAmpere.setText(((currentNow / 1000) / 1000) + " Ah");
                        } else {
                            cAmpere.setText((currentNow / 1000) + " mAh");
                        }
                    }
                } else {
                    cAmpere.setText("No H/W Found");
                }
                //Toast.makeText(getApplicationContext(),avgCurrent+" AND "+currentNow,Toast.LENGTH_SHORT).show();
                handler.postDelayed(this, 2000);
            }
        };

        handler.postDelayed(r, 1000);

        infoIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                infoBuilder();
            }
        });

        registerReceiver(this.batteryInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        try {
            mPowerProfile = Class.forName(POWER_PROFILE_CLASS)
                    .getConstructor(Context.class).newInstance(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            capacity = (Double) Class
                    .forName(POWER_PROFILE_CLASS)
                    .getMethod("getAveragePower", java.lang.String.class)
                    .invoke(mPowerProfile, "battery.capacity");
        } catch (Exception e) {
            e.printStackTrace();
        }

        systemInfoInitializer();

        systemInfoInflater();
    }

    //END OF onCREATE() METHOD

    BroadcastReceiver batteryInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            batteryInfoFetcher(intent);

            switch (status) {
                case BatteryManager.BATTERY_STATUS_CHARGING:
                    cStatusViewInitializer(R.string.battery_status_charging, 2000, View.VISIBLE);
                    cAmpere.setTextColor(getResources().getColor(R.color.green));
                    cVoltage.setTextColor(getResources().getColor(R.color.green));
                    break;
                case BatteryManager.BATTERY_STATUS_DISCHARGING:
                    cStatusViewInitializer(R.string.battery_status_discharging, ANIM_DURATION, View.INVISIBLE);
                    cAmpere.setTextColor(getResources().getColor(R.color.orange));
                    cVoltage.setTextColor(getResources().getColor(R.color.orange));
                    break;
                case BatteryManager.BATTERY_STATUS_FULL:
                    cStatusViewInitializer(R.string.battery_status_full, ANIM_DURATION, View.INVISIBLE);
                    cAmpere.setTextColor(getResources().getColor(R.color.green));
                    cVoltage.setTextColor(getResources().getColor(R.color.green));
                    break;
                case BatteryManager.BATTERY_STATUS_UNKNOWN:
                    cStatusViewInitializer(R.string.battery_status_unknown, ANIM_DURATION, View.INVISIBLE);
                    cAmpere.setTextColor(getResources().getColor(R.color.orange));
                    cVoltage.setTextColor(getResources().getColor(R.color.orange));
                    break;
                case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                    cStatusViewInitializer(R.string.battery_status_not_charging, ANIM_DURATION, View.INVISIBLE);
                    cAmpere.setTextColor(getResources().getColor(R.color.orange));
                    cVoltage.setTextColor(getResources().getColor(R.color.orange));
                    break;
            }

            switch (plugged) {
                case BatteryManager.BATTERY_PLUGGED_WIRELESS:
                    cPlugged.setText(R.string.battery_plugged_wireless);
                    break;

                case BatteryManager.BATTERY_PLUGGED_USB:
                    cPlugged.setText(R.string.battery_plugged_usb);
                    break;

                case BatteryManager.BATTERY_PLUGGED_AC:
                    cPlugged.setText(R.string.battery_plugged_ac);
                    break;

                default:
                    cPlugged.setText("None");
                    break;
            }

            switch (health) {
                case BatteryManager.BATTERY_HEALTH_COLD:
                    cHealth.setText(R.string.battery_health_cold);
                    break;

                case BatteryManager.BATTERY_HEALTH_DEAD:
                    cHealth.setText(R.string.battery_health_dead);
                    break;

                case BatteryManager.BATTERY_HEALTH_GOOD:
                    cHealth.setText(R.string.battery_health_good);
                    break;

                case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
                    cHealth.setText(R.string.battery_health_over_voltage);
                    break;

                case BatteryManager.BATTERY_HEALTH_OVERHEAT:
                    cHealth.setText(R.string.battery_health_overheat);
                    break;

                case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
                    cHealth.setText(R.string.battery_health_unspecified_failure);
                    break;
                default:
                    cHealth.setText(R.string.BATTERY_HEALTH_UNKNOWN);
                    break;
            }

            cTech.setText(tech);
            cCapacity.setText(capacity.intValue() + "mah");

            tempConverted = ((float) temp) / 10f;
            cTemperature.setText(tempConverted + "°C");

            cAnimator.setCenterTitle(level + "%");

            voltageConverted = (float) (voltage * 0.001);
            cVoltage.setText(voltageConverted + " v");

            cAnimator.setProgressValue(level);

            switch (level) {
                //1
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                    cAnimator.setWaveColor(Color.rgb(255, 0, 0));
                    break;
                //2
                case 6:
                case 7:
                case 8:
                case 9:
                case 10:
                    cAnimator.setWaveColor(Color.rgb(255, 0, 0));
                    break;
                //3
                case 11:
                case 12:
                case 13:
                case 14:
                case 15:
                    cAnimator.setWaveColor(Color.rgb(249, 64, 0));
                    break;
                //4
                case 16:
                case 17:
                case 18:
                case 19:
                case 20:
                    cAnimator.setWaveColor(Color.rgb(245, 80, 0));
                    break;
                //5
                case 21:
                case 22:
                case 23:
                case 24:
                case 25:
                    cAnimator.setWaveColor(Color.rgb(240, 95, 0));
                    break;
                //6
                case 26:
                case 27:
                case 28:
                case 29:
                case 30:
                    cAnimator.setWaveColor(Color.rgb(235, 107, 0));
                    break;
                //7
                case 31:
                case 32:
                case 33:
                case 34:
                case 35:
                    cAnimator.setWaveColor(Color.rgb(228, 119, 0));
                    break;
                //8
                case 36:
                case 37:
                case 38:
                case 39:
                case 40:
                    cAnimator.setWaveColor(Color.rgb(221, 130, 0));
                    break;
                //9
                case 41:
                case 42:
                case 43:
                case 44:
                case 45:
                    cAnimator.setWaveColor(Color.rgb(213, 140, 0));
                    break;
                //10
                case 46:
                case 47:
                case 48:
                case 49:
                case 50:
                    cAnimator.setWaveColor(Color.rgb(204, 150, 0));
                    break;

                //11
                case 51:
                case 52:
                case 53:
                case 54:
                case 55:
                    cAnimator.setWaveColor(Color.rgb(194, 159, 0));
                    break;
                //12
                case 56:
                case 57:
                case 58:
                case 59:
                case 60:
                    cAnimator.setWaveColor(Color.rgb(184, 168, 0));
                    break;
                //13
                case 61:
                case 62:
                case 63:
                case 64:
                case 65:
                    cAnimator.setWaveColor(Color.rgb(173, 176, 0));
                    break;
                //14
                case 66:
                case 67:
                case 68:
                case 69:
                case 70:
                    cAnimator.setWaveColor(Color.rgb(161, 184, 0));
                    break;
                //15
                case 71:
                case 72:
                case 73:
                case 74:
                case 75:
                    cAnimator.setWaveColor(Color.rgb(148, 191, 0));
                    break;
                //16
                case 76:
                case 77:
                case 78:
                case 79:
                case 80:
                    cAnimator.setWaveColor(Color.rgb(133, 199, 0));
                    break;
                //17
                case 81:
                case 82:
                case 83:
                case 84:
                case 85:
                    cAnimator.setWaveColor(Color.rgb(116, 206, 0));
                    break;
                //18
                case 86:
                case 87:
                case 88:
                case 89:
                case 90:
                    cAnimator.setWaveColor(Color.rgb(96, 212, 0));
                    break;
                //19
                case 91:
                case 92:
                case 93:
                case 94:
                case 95:
                    cAnimator.setWaveColor(Color.rgb(68, 219, 0));
                    break;
                //20
                case 96:
                case 97:
                case 98:
                case 99:
                case 100:
                    cAnimator.setWaveColor(Color.rgb(0, 225, 0));
                    break;
                default:
                    cAnimator.setWaveColor(Color.rgb(0, 255, 0));
            }
        }
    };

    private void viewFinder() {
        infoIcon = findViewById(R.id.licensesIcon);
        chargingParticleAnimation = findViewById(R.id.chargingParticleAnimation);
        flashAnimation = findViewById(R.id.flashAnimation);
        cAnimator = findViewById(R.id.chargingAnimator);
        cAnimator.setAnimDuration(ANIM_DURATION);
        cStatus = findViewById(R.id.cStatus);
        cPlugged = findViewById(R.id.cPlugged);
        cHealth = findViewById(R.id.cHealth);
        cTech = findViewById(R.id.cTechnology);
        cVoltage = findViewById(R.id.cVoltage);
        cTemperature = findViewById(R.id.cTemperature);
        cCapacity = findViewById(R.id.cCapacity);
        cAmpere = findViewById(R.id.cAmpere);
        actionBar = findViewById(R.id.actionBar);
        rootPullUpBarLayout = findViewById(R.id.rootPullUpBarLayout);
        closeArrow = findViewById(R.id.closeArrow);
        infoButton = findViewById(R.id.infoButton);
        systemInfoButton = findViewById(R.id.systemInfoButton);
        phoneInfoRootLayout = findViewById(R.id.phoneInfoRootLayout);
        phoneDetails = findViewById(R.id.phoneDetails);
        aboutRootLayout = findViewById(R.id.aboutRootLayout);
        textVersion = findViewById(R.id.textVersion);
        rateUsButton = findViewById(R.id.rateUsButton);
        rateUsButton.setOnClickListener(this);

        //SystemInfoViews
        textValManufacturer = findViewById(R.id.textValManufacturer);
        textValModel = findViewById(R.id.textValModel);
        textValAndroidVersion = findViewById(R.id.textValAndroidVersion);
        textValCPU = findViewById(R.id.textValCPU);
        textValBuild = findViewById(R.id.textValBuild);
        textValSecurityPatch = findViewById(R.id.textValSecurityPatch);
        textValBaseBandVersion = findViewById(R.id.textValBaseBandVersion);
        textValBootloader = findViewById(R.id.textValBootloader);

        findViewById(R.id.buttonInstgram).setOnClickListener(this);
        findViewById(R.id.buttonFacebook).setOnClickListener(this);
        findViewById(R.id.buttonTwitter).setOnClickListener(this);
        findViewById(R.id.buttonGithub).setOnClickListener(this);
        findViewById(R.id.buttonLinkedin).setOnClickListener(this);
        findViewById(R.id.buttonLink).setOnClickListener(this);
        findViewById(R.id.buttonFirmLink).setOnClickListener(this);
        findViewById(R.id.buttonMail).setOnClickListener(this);
    }

    private void batteryInfoFetcher(Intent intent) {
        status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, 0);
        plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
        health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, 0);
        tech = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY);
        level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
        voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0);
        temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
    }

    private void cStatusViewInitializer(Integer text, int duration, Integer visibility) {
        cStatus.setText(text);

        cAnimator.setAnimDuration(duration);

        chargingParticleAnimation.resumeAnimation();
        chargingParticleAnimation.setVisibility(visibility);

        flashAnimation.resumeAnimation();
        flashAnimation.setVisibility(visibility);
    }

    private void infoBuilder() {

        String URL = "file:///android_asset/licenses.html";
        AlertDialog.Builder alert = new AlertDialog.Builder(new ContextThemeWrapper(MainActivity.this, R.style.MyDialogTheme));
        alert.setTitle("Licenses and Credits");
        WebView wv = new WebView(MainActivity.this);
        wv.loadUrl(URL);
        wv.setVerticalScrollBarEnabled(false);
        wv.setHorizontalScrollBarEnabled(false);
        wv.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url != null && url.startsWith("http://") || url != null && url.startsWith("https://")) {
                    view.getContext().startActivity(
                            new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    return true;
                } else {
                    return false;
                }
            }
        });
        alert.setView(wv);
        alert.setNegativeButton("Okay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();

            }
        });

        AlertDialog alert11 = alert.create();
        alert11.show();

        Button buttonbackground = alert11.getButton(DialogInterface.BUTTON_NEGATIVE);
        buttonbackground.setBackgroundColor(getColor(R.color.solid_light_color_for_header));
        buttonbackground.setTextColor(Color.WHITE );

    }

    private void systemInfoInflater() {

        LinearLayout layoutMiscellaneous = findViewById(R.id.layoutMiscellaneous);
        final BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(layoutMiscellaneous);

        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });

        systemInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    aboutRootLayout.setVisibility(View.GONE);
                    phoneInfoRootLayout.setVisibility(View.VISIBLE);
                    actionBar.setVisibility(View.INVISIBLE);
                    if (Build.VERSION.SDK_INT >= 21)
                        getWindow().setNavigationBarColor(getResources().getColor(R.color.solid_light_color));

                } else {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    actionBar.setVisibility(View.VISIBLE);
                    if (Build.VERSION.SDK_INT >= 21)
                        getWindow().setNavigationBarColor(getResources().getColor(R.color.dark_blue));
                }
            }
        });
        closeArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                actionBar.setVisibility(View.VISIBLE);
                if (Build.VERSION.SDK_INT >= 21)
                    getWindow().setNavigationBarColor(getResources().getColor(R.color.dark_blue));
            }
        });
        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

                    phoneInfoRootLayout.setVisibility(View.GONE);
                    aboutRootLayout.setVisibility(View.VISIBLE);
                    actionBar.setVisibility(View.INVISIBLE);
                    if (Build.VERSION.SDK_INT >= 21)
                        getWindow().setNavigationBarColor(getResources().getColor(R.color.solid_light_color));

                } else {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    actionBar.setVisibility(View.VISIBLE);
                    if (Build.VERSION.SDK_INT >= 21)
                        getWindow().setNavigationBarColor(getResources().getColor(R.color.dark_blue));

                }
            }
        });

    }

    private void systemInfoInitializer() {
        textValManufacturer.setText(Build.MANUFACTURER);
        textValModel.setText(Build.MODEL);
        textValAndroidVersion.setText(AndroidVersion.getAndroidCodeNames(Build.VERSION.SDK_INT));
        textValCPU.setText(Build.BOARD);
        textValSecurityPatch.setText(Build.VERSION.SECURITY_PATCH);
        textValBaseBandVersion.setText(Build.getRadioVersion());
        textValBootloader.setText(Build.BOOTLOADER);
        textValBuild.setText(getOSBuildNumber());

        TextView phoneDetails = findViewById(R.id.phoneDetails);

    }

    private static String getOSBuildNumber() {
        String osBuildNumber = Build.FINGERPRINT;  //"google/shamu/shamu:5.1.1/LMY48Y/2364368:user/release-keys”
        final String forwardSlash = "/";
        String osReleaseVersion = Build.VERSION.RELEASE + forwardSlash;
        try {
            osBuildNumber = osBuildNumber.substring(osBuildNumber.indexOf(osReleaseVersion));  //"5.1.1/LMY48Y/2364368:user/release-keys”
            osBuildNumber = osBuildNumber.replace(osReleaseVersion, "");  //"LMY48Y/2364368:user/release-keys”
            osBuildNumber = osBuildNumber.substring(0, osBuildNumber.indexOf(forwardSlash)); //"LMY48Y"
        } catch (Exception e) {
            Log.e("getOSBuildNumber", "Exception while parsing - " + e.getMessage());
        }

        return osBuildNumber;
    }
    public boolean isItFirstTime() {
        sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        sharedEditor = sharedPreferences.edit();

        if (sharedPreferences.getBoolean("firstTime", true)) {
            sharedEditor.putBoolean("firstTime", false);
            sharedEditor.commit();
            sharedEditor.apply();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonLinkedin:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.linkedin.com/in/anujmutha")));
                break;

            case R.id.buttonGithub:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/AnujMutha")));
                break;
            case R.id.buttonInstgram:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/anuj.mutha")));
                break;
            case R.id.buttonFacebook:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/anuj.mutha.31")));
                break;
            case R.id.buttonTwitter:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/anuj_mutha")));
                break;
            case R.id.buttonLink:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.anujmutha.com")));
                break;
            case R.id.buttonMail:
                Intent email = new Intent(Intent.ACTION_SEND);
                email.putExtra(Intent.EXTRA_EMAIL, new String[]{ "business@anujmutha.com"});
                email.putExtra(Intent.EXTRA_SUBJECT, "Inquiry/Suggestion/Feedback");
                email.putExtra(Intent.EXTRA_TEXT,"Hello Developer   ,\n");
                email.setType("message/rfc822");
                startActivity(email);
                break;
            case R.id.buttonFirmLink:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.muthadevelopers.com")));
                break;
            case R.id.rateUsButton:
                try
                {
                    Intent rateIntent = rateIntentForUrl("market://details");
                    startActivity(rateIntent);
                }
                catch (ActivityNotFoundException e)
                {
                    Intent rateIntent = rateIntentForUrl("https://play.google.com/store/apps/details");
                    startActivity(rateIntent);
                }
                break;
            default:

        }
    }
    Intent rateIntentForUrl(String url)
    {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("%s?id=%s", url, getPackageName())));
        Toast.makeText(this, "Package name : "+getPackageName(), Toast.LENGTH_SHORT).show();
        int flags = Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_MULTIPLE_TASK;
        if (Build.VERSION.SDK_INT >= 21)
        {
            flags |= Intent.FLAG_ACTIVITY_NEW_DOCUMENT;
        }
        else
        {
            //noinspection deprecation
            flags |= Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET;
        }
        intent.addFlags(flags);
        return intent;
    }



}

