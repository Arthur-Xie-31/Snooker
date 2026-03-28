package com.example.snooker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;

import com.example.snooker.model.Ball;
import com.example.snooker.model.ColorBall;
import com.example.snooker.model.CueBall;
import com.example.snooker.model.Player;
import com.example.snooker.model.RedBall;
import com.example.snooker.model.Table;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;

import java.util.HashSet;
import java.util.Set;

public class GameView extends View {

    public static final float WORLD_SCALE = 0.35f;

    // JBox2D World
    private final World world;
    // The table and cushions
    private Table table;
    // The cue ball
    private CueBall cueBall;
    private Set<Ball> allRemainingBalls = new HashSet<>();
    private Set<RedBall> targetBalls;
    private Set<RedBall> redBalls = new HashSet<>();
    private Set<RedBall> colorBalls = new HashSet<>();
    private Set<Ball> pottedBalls = new HashSet<>();
    private boolean isTargetRed = true;
    private Player currentPlayer;
    private static float scale;

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
                redBalls.add(new RedBall(world, currentX, currentY));
                currentX += (Ball.RADIUS * 2);
            }
            currentY += (float) (Ball.RADIUS * Math.sqrt(3.1d));
            currentX = (Table.WIDTH / 2) - (Ball.RADIUS * i);
        }
        */
        redBalls.add(new RedBall(world, Table.WIDTH / 4, Table.LENGTH / 4 * 3));
        redBalls.add(new RedBall(world, Table.WIDTH / 4 * 3, Table.LENGTH / 4 * 3));
        allRemainingBalls.addAll(redBalls);
        // 2.2 Create color balls
        colorBalls.add(new ColorBall(world, Table.WIDTH / 3, Table.LENGTH / 5, 2));  // Yellow ball
        colorBalls.add(new ColorBall(world, Table.WIDTH / 3 * 2, Table.LENGTH / 5, 3));  // Green ball
        colorBalls.add(new ColorBall(world, Table.WIDTH / 2, Table.LENGTH / 5, 4));  // Brown ball
        colorBalls.add(new ColorBall(world, Table.WIDTH / 2, Table.LENGTH / 2, 5));  // Blue ball
        colorBalls.add(new ColorBall(world, Table.WIDTH / 2, Table.LENGTH / 4 * 3, 6));  // Pink ball
        colorBalls.add(new ColorBall(world, Table.WIDTH / 2, Table.LENGTH / 11 * 10, 7));  // Black ball
        allRemainingBalls.addAll(colorBalls);
        // 2.3 Create the cue ball
        cueBall = new CueBall(world, Table.WIDTH / 12 * 5, Table.LENGTH / 5);  // Cue ball
        allRemainingBalls.add(cueBall);
        targetBalls = redBalls;
        isTargetRed = true;

        // 3. Create player
        currentPlayer = new Player("Mark Selby", world);

        // 4. Enable touch events
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
        if (currentPlayer.getCurrentState() == Player.GameState.MOVING) {
            return true;  // Can't aim while ball is moving
        }

        // Convert screen coordinates to world coordinates
        float worldX = event.getX() / scale;
        float worldY = event.getY() / scale;
        Vec2 touchPoint = new Vec2(worldX, worldY);

        if (currentPlayer.getCurrentState() == Player.GameState.AIMING) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // Start aiming
                case MotionEvent.ACTION_MOVE:
                    // Update aim direction
                    currentPlayer.Aiming(touchPoint);
                    return true;
                case MotionEvent.ACTION_UP:
                    // Finish aiming
                    currentPlayer.onActionFinish();
                    return true;
            }

        } else if (currentPlayer.getCurrentState() == Player.GameState.FEATHERING) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // Start feathering
                case MotionEvent.ACTION_MOVE:
                    // Update cue power
                    currentPlayer.feathering(touchPoint, cueBall.GetPosition());
                    return true;
                case MotionEvent.ACTION_UP:
                    // Take the shot
                    currentPlayer.decideCue(cueBall.GetPosition());
                    return true;
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        // 1. Update the physics simulation
        // Time step = 1/60 seconds, 10 velocity iterations, 10 position iterations
        world.step(1.0f / 60.0f, 10, 10);

        // 2.0 Check if cue tip hit the cue ball
        if (currentPlayer.getCurrentState() == Player.GameState.CUEING) {
            currentPlayer.Cueing(cueBall);
        }

        // 2.1 Check potted balls in the last frame
        table.CheckPottedBalls(allRemainingBalls);

        // 2.2 Check if ball has stopped moving, if so, check foul or not
        if ((currentPlayer.getCurrentState() == Player.GameState.MOVING) && isAllBallStopped()) {
            CheckFoul();
            currentPlayer.onActionFinish();
        }

        // 3. Draw the table
        table.draw(canvas);

        // 4. Draw balls
        for (Ball ball : allRemainingBalls) {
            ball.draw(canvas);
        }

        // 5. Draw player with aiming UI (Do not draw if the cue action is done)
        if (currentPlayer.getCurrentState() != Player.GameState.MOVING) {
            currentPlayer.drawCue(canvas, targetBalls, table, cueBall.GetPosition());
        }
        currentPlayer.printScore(canvas);

        // 6. Redraw continuously for animation
        invalidate();
    }

    private boolean isAllBallStopped() {
        if (!cueBall.IsStopped()) return false;
        for (Ball ball: allRemainingBalls) {
            if (!ball.IsStopped()) return false;
        }
        return true;
    }

    private void CheckFoul() {
        boolean isFoul = false;
        int score = 0;
        for (Ball ball : allRemainingBalls) {
            if (ball.IsPotted()) {
                // 1. Cue ball fall, foul and replace cue ball to D area;
                if (ball instanceof CueBall) {
                    isFoul = true;
                    ((CueBall) ball).ReplaceInDArea();
                } else if (ball instanceof RedBall) {
                    RedBall pottedBall = (RedBall) ball;
                    if (!targetBalls.contains(pottedBall)) {
                        // 2. color ball fall when target red ball, or red ball fall when target color ball
                        isFoul = true;
                    } else {
                        // 3. Valid goal
                        score += pottedBall.GetScore();
                    }
                    // 4. replace color ball either foul or not
                    if (pottedBall instanceof ColorBall) {
                        ((ColorBall) pottedBall).Replace();
                        // TODO: Consider if the default position has been placed
                    } else {
                        pottedBalls.add(pottedBall);
                        redBalls.remove(pottedBall);
                    }
                }
            }
        }
        allRemainingBalls.removeAll(pottedBalls);

        if (isFoul) {
            currentPlayer.onFoul();
            isTargetRed = true;
        } else if (score > 0) {
            currentPlayer.AddBreak(score);
            isTargetRed = !isTargetRed;
        } else {
            currentPlayer.onNoBallPotted();
            isTargetRed = true;
        }
        if (redBalls.isEmpty()) isTargetRed = false;
        targetBalls = isTargetRed ? redBalls : colorBalls;
    }
}