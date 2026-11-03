package com.montai.matrix;

import android.app.Activity;
import android.app.ActivityManager;
import android.os.Bundle;
import android.os.BatteryManager;
import android.os.Build;
import android.os.StatFs;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.view.Gravity;
import android.widget.*;
import android.view.View;
import android.view.ViewGroup;
import java.io.File;
import java.util.List;
import java.util.Locale;

public class DeviceDashboardActivity extends Activity {
    LinearLayout root, cardsHolder;
    int dp(float n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
    TextView text(String s,int size,String color){
        TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(Color.parseColor(color));
        t.setTypeface(Typeface.MONOSPACE);return t;
    }
    GradientDrawable panel(){
        GradientDrawable d=new GradientDrawable();d.setColor(Color.rgb(7,17,15));
        d.setCornerRadius(dp(13));d.setStroke(dp(1),Color.rgb(21,84,63));return d;
    }
    void card(String title,String body){
        LinearLayout box=new LinearLayout(this);box.setOrientation(1);box.setPadding(dp(14),dp(13),dp(14),dp(13));box.setBackground(panel());
        box.addView(text(title,11,"#00F5A0"));TextView detail=text(body,12,"#C8EDE0");
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.topMargin=dp(6);box.addView(detail,p);
        LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,-2);cp.topMargin=dp(10);cardsHolder.addView(box,cp);
    }
    @Override public void onCreate(Bundle b){
        super.onCreate(b);getWindow().setStatusBarColor(Color.rgb(3,8,10));getWindow().setNavigationBarColor(Color.rgb(3,8,10));
        ScrollView scroll=new ScrollView(this);root=new LinearLayout(this);root.setOrientation(1);
        root.setPadding(dp(18),dp(16),dp(18),dp(24));root.setBackgroundColor(Color.rgb(3,8,10));scroll.addView(root);setContentView(scroll);
        root.addView(text("MONTAI  /  COMMAND CENTRE",20,"#E8FFF7"));
        root.addView(text("DEVICE INTELLIGENCE  //  LOCAL TELEMETRY",10,"#00F5A0"));
        cardsHolder = new LinearLayout(this); cardsHolder.setOrientation(LinearLayout.VERTICAL);
        root.addView(cardsHolder);
        refreshCards();
        Button refresh=new Button(this);refresh.setText("↻  REFRESH TELEMETRY");refresh.setAllCaps(false);refresh.setTextColor(Color.rgb(191,255,229));refresh.setBackground(panel());
        refresh.setOnClickListener(v->{cardsHolder.removeAllViews();refreshCards();});
        LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,dp(50));rp.topMargin=dp(12);root.addView(refresh,rp);
        Button back=new Button(this);back.setText("BACK TO MATRIX");back.setAllCaps(false);back.setTextColor(Color.rgb(191,255,229));back.setBackground(panel());
        back.setOnClickListener(v->finish());LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-1,dp(50));bp.topMargin=dp(8);root.addView(back,bp);
    }
    void refreshCards(){
        Intent battery=registerReceiver(null,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level=battery==null?-1:battery.getIntExtra(BatteryManager.EXTRA_LEVEL,-1);
        int scale=battery==null?100:battery.getIntExtra(BatteryManager.EXTRA_SCALE,100);
        int pct=(level>=0&&scale>0)?Math.round(level*100f/scale):-1;
        int status=battery==null?-1:battery.getIntExtra(BatteryManager.EXTRA_STATUS,-1);
        String charging=(status==BatteryManager.BATTERY_STATUS_CHARGING||status==BatteryManager.BATTERY_STATUS_FULL)?"CHARGING":"NOT CHARGING";
        card("POWER", "BATTERY       "+(pct<0?"Unavailable":pct+"%")+"\nSTATUS        "+charging);

        ActivityManager am=(ActivityManager)getSystemService(ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi=new ActivityManager.MemoryInfo();am.getMemoryInfo(mi);
        long totalRam=mi.totalMem, avail=mi.availMem, used=Math.max(0,totalRam-avail);
        card("MEMORY", "TOTAL RAM     "+fmt(totalRam)+"\nAVAILABLE     "+fmt(avail)+"\nUSED ESTIMATE "+fmt(used));

        StatFs fs=new StatFs(android.os.Environment.getDataDirectory().getPath());
        long total=fs.getTotalBytes(),free=fs.getAvailableBytes();
        card("INTERNAL STORAGE", "TOTAL         "+fmt(total)+"\nAVAILABLE     "+fmt(free)+"\nUSED          "+fmt(Math.max(0,total-free)));

        card("PROCESSOR / PLATFORM","DEVICE        "+Build.MANUFACTURER+" "+Build.MODEL+"\nANDROID       "+Build.VERSION.RELEASE+" / API "+Build.VERSION.SDK_INT+
            "\nCPU CORES     "+Runtime.getRuntime().availableProcessors()+"\nSUPPORTED ABI "+(Build.SUPPORTED_ABIS.length>0?Build.SUPPORTED_ABIS[0]:"Unknown"));

        float hz=getWindowManager().getDefaultDisplay().getRefreshRate();
        card("DISPLAY","RESOLUTION    "+getResources().getDisplayMetrics().widthPixels+" × "+getResources().getDisplayMetrics().heightPixels+
            "\nDENSITY       "+getResources().getDisplayMetrics().densityDpi+" dpi\nREFRESH RATE  "+String.format(Locale.US,"%.1f Hz",hz));

        SensorManager sm=(SensorManager)getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sensors=sm.getSensorList(Sensor.TYPE_ALL);
        StringBuilder sb=new StringBuilder();
        for(Sensor s:sensors){if(sb.length()>0)sb.append("\n");sb.append(s.getName());}
        card("SENSOR INVENTORY", "AVAILABLE SENSORS: "+sensors.size()+"\n"+(sb.length()==0?"No sensors reported":sb.toString()));
    }
    String fmt(long bytes){
        if(bytes<1024)return bytes+" B";
        double v=bytes/1024.0;String[] u={"KB","MB","GB","TB"};int i=0;
        while(v>=1024&&i<u.length-1){v/=1024;i++;}
        return String.format(Locale.US,"%.2f %s",v,u[i]);
    }
}
