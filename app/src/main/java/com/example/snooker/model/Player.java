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

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;

import java.util.Set;

// This class include aiming line, cue and cue power indicator
public class Player {

    private static final float MAX_POWER = 100f;
    private static final float MIN_POWER = 1.0f;
    private static final float CUE_LENGTH = 145f * WORLD_SCALE;
    private static final float AIMING_LINE_LENGTH = 100f * WORLD_SCALE;
    private static final float CUE_TIP_RADIUS = 0.94f * WORLD_SCALE;

    private final String name;

    public enum GameState {
        AIMING,     // Player determine cue direction
        FEATHERING,     // Player determine cue power
        CUEING,
        MOVING    // Ball is moving, player do nothing
    }
    private GameState currentState = GameState.AIMING;

    private Body cueTip;
    // Visual elements
    private final Paint cuePaint;
    private final Paint aimingLinePaint;
    // Cue control variables
    private Vec2 aimingPoint = new Vec2(Table.WIDTH / 2, Table.LENGTH);
    private float cuePower = 0;      // 0 - 100
    private Vec2 cueVelocity = new Vec2();

    public Player(String name, World world) {
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

        // Create cue tip body definition
        BodyDef pocketDef = new BodyDef();
        pocketDef.type = BodyType.DYNAMIC;

        CircleShape pocketShape = new CircleShape();
        pocketShape.m_radius = CUE_TIP_RADIUS;

        FixtureDef pocketFixture = new FixtureDef();
        pocketFixture.shape = pocketShape;
        pocketFixture.isSensor = true;  // CRITICAL: This makes it a sensor!

        cueTip = world.createBody(pocketDef);
        cueTip.createFixture(pocketFixture);
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

    public void cueing(Vec2 cueBallPosition) {
        if (currentState == GameState.CUEING) {
            if (cuePower <= 0) return;
            // Calculate cue velocity
            float dx = aimingPoint.x - cueBallPosition.x;
            float dy = aimingPoint.y - cueBallPosition.y;

            // Normalize direction
            float length = (float) Math.sqrt(dx * dx + dy * dy);
            if (length > 0.01f) {
                dx /= length;
                dy /= length;
            }

            cueVelocity.x = dx * cuePower;
            cueVelocity.y = dy * cuePower;

            // place cue tip
            float powerLength = cuePower / 4f;
            float cueTipX = cueBallPosition.x - dx * powerLength;
            float cueTipY = cueBallPosition.y - dy * powerLength;
            cueTip.setTransform(new Vec2(cueTipX, cueTipY), 0);
            cueTip.setLinearVelocity(cueVelocity.clone());
        }
    }

    public void CheckHitCueBall(CueBall cueBall) {
        Vec2 distance = new Vec2(cueBall.GetPosition().x - cueTip.getPosition().x, cueBall.GetPosition().y - cueTip.getPosition().y);
        if (distance.length() <= Ball.RADIUS + CUE_TIP_RADIUS) {
            TakeShot(cueBall);
        }
    }

    private void TakeShot(CueBall cueBall) {
        // Apply the cue velocity to cue ball
        cueBall.CueAction(cueVelocity);
        // Switch to shooting state
        cuePower = 0;
        cueVelocity.setZero();
        cueTip.setLinearVelocity(new Vec2(0, 0));
        cueTip.setAngularVelocity(0);
        currentState = GameState.MOVING;
    }

    public GameState getCurrentState() {
        return currentState;
    }

    public void onActionFinish() {
        switch (currentState) {
            case AIMING:
                currentState = GameState.FEATHERING;
                break;
            case FEATHERING:
                currentState = GameState.CUEING;
                break;
            case CUEING:
                currentState = GameState.MOVING;
            case MOVING:
                currentState = GameState.AIMING;
                break;
        }
    }

    public void draw(@NonNull Canvas canvas, final Set<RedBall> targetBalls, final Table table, final Vec2 cueBallPosition) {
        float scale = GameView.GetScale();

        // Draw the cue
        float dx = cueBallPosition.x - aimingPoint.x;
        float dy = cueBallPosition.y - aimingPoint.y;
        // Normalize direction
        float directionLength = (float) Math.sqrt(dx * dx + dy * dy);
        if (directionLength > 0.01f) {
            dx /= directionLength;
            dy /= directionLength;
        }

        float powerLength = cuePower / 4f;
        float cueStartX = currentState == GameState.CUEING ?
                cueTip.getPosition().x : cueBallPosition.x + dx * powerLength;
        float cueStartY = currentState == GameState.CUEING ?
                cueTip.getPosition().y : cueBallPosition.y + dy * powerLength;
        float cueEndX = cueStartX + dx * CUE_LENGTH;
        float cueEndY = cueStartY + dy * CUE_LENGTH;

        // Convert world coordinates to screen coordinates
        canvas.drawLine(cueStartX * scale, cueStartY * scale,
                        cueEndX * scale, cueEndY * scale,
                        cuePaint);

        if (currentState == GameState.CUEING) return;

        // Draw aim line (from cue ball to hit point)
        Vec2 cueDirection = new Vec2(aimingPoint.x - cueBallPosition.x, aimingPoint.y - cueBallPosition.y);
        // 1. Check if it will hit any ball
        Vec2 minDistance = new Vec2(Table.WIDTH, Table.LENGTH);
        Vec2 minHitPoint = new Vec2(-1f, -1f);
        Ball hitBall = null;
        for (Ball ball : targetBalls) {
            Vec2 ballHitPoint = new Vec2();
            if (ball.WillHit(cueBallPosition, cueDirection, ballHitPoint)) {
                Vec2 distance = new Vec2(ballHitPoint.x - cueBallPosition.x, ballHitPoint.y - cueBallPosition.y);
                if (distance.length() < minDistance.length()) {
                    minDistance = distance;
                    minHitPoint = ballHitPoint;
                    hitBall = ball;
                }
            }
        }

        // 2. If it won't hit any ball, check if it will hit cushion
        if (hitBall != null) {
            canvas.drawLine(cueBallPosition.x * scale, cueBallPosition.y * scale,
                    minHitPoint.x * scale, minHitPoint.y * scale, aimingLinePaint);
            canvas.drawCircle(minHitPoint.x * scale, minHitPoint.y * scale,
                    Ball.RADIUS * scale, aimingLinePaint);

            // Draw the target ball prediction line
            dx = hitBall.GetPosition().x - minHitPoint.x;
            dy = hitBall.GetPosition().y - minHitPoint.y;
            // Normalize direction
            directionLength = (float) Math.sqrt(dx * dx + dy * dy);
            if (directionLength > 0.01f) {
                dx /= directionLength;
                dy /= directionLength;
            }
            float targetX = hitBall.GetPosition().x + dx * AIMING_LINE_LENGTH;
            float targetY = hitBall.GetPosition().y + dy * AIMING_LINE_LENGTH;
            canvas.drawLine(hitBall.GetPosition().x * scale, hitBall.GetPosition().y * scale,
                    targetX * scale, targetY * scale, aimingLinePaint);
        } else {
            Vec2 cushionHitPoint = table.getCushionHitPoint(cueBallPosition, cueDirection);
            canvas.drawLine(cueBallPosition.x * scale, cueBallPosition.y * scale,
                    cushionHitPoint.x * scale, cushionHitPoint.y * scale, aimingLinePaint);
            canvas.drawCircle(cushionHitPoint.x * scale, cushionHitPoint.y * scale,
                    Ball.RADIUS * scale, aimingLinePaint);
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
