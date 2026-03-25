package com.example.snooker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;

import com.example.snooker.model.Ball;
import com.example.snooker.model.CueBall;
import com.example.snooker.model.RedBall;
import com.example.snooker.model.Table;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class GameView extends View {

    public static final float WORLD_SCALE = 0.35f;

    // JBox2D World
    private final World world;
    // The table and cushions
    private Table table;
    // The cue ball
    private CueBall cueBall;
    private List<Ball> allBalls = new LinkedList<>();
    private Set<Ball> redBalls = new HashSet<>();
    private static float scale;

    // Game state
    private enum GameState {
        AIMING,     // Player determine cue direction
        FEATHERING,     // Player determine cue power
        MOVING    // Ball is moving, player do nothing
    }
    private GameState currentState = GameState.AIMING;
    // Visual elements
    private Paint cuePaint;
    private Paint powerPaint;
    // Cue control variables
    private Vec2 cueEndPoint;        // Where touch ended (for direction)
    private float cuePower = 0;      // Power of the shot (0-1 range)
    private boolean isAiming = false;
    // Constants
    private static final float MAX_POWER = 30f;      // Max velocity in world units/sec
    private static final float MIN_POWER = 5f;       // Minimum power for a shot
    // Track if shot has been taken
    private boolean shotTaken = false;

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // Create the physics world with gravity
        // (no gravity in snooker, but we use it for friction/slowing down).
        world = new World(new Vec2(0.0f, 0.0f)); // Zero gravity
        init();
    }

    private void init() {
        // 1. Create the table and cushions
        table = new Table(world);

        // 2. Create balls
        // 2.1 Create red balls
        /*
        float currentX = Table.WIDTH / 2;
        float currentY = (Table.LENGTH / 4 * 3) + (Ball.RADIUS * 2);
        for (int i = 1; i <= 5; i++) {
            for (int j = 0; j < i; j++) {
                targetBalls.add(new RedBall(world, currentX, currentY));
                currentX += (Ball.RADIUS * 2);
            }
            currentY += (float) (Ball.RADIUS * Math.sqrt(3.1d));
            currentX = (Table.WIDTH / 2) - (Ball.RADIUS * i);
        }
        // 2.2 Create color balls
        targetBalls.add(new ColorBall(world, Table.WIDTH / 3, Table.LENGTH / 5, 2));  // Yellow ball
        targetBalls.add(new ColorBall(world, Table.WIDTH / 3 * 2, Table.LENGTH / 5, 3));  // Green ball
        targetBalls.add(new ColorBall(world, Table.WIDTH / 2, Table.LENGTH / 5, 4));  // Brown ball
        targetBalls.add(new ColorBall(world, Table.WIDTH / 2, Table.LENGTH / 2, 5));  // Blue ball
        targetBalls.add(new ColorBall(world, Table.WIDTH / 2, Table.LENGTH / 4 * 3, 6));  // Pink ball
        targetBalls.add(new ColorBall(world, Table.WIDTH / 2, Table.LENGTH / 11 * 10, 7));  // Black ball
        */
        redBalls.add(new RedBall(world, Table.WIDTH / 4 * 3, Table.LENGTH/ 4 * 3));
        allBalls.addAll(redBalls);
        // 2.3 Create the cue ball
        //cueBall = new CueBall(world, Table.WIDTH / 12 * 5, Table.LENGTH / 5);  // Cue ball
        cueBall = new CueBall(world, Table.WIDTH / 2, Table.LENGTH / 2);
        allBalls.add(cueBall);

        // 3. Give it an initial push (like a cue shot)
        // maximum (100, 100)
        //cueBall.CueAction(-2.2f, 55f);

        // Cue line paint
        cuePaint = new Paint();
        cuePaint.setColor(Color.LTGRAY);
        cuePaint.setStrokeWidth(12);
        cuePaint.setStyle(Paint.Style.STROKE);

        // Power indicator paint
        powerPaint = new Paint();
        powerPaint.setColor(Color.WHITE);
        powerPaint.setStrokeWidth(8);
        powerPaint.setStyle(Paint.Style.STROKE);

        // Enable touch events
        setFocusable(true);
        setFocusableInTouchMode(true);
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

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Only allow aiming when ball is stationary
        if (currentState == GameState.MOVING) {
            return true;  // Can't aim while ball is moving
        }

        // Convert screen coordinates to world coordinates
        float worldX = event.getX() / scale;
        float worldY = event.getY() / scale;
        Vec2 touchPoint = new Vec2(worldX, worldY);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // Start aiming
                cueEndPoint = touchPoint;
                isAiming = true;
                cuePower = 0;
                return true;

            case MotionEvent.ACTION_MOVE:
                // Update aim direction
                if (isAiming) {
                    cueEndPoint = touchPoint;
                    // Calculate power based on drag distance
                    float dx = cueEndPoint.x - cueBall.GetPosition().x;
                    float dy = cueEndPoint.y - cueBall.GetPosition().y;
                    float distance = (float) Math.sqrt(dx * dx + dy * dy);
                    cuePower = Math.max(1.0f, distance / 2.0f);  // Max power at 2 units distance
                }
                return true;

            case MotionEvent.ACTION_UP:
                // Take the shot
                if (isAiming && cuePower > 0.1f) {
                    takeShot();
                }
                isAiming = false;
                cueEndPoint = null;
                cuePower = 0;
                return true;
        }
        return super.onTouchEvent(event);
    }

    private void takeShot() {
        if (cueEndPoint == null) return;

        // Calculate direction from cue ball to touch point
        float dx = cueBall.GetPosition().x - cueEndPoint.x;  // Reverse direction (pull back)
        float dy = cueBall.GetPosition().y - cueEndPoint.y;

        // Normalize direction
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        if (length > 0.01f) {
            dx /= length;
            dy /= length;
        }

        // Calculate power (MIN_POWER to MAX_POWER)
        float power = cuePower * 15;

        // Apply velocity to ball
        cueBall.CueAction(dx * power, dy * power);

        // Switch to shooting state
        currentState = GameState.MOVING;
        shotTaken = true;

        // Invalidate to redraw
        invalidate();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        // 1. Update the physics simulation
        // Time step = 1/60 seconds, 10 velocity iterations, 10 position iterations
        world.step(1.0f / 60.0f, 10, 10);

        // 2. Check potted balls in the last frame
        table.CheckPottedBalls(allBalls);

        // Check if ball has stopped moving
        if (currentState == GameState.MOVING && isAllBallStopped()) {
            currentState = GameState.AIMING;
            shotTaken = false;
        }

        // 3. Draw the table
        table.draw(canvas);

        // 4. Draw balls
        for (Ball ball : allBalls) {
            ball.draw(canvas);
        }

        // Draw aiming UI (only when not shooting)
        if (currentState == GameState.AIMING) {
            if (isAiming) {
                drawAimingUI(canvas);
            } else {
                // Draw aiming reminder
                Paint textPaint = new Paint();
                textPaint.setColor(Color.WHITE);
                textPaint.setTextSize(80);
                textPaint.setStyle(Paint.Style.FILL);
                canvas.drawText("Please take the cue", 50, 100, textPaint);
            }
        }

        // 5. Redraw continuously for animation
        invalidate();
    }

    private boolean isAllBallStopped() {
        if (!cueBall.IsStopped()) return false;
        for (Ball ball: allBalls) {
            if (!ball.IsStopped()) return false;
        }
        return true;
    }

    private void drawAimingUI(Canvas canvas) {
        // Convert world coordinates to screen coordinates
        float startX = cueBall.GetPosition().x * scale;
        float startY = cueBall.GetPosition().y * scale;
        float endX = cueEndPoint.x * scale;
        float endY = cueEndPoint.y * scale;

        // Draw the cue
        canvas.drawLine(startX, startY, endX, endY, cuePaint);

        // Draw power indicator (length of line represents power)
        float powerLength = 50 + cuePower * 100;  // 50-250 pixels
        float angle = (float) Math.atan2(endY - startY, endX - startX);

        // Draw the cue stick extending beyond the ball
        float cueX = (startX - (float) Math.cos(angle) * powerLength) / scale;
        float cueY = (startY - (float) Math.sin(angle) * powerLength) / scale;
        //canvas.drawLine(startX, startY, cueX, cueY, powerPaint);

        // Draw aim line (from cue ball to hit point)
        Vec2 cueDirection = new Vec2(cueX - cueBall.GetPosition().x, cueY - cueBall.GetPosition().y);
        // 1. Check if it will hit any ball
        boolean willHitBall = false;
        Vec2 minDistance = new Vec2(Table.WIDTH, Table.LENGTH);
        Vec2 minHitPoint = new Vec2(-1f, -1f);
        for (Ball ball : redBalls) {
            Vec2 ballHitPoint = new Vec2();
            Vec2 tempDirection = new Vec2(-100f, -100f);
            if (ball.WillHit(cueBall.GetPosition(), cueDirection, ballHitPoint)) {
                Vec2 distance = new Vec2(ballHitPoint.x - cueBall.GetPosition().x, ballHitPoint.y - cueBall.GetPosition().y);
                if (distance.length() < minDistance.length()) {
                    minDistance = distance;
                    minHitPoint = ballHitPoint;
                    willHitBall = true;
                }
            }
        }

        // 2. If it won't hit any ball, check if it will hit cushion
        if (willHitBall) {
            canvas.drawLine(startX, startY, minHitPoint.x * scale, minHitPoint.y * scale, powerPaint);
            canvas.drawCircle(minHitPoint.x * scale, minHitPoint.y * scale, Ball.RADIUS * scale, powerPaint);
        } else {
            Vec2 cushionHitPoint = table.getCushionHitPoint(cueBall.GetPosition(), cueDirection);
            canvas.drawLine(startX, startY, cushionHitPoint.x * scale, cushionHitPoint.y * scale, powerPaint);
            canvas.drawCircle(cushionHitPoint.x * scale, cushionHitPoint.y * scale, Ball.RADIUS * scale, powerPaint);
        }

        // Draw power text
        Paint textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(80);
        textPaint.setStyle(Paint.Style.FILL);
        int powerPercent = (int) (cuePower * 100);
        canvas.drawText("Power: " + powerPercent + "%", 50, 100, textPaint);
    }
}