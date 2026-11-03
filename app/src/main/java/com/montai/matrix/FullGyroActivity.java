package com.montai.matrix;

import android.app.Activity;
import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.hardware.*;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import java.util.Locale;

/** Full-screen gyro test: rotation-vector orientation drives a perspective 3D grid. */
public class FullGyroActivity extends Activity implements SensorEventListener {
    private SensorManager sm;
    private Sensor gyro, rotation;
    private float gx, gy, gz, yaw, pitch, roll;
    private float baseYaw, basePitch, baseRoll;
    private boolean calibrated = false;
    private TextView status, legend, readout;
    private ScopeView scope;
    private long samples;
    private final float[][] trace = new float[3][180];
    private int head, count;

    private int dp(float v) { return (int)(v * getResources().getDisplayMetrics().density + .5f); }
    private TextView txt(String s, int size, int color) {
        TextView t = new TextView(this); t.setText(s); t.setTextSize(size);
        t.setTextColor(color); t.setTypeface(Typeface.MONOSPACE, Typeface.BOLD); return t;
    }
    private GradientDrawable panel() {
        GradientDrawable d = new GradientDrawable(); d.setColor(Color.rgb(5,17,14));
        d.setCornerRadius(dp(12)); d.setStroke(dp(1), Color.rgb(21,92,68)); return d;
    }
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(Color.rgb(3,8,10));
        getWindow().setNavigationBarColor(Color.rgb(3,8,10));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        sm = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        gyro = sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        rotation = sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(12),dp(8),dp(12),dp(8)); root.setBackgroundColor(Color.rgb(3,8,10));
        LinearLayout header = new LinearLayout(this); header.setGravity(Gravity.CENTER_VERTICAL);
        TextView title = txt("MONTAI // FULL GYRO TEST",15,Color.rgb(224,255,244));
        header.addView(title,new LinearLayout.LayoutParams(0,-2,1));
        Button recenter = btn("RECENTER"); recenter.setOnClickListener(v -> { baseYaw=yaw; basePitch=pitch; baseRoll=roll; calibrated=true; });
        header.addView(recenter); root.addView(header);
        status = txt("● INITIALIZING SENSORS",10,Color.rgb(0,245,160)); root.addView(status);

        LinearLayout body = new LinearLayout(this); body.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams bodyP = new LinearLayout.LayoutParams(-1,0,1); bodyP.topMargin=dp(6); root.addView(body,bodyP);
        scope = new ScopeView(this);
        LinearLayout.LayoutParams sceneP = new LinearLayout.LayoutParams(0,-1,1.55f); sceneP.rightMargin=dp(8); body.addView(scope,sceneP);

        LinearLayout side = new LinearLayout(this); side.setOrientation(LinearLayout.VERTICAL); side.setPadding(dp(10),dp(8),dp(10),dp(8)); side.setBackground(panel());
        body.addView(side,new LinearLayout.LayoutParams(0,-1,1));
        side.addView(txt("LIVE ANGULAR VELOCITY",10,Color.rgb(0,245,160)));
        legend = txt("X  BLUE   //  LEFT-RIGHT\nY  GREEN  //  UP-DOWN\nZ  WHITE  //  TWIST",10,Color.rgb(190,230,215));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.topMargin=dp(8);side.addView(legend,lp);
        View divider=new View(this);divider.setBackgroundColor(Color.rgb(25,85,65));lp=new LinearLayout.LayoutParams(-1,dp(1));lp.topMargin=dp(10);lp.bottomMargin=dp(10);side.addView(divider,lp);
        readout=txt("X  +0.000 rad/s\nY  +0.000 rad/s\nZ  +0.000 rad/s\n\nYAW    0.0°\nPITCH  0.0°\nROLL   0.0°",11,Color.rgb(220,255,240));side.addView(readout);
        TextView hint=txt("MOVE PHONE\nTilt and rotate the device.\n\nGRID = orientation\nTRACE = angular speed\n\nUse RECENTER to set the current pose as zero.",9,Color.rgb(140,195,174));lp=new LinearLayout.LayoutParams(-1,0,1);lp.topMargin=dp(12);side.addView(hint,lp);
        Button close=btn("EXIT FULL TEST");close.setOnClickListener(v -> finish());side.addView(close);
        setContentView(root);
        if(gyro==null) status.setText("● NO HARDWARE GYROSCOPE DETECTED");
        else status.setText("● GYROSCOPE READY  //  "+gyro.getName());
        if(rotation==null) status.append("\nRotation-vector sensor unavailable; grid will use gyro integration only.");
    }
    private Button btn(String s){Button b=new Button(this);b.setText(s);b.setAllCaps(false);b.setTextSize(9);b.setTextColor(Color.rgb(190,255,225));b.setTypeface(Typeface.MONOSPACE,Typeface.BOLD);b.setBackground(panel());return b;}
    @Override protected void onResume(){super.onResume();if(gyro!=null)sm.registerListener(this,gyro,SensorManager.SENSOR_DELAY_GAME);if(rotation!=null)sm.registerListener(this,rotation,SensorManager.SENSOR_DELAY_GAME);}
    @Override protected void onPause(){super.onPause();sm.unregisterListener(this);}
    @Override public void onAccuracyChanged(Sensor s,int a){}
    @Override public void onSensorChanged(SensorEvent e){
        if(e.sensor.getType()==Sensor.TYPE_GYROSCOPE){gx=e.values[0];gy=e.values[1];gz=e.values[2];samples++;trace[0][head]=gx;trace[1][head]=gy;trace[2][head]=gz;head=(head+1)%180;if(count<180)count++;}
        else if(e.sensor.getType()==Sensor.TYPE_ROTATION_VECTOR){float[] r=new float[9],o=new float[3];SensorManager.getRotationMatrixFromVector(r,e.values);SensorManager.getOrientation(r,o);yaw=o[0];pitch=o[1];roll=o[2];if(!calibrated){baseYaw=yaw;basePitch=pitch;baseRoll=roll;calibrated=true;}}
        float dy=wrap(yaw-baseYaw),dp=wrap(pitch-basePitch),dr=wrap(roll-baseRoll);
        readout.setText(String.format(Locale.US,"X  %+.3f rad/s\nY  %+.3f rad/s\nZ  %+.3f rad/s\n\nYAW    %+.1f°\nPITCH  %+.1f°\nROLL   %+.1f°",gx,gy,gz,Math.toDegrees(dy),Math.toDegrees(dp),Math.toDegrees(dr)));
        scope.setPose(dy,dp,dr,trace,head,count);
    }
    private float wrap(float a){while(a>Math.PI)a-=2*(float)Math.PI;while(a< -Math.PI)a+=2*(float)Math.PI;return a;}

    private class ScopeView extends View {
        Paint p=new Paint(Paint.ANTI_ALIAS_FLAG); float yaw,pitch,roll;float[][] data;int head,count;
        ScopeView(Context c){super(c);setBackground(panel());}
        void setPose(float y,float pi,float r,float[][] d,int h,int n){yaw=y;pitch=pi;roll=r;data=d;head=h;count=n;postInvalidateOnAnimation();}
        @Override protected void onDraw(Canvas c){super.onDraw(c);int w=getWidth(),h=getHeight();c.drawColor(Color.rgb(4,14,12));
            p.setStyle(Paint.Style.FILL);p.setColor(Color.rgb(0,245,160));p.setTypeface(Typeface.MONOSPACE);p.setTextSize(dp(11));c.drawText("LIVE ENVIRONMENT // 3D ORIENTATION",dp(12),dp(22),p);
            // Perspective floor grid. Rotation-vector angles transform the virtual environment opposite the phone.
            float cx=w*.5f, horizon=h*.43f; c.save();c.clipRect(0,dp(32),w,h);
            p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(1));p.setColor(Color.rgb(19,92,68));
            for(int i=-10;i<=10;i++){float x=cx+i*w*.055f;Path v=new Path();v.moveTo(cx+(x-cx)*.08f,horizon);v.lineTo(cx+(x-cx)*2.8f+(float)Math.sin(yaw)*w*.18f,h+(float)Math.sin(roll)*h*.15f);c.drawPath(v,p);}
            for(int j=1;j<=14;j++){float t=j/14f;float yy=horizon+(h-horizon)*(t*t);float shift=(float)Math.sin(pitch)*w*.22f*(1-t);c.drawLine(0,yy+shift,w,yy-shift,p);}
            // horizon guides react to yaw and roll to create a stable spatial reference illusion
            p.setColor(Color.rgb(0,170,115));p.setStrokeWidth(dp(2));float hx=(float)Math.sin(yaw)*w*.16f,hy=(float)Math.sin(roll)*h*.13f;c.drawLine(0,horizon+hy,w,horizon-hy,p);
            c.restore();
            // compact live graph in lower overlay
            float top=h-dp(112), bottom=h-dp(16), left=dp(12), right=w-dp(12);p.setColor(Color.rgb(7,24,19));p.setStyle(Paint.Style.FILL);c.drawRoundRect(left,top,right,bottom,dp(8),dp(8),p);
            p.setColor(Color.rgb(25,75,58));p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1);for(int i=1;i<4;i++)c.drawLine(left,top+(bottom-top)*i/4f,right,top+(bottom-top)*i/4f,p);
            int[] colors={Color.rgb(80,190,255),Color.rgb(0,245,160),Color.rgb(225,255,238)};String[] names={"X","Y","Z"};
            for(int a=0;a<3;a++){p.setColor(colors[a]);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(1.5f));Path path=new Path();for(int i=0;i<count;i++){int idx=(head-count+i+180)%180;float px=left+(right-left)*i/179f;float py=(top+bottom)/2f-data[a][idx]*dp(13);py=Math.max(top+dp(5),Math.min(bottom-dp(5),py));if(i==0)path.moveTo(px,py);else path.lineTo(px,py);}c.drawPath(path,p);p.setStyle(Paint.Style.FILL);p.setTextSize(dp(9));c.drawText(names[a],left+dp(8+a*22),bottom-dp(5),p);}
        }
    }
}
