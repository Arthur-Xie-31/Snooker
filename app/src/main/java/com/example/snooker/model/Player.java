package com.example.snooker.model;

import static com.example.snooker.GameView.WORLD_SCALE;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.snooker.GameView;

import org.jbox2d.common.Vec2;

import java.util.Set;

// This class include aiming line, cue and cue power indicator
public class Player {

    private static final float MAX_POWER = 100f;
    private static final float MIN_POWER = 1.0f;
    private static final float CUE_LENGTH = 145f * WORLD_SCALE;

    private final String name;

    public enum GameState {
        AIMING,     // Player determine cue direction
        FEATHERING,     // Player determine cue power
        MOVING    // Ball is moving, player do nothing
    }
    private GameState currentState = GameState.AIMING;
    // Visual elements
    private Paint cuePaint;
    private Paint aimingLinePaint;
    // Cue control variables
    private Vec2 aimingPoint = new Vec2(Table.WIDTH / 2, Table.LENGTH);
    private float cuePower = 0;      // Power of the shot (0-1 range)

    public Player(String name) {
        this.name = name;

        // Cue paint
        cuePaint = new Paint();
        cuePaint.setColor(Color.LTGRAY);
        cuePaint.setStrokeWidth(12);
        cuePaint.setStyle(Paint.Style.STROKE);

        // Aiming line paint
        aimingLinePaint = new Paint();
        aimingLinePaint.setColor(Color.WHITE);
        aimingLinePaint.setStrokeWidth(8);
        aimingLinePaint.setStyle(Paint.Style.STROKE);
    }

    public void Aiming(Vec2 touchPoint) {
        if (currentState == GameState.AIMING) {
            this.aimingPoint = touchPoint;
        }
    }

    public void feathering(Vec2 touchPoint, Vec2 originPoint) {
        if (currentState == GameState.FEATHERING) {
            float dx = touchPoint.x - originPoint.x;
            float dy = touchPoint.y - originPoint.y;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);
            cuePower = Math.max(MIN_POWER, distance);
            cuePower = Math.min(cuePower, MAX_POWER);
        }
    }

    public void TakeShot(CueBall cueBall) {
        if (cuePower <= 0) return;

        // Calculate direction from cue ball to touch point
        float dx = aimingPoint.x - cueBall.GetPosition().x;
        float dy = aimingPoint.y - cueBall.GetPosition().y;

        // Normalize direction
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        if (length > 0.01f) {
            dx /= length;
            dy /= length;
        }

        // Apply the initial velocity to cue ball
        cueBall.CueAction(dx * cuePower, dy * cuePower);

        // Switch to shooting state
        cuePower = 0;
        currentState = GameState.MOVING;
    }

    public GameState getCurrentState() {
        return currentState;
    }

    public void setCurrentState(GameState state) {
        currentState = state;
    }

    public void draw(@NonNull Canvas canvas, final Set<Ball> targetBalls, final Table table, final Vec2 cueBallPosition) {
        float scale = GameView.GetScale();
        // Convert world coordinates to screen coordinates
        float startX = cueBallPosition.x * scale;
        float startY = cueBallPosition.y * scale;

        float dx = cueBallPosition.x - aimingPoint.x;
        float dy = cueBallPosition.y - aimingPoint.y;

        // Normalize direction
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        if (length > 0.01f) {
            dx /= length;
            dy /= length;
        }
        float powerLength = (cuePower / 4f) * scale;
        float cueLength = CUE_LENGTH * scale;

        float cueStartX = startX + dx * powerLength;
        float cueStartY = startY + dy * powerLength;
        float cueEndX = cueStartX + dx * cueLength;
        float cueEndY = cueStartY + dy * cueLength;

        // Draw the cue
        canvas.drawLine(cueStartX, cueStartY, cueEndX, cueEndY, cuePaint);

        // Draw aim line (from cue ball to hit point)
        Vec2 cueDirection = new Vec2(aimingPoint.x - cueBallPosition.x, aimingPoint.y - cueBallPosition.y);
        // 1. Check if it will hit any ball
        boolean willHitBall = false;
        Vec2 minDistance = new Vec2(Table.WIDTH, Table.LENGTH);
        Vec2 minHitPoint = new Vec2(-1f, -1f);
        for (Ball ball : targetBalls) {
            Vec2 ballHitPoint = new Vec2();
            if (ball.WillHit(cueBallPosition, cueDirection, ballHitPoint)) {
                Vec2 distance = new Vec2(ballHitPoint.x - cueBallPosition.x, ballHitPoint.y - cueBallPosition.y);
                if (distance.length() < minDistance.length()) {
                    minDistance = distance;
                    minHitPoint = ballHitPoint;
                    willHitBall = true;
                }
            }
        }

        // 2. If it won't hit any ball, check if it will hit cushion
        if (willHitBall) {
            canvas.drawLine(startX, startY, minHitPoint.x * scale, minHitPoint.y * scale, aimingLinePaint);
            canvas.drawCircle(minHitPoint.x * scale, minHitPoint.y * scale, Ball.RADIUS * scale, aimingLinePaint);
        } else {
            Vec2 cushionHitPoint = table.getCushionHitPoint(cueBallPosition, cueDirection);
            canvas.drawLine(startX, startY, cushionHitPoint.x * scale, cushionHitPoint.y * scale, aimingLinePaint);
            canvas.drawCircle(cushionHitPoint.x * scale, cushionHitPoint.y * scale, Ball.RADIUS * scale, aimingLinePaint);
        }

        // Draw power text
        Paint textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(80);
        textPaint.setStyle(Paint.Style.FILL);
        int powerPercent = (int) (cuePower);
        canvas.drawText("Power: " + powerPercent + "%", 50, 100, textPaint);
    }
}
