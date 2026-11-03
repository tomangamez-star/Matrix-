package com.montai.matrix;

import android.app.Activity;
import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.hardware.*;
import android.view.*;
import android.widget.*;
import java.util.Locale;

public class GyroscopeActivity extends Activity implements SensorEventListener {
    private SensorManager manager;
    private Sensor gyro;
    private TextView state, values, stats, orientation;
    private GraphView graph;
    private long samples = 0, lastTimestamp = 0;
    private double sumInterval = 0, sumIntervalSq = 0;
    private long minInterval = Long.MAX_VALUE, maxInterval = 0;
    private float x, y, z, magnitude;
    private boolean running = false;

    private int dp(float v) { return (int)(v * getResources().getDisplayMetrics().density + .5f); }
    private TextView label(String s, int size, String color) {
        TextView t = new TextView(this); t.setText(s); t.setTextSize(size);
        t.setTextColor(Color.parseColor(color)); t.setTypeface(Typeface.MONOSPACE);
        return t;
    }
    private GradientDrawable panel() {
        GradientDrawable d = new GradientDrawable(); d.setColor(Color.rgb(7,17,15));
        d.setCornerRadius(dp(13)); d.setStroke(dp(1), Color.rgb(21,84,63)); return d;
    }
    private void addPanel(LinearLayout root, View child, int top) {
        child.setPadding(dp(14),dp(13),dp(14),dp(13)); child.setBackground(panel());
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1,-2);
        p.topMargin = dp(top); root.addView(child,p);
    }
    private Button action(String text) {
        Button b = new Button(this); b.setText(text); b.setAllCaps(false); b.setTextSize(11);
        b.setTextColor(Color.rgb(191,255,229)); b.setTypeface(Typeface.MONOSPACE,Typeface.BOLD);
        b.setBackground(panel()); return b;
    }

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(Color.rgb(3,8,10));
        getWindow().setNavigationBarColor(Color.rgb(3,8,10));
        manager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        gyro = manager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18),dp(16),dp(18),dp(24)); root.setBackgroundColor(Color.rgb(3,8,10));
        scroll.addView(root); setContentView(scroll);

        root.addView(label("MONTAI  /  SENSOR LAB",21,"#E8FFF7"));
        root.addView(label("GYROSCOPE DIAGNOSTICS  //  LIVE",10,"#00F5A0"));
        TextView rule = label("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",12,"#14634B"); root.addView(rule);

        state = label("",12,"#00F5A0"); addPanel(root,state,dp(12));
        graph = new GraphView(this);
        LinearLayout.LayoutParams gp = new LinearLayout.LayoutParams(-1,dp(210)); gp.topMargin=dp(12); root.addView(graph,gp);
        TextView legend = label("X  BLUE  //  LEFT-RIGHT       Y  GREEN  //  UP-DOWN       Z  WHITE  //  TWIST",10,"#BFEEDD");
        legend.setPadding(dp(8),dp(8),dp(8),dp(8)); root.addView(legend);

        values = label("X  0.000 rad/s\nY  0.000 rad/s\nZ  0.000 rad/s\nTOTAL  0.000 rad/s",14,"#C8EDE0");
        addPanel(root,values,12);
        stats = label("SAMPLES       0\nRATE           waiting for sensor\nINTERVAL       —\nJITTER         —",12,"#C8EDE0");
        addPanel(root,stats,10);
        orientation = label("HOW TO TEST\n1. Keep the phone still and watch the resting readings.\n2. Slowly rotate it around each axis.\n3. Make a few quick turns and watch the graph respond.\n4. Stop moving it; readings should settle near zero.\n\nValues are angular velocity, not tilt angle.",11,"#A8DCC8");
        addPanel(root,orientation,10);

        Button full = action("◎  OPEN FULL 3D GYROSCOPE TEST");
        full.setOnClickListener(v -> startActivity(new Intent(this, FullGyroActivity.class)));
        LinearLayout.LayoutParams fp=new LinearLayout.LayoutParams(-1,dp(52));fp.topMargin=dp(12);root.addView(full,fp);
        LinearLayout buttons = new LinearLayout(this); buttons.setOrientation(LinearLayout.HORIZONTAL);
        Button reset = action("RESET STATS"); reset.setOnClickListener(v -> resetStats());
        Button back = action("BACK"); back.setOnClickListener(v -> finish());
        buttons.addView(reset,new LinearLayout.LayoutParams(0,dp(50),1));
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(0,dp(50),1); bp.leftMargin=dp(8); buttons.addView(back,bp);
        LinearLayout.LayoutParams bpar = new LinearLayout.LayoutParams(-1,-2); bpar.topMargin=dp(12); root.addView(buttons,bpar);

        if (gyro == null) { state.setText("● GYROSCOPE NOT DETECTED\nThis device does not report a hardware gyroscope."); values.setText("No gyroscope data available."); }
        else state.setText("● GYROSCOPE DETECTED\n" + gyro.getName() + "\nVendor: " + gyro.getVendor());
    }
    @Override protected void onResume() { super.onResume(); if (gyro != null) { running = manager.registerListener(this,gyro,SensorManager.SENSOR_DELAY_GAME); if (!running) state.setText("● SENSOR COULD NOT BE STARTED"); } }
    @Override protected void onPause() { super.onPause(); manager.unregisterListener(this); running = false; }
    @Override public void onSensorChanged(SensorEvent e) {
        if (e.sensor.getType()!=Sensor.TYPE_GYROSCOPE) return;
        x=e.values[0]; y=e.values[1]; z=e.values[2]; magnitude=(float)Math.sqrt(x*x+y*y+z*z); samples++;
        if (lastTimestamp != 0) { long interval=(e.timestamp-lastTimestamp)/1000000L; if(interval>0) { sumInterval += interval; sumIntervalSq += (double)interval*interval; minInterval=Math.min(minInterval,interval); maxInterval=Math.max(maxInterval,interval); } }
        lastTimestamp=e.timestamp; double avg=samples>1?sumInterval/(samples-1):0; double variance=samples>2?(sumIntervalSq/(samples-1))-avg*avg:0; double jitter=Math.sqrt(Math.max(0,variance));
        values.setText(String.format(Locale.US,"X  %+.4f rad/s\nY  %+.4f rad/s\nZ  %+.4f rad/s\nTOTAL  %.4f rad/s",x,y,z,magnitude));
        stats.setText(String.format(Locale.US,"SAMPLES       %d\nAVG INTERVAL  %.2f ms\nMIN / MAX     %s / %s ms\nTIMING JITTER %.2f ms",samples,avg,minInterval==Long.MAX_VALUE?"—":String.valueOf(minInterval),maxInterval==0?"—":String.valueOf(maxInterval),jitter));
        graph.push(x,y,z);
    }
    @Override public void onAccuracyChanged(Sensor sensor,int accuracy) { state.setText("● GYROSCOPE ACTIVE  //  accuracy: " + accuracy); }
    private void resetStats() { samples=0; lastTimestamp=0; sumInterval=0; sumIntervalSq=0; minInterval=Long.MAX_VALUE; maxInterval=0; graph.clear(); stats.setText("SAMPLES       0\nRATE           waiting for sensor\nINTERVAL       —\nJITTER         —"); }

    class GraphView extends View {
        Paint p=new Paint(Paint.ANTI_ALIAS_FLAG); float[][] data=new float[3][150]; int count=0, head=0;
        int[] colors={Color.rgb(80,190,255),Color.rgb(0,245,160),Color.rgb(220,255,230)};
        GraphView(Context c){super(c);}
        void push(float a,float b,float c){data[0][head]=a;data[1][head]=b;data[2][head]=c;head=(head+1)%150;if(count<150)count++;postInvalidate();}
        void clear(){count=0;head=0;invalidate();}
        @Override protected void onDraw(Canvas c) { c.drawColor(Color.rgb(5,13,11)); p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(1));p.setColor(Color.rgb(24,75,58));for(int i=1;i<4;i++)c.drawLine(0,getHeight()*i/4f,getWidth(),getHeight()*i/4f,p);c.drawLine(0,getHeight()/2f,getWidth(),getHeight()/2f,p);p.setStyle(Paint.Style.FILL);p.setTextSize(dp(10));p.setTypeface(Typeface.MONOSPACE);p.setColor(Color.rgb(0,245,160));c.drawText("ANGULAR VELOCITY  //  LIVE TRACE",dp(10),dp(17),p);float scale=dp(34);for(int axis=0;axis<3;axis++){p.setColor(colors[axis]);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(1.6f));Path path=new Path();for(int i=0;i<count;i++){int idx=(head-count+i+150)%150;float px=(count<=1?0:(getWidth()-dp(12))*i/(float)(count-1))+dp(6);float py=getHeight()/2f-(data[axis][idx]*scale);py=Math.max(dp(25),Math.min(getHeight()-dp(8),py));if(i==0)path.moveTo(px,py);else path.lineTo(px,py);}c.drawPath(path,p);}}
    }
}
