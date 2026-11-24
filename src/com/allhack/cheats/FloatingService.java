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
import android.widget.Toast;

public class FloatingService extends Service {
    private Handler handler = new Handler(Looper.getMainLooper());
    private WindowManager windowManager;
    private View floatingIcon;
    private LinearLayout menuLayout;
    private WindowManager.LayoutParams paramsIcon, paramsMenu;
    private boolean isMenuOpen = false;
    private boolean isEspRunning = true;

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
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        createFloatingIcon();
        createMenu();
        handler.post(espLoop);
    }

    private void createFloatingIcon() {
        Button icon = new Button(this);
        icon.setText("OLLIE");
        icon.setBackgroundColor(Color.parseColor("#AA000000"));
        icon.setTextColor(Color.GREEN);

        int layoutFlag = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) 
            ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY 
            : WindowManager.LayoutParams.TYPE_PHONE;

        paramsIcon = new WindowManager.LayoutParams(
                150, 150, layoutFlag,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        paramsIcon.gravity = Gravity.TOP | Gravity.START;
        paramsIcon.x = 50;
        paramsIcon.y = 200;

        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isMenuOpen) {
                    menuLayout.setVisibility(View.GONE);
                } else {
                    menuLayout.setVisibility(View.VISIBLE);
                }
                isMenuOpen = !isMenuOpen;
            }
        });

        // Arrastar o icone
        icon.setOnTouchListener(new View.OnTouchListener() {
            float initialX, initialY;
            int initialTouchX, initialTouchY;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = paramsIcon.x;
                        initialY = paramsIcon.y;
                        initialTouchX = (int) event.getRawX();
                        initialTouchY = (int) event.getRawY();
                        return false;
                    case MotionEvent.ACTION_MOVE:
                        paramsIcon.x = (int) (initialX + (int) event.getRawX() - initialTouchX);
                        paramsIcon.y = (int) (initialY + (int) event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(floatingIcon, paramsIcon);
                        return true;
                }
                return false;
            }
        });

        floatingIcon = icon;
        windowManager.addView(floatingIcon, paramsIcon);
    }

    private void createMenu() {
        menuLayout = new LinearLayout(this);
        menuLayout.setOrientation(LinearLayout.VERTICAL);
        menuLayout.setBackgroundColor(Color.parseColor("#CC1E1E1E"));
        menuLayout.setPadding(30, 30, 30, 30);
        menuLayout.setVisibility(View.GONE); // Começa escondido

        Button btnEsp = new Button(this);
        btnEsp.setText("ESP BOX");
        menuLayout.addView(btnEsp);

        Button btnAim = new Button(this);
        btnAim.setText("AIMBOT");
        menuLayout.addView(btnAim);

        int layoutFlag = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) 
            ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY 
            : WindowManager.LayoutParams.TYPE_PHONE;

        paramsMenu = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layoutFlag,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        paramsMenu.gravity = Gravity.CENTER;

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

        windowManager.addView(menuLayout, paramsMenu);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) { return START_STICKY; }

    @Override
    public void onDestroy() {
        isEspRunning = false;
        handler.removeCallbacks(espLoop);
        if (floatingIcon != null) windowManager.removeView(floatingIcon);
        if (menuLayout != null) windowManager.removeView(menuLayout);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}
