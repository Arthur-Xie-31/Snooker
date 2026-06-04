package com.example.snooker;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;

import com.example.snooker.model.GameModel;
import com.example.snooker.model.Table;

public class GameView extends View {

    public static final float WORLD_SCALE = 0.35f;

    private GameModel gameModel;

    private static float scale;

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // Enable touch events
        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    public void setGameModel(GameModel gameModel) {
        this.gameModel = gameModel;
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
        super.onDraw(canvas);
        gameModel.draw(canvas);
    }
}