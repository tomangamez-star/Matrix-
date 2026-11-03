package com.montai.matrix;

import android.app.Activity;
import android.os.Bundle;
import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import android.os.Build;
import android.os.BatteryManager;
import android.content.Intent;
import android.content.IntentFilter;
import java.util.*;

public class MainActivity extends Activity {
    LinearLayout root, terminal;
    MatrixView matrix;
    TextView output, status;
    boolean rainOn = true, soundless = true;
    final String GREEN = "#00F5A0";
    int dp(float n) { return (int)(n * getResources().getDisplayMetrics().density + .5f); }

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(Color.rgb(3,8,10));
        getWindow().setNavigationBarColor(Color.rgb(3,8,10));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        buildUI();
    }

    TextView text(String s, int size, String color) {
        TextView t = new TextView(this); t.setText(s); t.setTextSize(size);
        t.setTextColor(Color.parseColor(color)); t.setTypeface(Typeface.MONOSPACE);
        return t;
    }
    GradientDrawable bg(String color, String stroke) {
        GradientDrawable d = new GradientDrawable(); d.setColor(Color.parseColor(color));
        d.setCornerRadius(dp(12)); d.setStroke(dp(1), Color.parseColor(stroke)); return d;
    }
    void buildUI() {
        ScrollView scroll = new ScrollView(this);
        root = new LinearLayout(this); root.setOrientation(1);
        root.setPadding(dp(18), dp(16), dp(18), dp(24)); root.setBackgroundColor(Color.rgb(3,8,10));
        scroll.addView(root); setContentView(scroll);

        LinearLayout head = new LinearLayout(this); head.setGravity(Gravity.CENTER_VERTICAL);
        TextView logo = text("Ｍ", 30, GREEN); logo.setPadding(0,0,dp(10),0);
        head.addView(logo);
        LinearLayout names = new LinearLayout(this); names.setOrientation(1);
        names.addView(text("MONTAI", 23, "#E8FFF7"));
        names.addView(text("M A T R I X   /   MOBILE TERMINAL", 10, GREEN));
        head.addView(names); root.addView(head);
        TextView line = text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", 12, "#14634B");
        root.addView(line);

        matrix = new MatrixView(this); 
        LinearLayout.LayoutParams mp = new LinearLayout.LayoutParams(-1, dp(190));
        mp.topMargin = dp(12); root.addView(matrix, mp);

        status = text("● SYSTEM ONLINE     ENCRYPTION: SIMULATED", 10, GREEN);
        status.setPadding(dp(10),dp(12),dp(10),dp(12)); status.setBackground(bg("#071411","#124A39"));
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(-1,-2); sp.topMargin=dp(10); root.addView(status,sp);

        root.addView(section("DEVICE SNAPSHOT"));
        root.addView(infoCard(deviceInfo()));

        root.addView(section("COMMAND CENTRE"));
        Button dashboard = button("▣  OPEN DEVICE COMMAND CENTRE");
        dashboard.setOnClickListener(v -> startActivity(new Intent(this, DeviceDashboardActivity.class)));
        root.addView(dashboard);

        Button gyro = button("◎  OPEN GYROSCOPE LAB");
        gyro.setOnClickListener(v -> startActivity(new Intent(this, GyroscopeActivity.class)));
        root.addView(gyro);

        root.addView(section("COMMAND DECK"));
        String[][] cmds = {
            {"help","Show available commands"},{"scan","Run a local device snapshot"},{"whoami","Display terminal identity"},
            {"matrix","Toggle digital rain"},{"clear","Clear terminal output"},{"about","About this simulator"}
        };
        for (String[] c : cmds) {
            Button b = button("$ " + c[0] + "     // " + c[1]);
            b.setOnClickListener(v -> runCommand(c[0]));
            root.addView(b);
        }

        root.addView(section("LIVE TERMINAL"));
        output = text("> MONTAI MATRIX initialized.\n> Type a command from the deck above.\n> No root access. No network access. No files modified.", 12, "#B9FCE0");
        output.setPadding(dp(14),dp(14),dp(14),dp(14)); output.setMinHeight(dp(130));
        output.setBackground(bg("#050D0B","#15543F")); root.addView(output);

        Button refresh = button("↻  REFRESH DEVICE SNAPSHOT");
        refresh.setOnClickListener(v -> runCommand("scan"));
        root.addView(refresh);
        TextView foot = text("MONTAI // BUILT FOR THE CURIOUS", 10, "#477D68");
        foot.setGravity(Gravity.CENTER); foot.setPadding(0,dp(22),0,0); root.addView(foot);
    }

    TextView section(String s) {
        TextView t=text(s,11,GREEN); t.setPadding(0,dp(18),0,dp(8)); return t;
    }
    TextView infoCard(String s) {
        TextView t=text(s,12,"#C8EDE0"); t.setPadding(dp(14),dp(12),dp(14),dp(12));
        t.setBackground(bg("#07110F","#15543F")); return t;
    }
    Button button(String label) {
        Button b=new Button(this); b.setText(label); b.setTextSize(11); b.setAllCaps(false);
        b.setTextColor(Color.parseColor("#BFFFE5")); b.setTypeface(Typeface.MONOSPACE,Typeface.BOLD);
        b.setGravity(Gravity.CENTER_VERTICAL); b.setPadding(dp(12),0,dp(12),0);
        b.setBackground(bg("#081712","#176047"));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(48)); p.topMargin=dp(7);
        b.setLayoutParams(p); return b;
    }
    String deviceInfo() {
        String model=Build.MANUFACTURER+" "+Build.MODEL;
        String ver=Build.VERSION.RELEASE+" (API "+Build.VERSION.SDK_INT+")";
        Intent i=registerReceiver(null,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level=-1; if(i!=null) level=i.getIntExtra(BatteryManager.EXTRA_LEVEL,-1);
        return "DEVICE   "+model+"\nANDROID  "+ver+"\nBATTERY  "+(level>=0?level+"%":"Unavailable")+
            "\nAPP      MONTAI MATRIX 1.0\nMODE     OFFLINE / READ-ONLY";
    }
    void runCommand(String cmd) {
        String result;
        switch(cmd) {
            case "help": result="COMMANDS: help · scan · whoami · matrix · clear · about\nAll commands are local visual simulations."; break;
            case "scan": result="LOCAL SNAPSHOT COMPLETE\n"+deviceInfo()+"\n\nNo private files were scanned."; break;
            case "whoami": result="IDENTITY: MONTAI GUEST\nPRIVILEGE: STANDARD APP\nROOT: NOT REQUESTED\nNETWORK: DISABLED"; break;
            case "matrix": rainOn=!rainOn; matrix.setRain(rainOn); result="DIGITAL RAIN: "+(rainOn?"ENABLED":"PAUSED"); break;
            case "clear": output.setText("> Terminal cleared.\n> Awaiting command."); return;
            default: result="MONTAI MATRIX is an offline cyber-themed terminal simulator.\nNo hacking or device modification occurs."; break;
        }
        output.append("\n\n$ "+cmd+"\n"+result);
    }

    class MatrixView extends View {
        Paint p=new Paint(3); Random random=new Random(); float[] y; String chars="01アイウエオカキクケコサシスセソ";
        boolean active=true; int cols; float cell;
        MatrixView(Context c) { super(c); setLayerType(View.LAYER_TYPE_SOFTWARE,null); }
        void setRain(boolean b){active=b; invalidate();}
        @Override protected void onDraw(Canvas c) {
            super.onDraw(c); int w=getWidth(),h=getHeight();
            c.drawColor(Color.rgb(2,13,10));
            p.setColor(Color.rgb(0,70,48)); p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(dp(1));
            c.drawRoundRect(0,0,w,h,dp(12),dp(12),p); p.setStyle(Paint.Style.FILL);
            if(cell==0) {cell=dp(15); cols=Math.max(1,(int)(w/cell)); y=new float[cols]; for(int k=0;k<cols;k++)y[k]=random.nextInt(Math.max(1,h));}
            p.setTextSize(dp(12)); p.setTypeface(Typeface.MONOSPACE);
            for(int k=0;k<cols;k++) {
                for(int j=0;j<7;j++) {
                    int yy=(int)y[k]-j*dp(17); if(yy<dp(12)||yy>h-dp(8))continue;
                    p.setColor(j==0?Color.rgb(190,255,220):Color.rgb(0,Math.max(65,190-j*18),Math.max(35,115-j*10)));
                    c.drawText(""+chars.charAt(random.nextInt(chars.length())),k*cell,yy,p);
                }
                if(active) { y[k]+=dp(2)+(k%4); if(y[k]>h+dp(100))y[k]=-random.nextInt(h+1); }
            }
            p.setColor(Color.rgb(0,245,160)); p.setTextSize(dp(10));
            c.drawText(active?"DIGITAL RAIN // LIVE":"DIGITAL RAIN // PAUSED",dp(12),h-dp(12),p);
            if(active) postInvalidateDelayed(75);
        }
    }
}
