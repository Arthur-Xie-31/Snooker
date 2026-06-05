package com.example.snooker;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;

import com.example.snooker.model.Ball;
import com.example.snooker.model.Player;
import com.example.snooker.model.Table;

import java.util.HashSet;
import java.util.Set;

public class GameView extends View {

    public static final float WORLD_SCALE = 0.35f;
    private static float scale;

    // The table and cushions
    private Table table;
    // All balls
    private Set<Ball> balls = new HashSet<>();
    private Player player;

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // Enable touch events
        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    public void setGameModel(Table table, Set<Ball> balls, Player player) {
        this.table = table;
        this.balls.addAll(balls);
        this.player = player;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // Calculate scale between screen pixels and physics world units
        float scaleX = w / Table.WIDTH;
        float scaleY = h / Table.LENGTH;
        scale = Math.min(scaleX, scaleY);
    }

    public static float GetScale() {
        return scale;
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        // 1. Draw the table
        table.draw(canvas);

        // 2. Draw balls
        for (Ball ball : balls) {
            ball.draw(canvas);
        }

        // 3. Draw player with their cue, aiming line and scores
        player.draw(canvas, balls, table);
    }
}