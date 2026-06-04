package com.example.snooker;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private GameController gameController;
    private GameView gameView;

    // Loop Controlling
    private long lastTime;
    private boolean isGameRunning = true;
    private final Handler gameLoopHandler = new Handler(Looper.getMainLooper());

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gameView = findViewById(R.id.gameView);
        gameController = new GameController(gameView);
        // set controller to touch event
        gameView.setOnTouchListener((v, event) -> gameController.onTouchEvent(event));
        startGameLoop();
    }

    private void startGameLoop() {
        lastTime = System.nanoTime();

        gameLoopHandler.post(new Runnable() {
            @Override
            public void run() {
                if (isGameRunning) {
                    updateGame();
                    gameLoopHandler.postDelayed(this, 25);
                }
            }
        });
    }

    private void updateGame() {
        // Calculate the delta time
        long now = System.nanoTime();
        float deltaTime = (now - lastTime) / 1_000_000_000.0f;
        lastTime = now;

        // To avoid jump
        if (deltaTime > 0.05f) deltaTime = 0.05f;

        // Play one step
        gameController.updatePhysics(deltaTime);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isGameRunning = false;
        gameLoopHandler.removeCallbacksAndMessages(null);
    }
}