package com.allhack.cheats;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class FloatingService extends Service {
    
    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean isEspRunning = true;
    private WindowManager windowManager;
    private View floatingView;
    private WindowManager.LayoutParams params;
    private float initialX, initialY;
    private int initialTouchX, initialTouchY;

    private final Runnable espLoop = new Runnable() {
        @Override
        public void run() {
            if (!isEspRunning) return;
            DisplayMetrics dm = getResources().getDisplayMetrics();
            NativeBridge.updateESP(dm.widthPixels, dm.heightPixels);
            handler.postDelayed(this, 16);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Toast.makeText(this, "PAINEL INICIADO!", Toast.LENGTH_SHORT).show();
        try {
            CreateFloatingMenu();
            handler.post(espLoop);
        } catch (Exception e) {
            Toast.makeText(this, "ERRO: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void CreateFloatingMenu() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundColor(Color.parseColor("#CC1E1E1E"));
        layout.setPadding(30, 30, 30, 30);

        TextView title = new TextView(this);
        title.setText("OLLIE MENU");
        title.setTextColor(Color.GREEN);
        title.setTextSize(18);
        layout.addView(title);

        Button btnEsp = new Button(this);
        btnEsp.setText("ESP BOX");
        layout.addView(btnEsp);

        Button btnAim = new Button(this);
        btnAim.setText("AIMBOT");
        layout.addView(btnAim);

        floatingView = layout;

        int layoutFlag;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            layoutFlag = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutFlag = WindowManager.LayoutParams.TYPE_PHONE;
        }

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT, 
                WindowManager.LayoutParams.WRAP_CONTENT,
                layoutFlag, 
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, 
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 100;
        params.y = 200;

        windowManager.addView(floatingView, params);

        btnEsp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NativeBridge.setEspBox(true);
                Toast.makeText(FloatingService.this, "ESP LIGADO", Toast.LENGTH_SHORT).show();
            }
        });

        btnAim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NativeBridge.setAimCollider(true);
                Toast.makeText(FloatingService.this, "AIM LIGADO", Toast.LENGTH_SHORT).show();
            }
        });

        floatingView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = (int) event.getRawX();
                        initialTouchY = (int) event.getRawY();
                        return false;
                    case MotionEvent.ACTION_MOVE:
                        params.x = (int) (initialX + (int) event.getRawX() - initialTouchX);
                        params.y = (int) (initialY + (int) event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(floatingView, params);
                        return true;
                }
                return false;
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) { 
        return START_STICKY; 
    }

    @Override
    public void onDestroy() {
        isEspRunning = false;
        handler.removeCallbacks(espLoop);
        if (floatingView != null) {
            windowManager.removeView(floatingView);
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) { 
        return null; 
    }
}
