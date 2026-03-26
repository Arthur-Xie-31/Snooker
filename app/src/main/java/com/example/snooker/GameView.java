package com.example.snooker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;

import com.example.snooker.model.Ball;
import com.example.snooker.model.CueBall;
import com.example.snooker.model.Player;
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
        redBalls.add(new RedBall(world, Table.WIDTH / 4, Table.LENGTH/ 4 * 3));
        allBalls.addAll(redBalls);
        // 2.3 Create the cue ball
        //cueBall = new CueBall(world, Table.WIDTH / 12 * 5, Table.LENGTH / 5);  // Cue ball
        cueBall = new CueBall(world, Table.WIDTH / 2, Table.LENGTH / 2);
        allBalls.add(cueBall);

        // 3. Create player
        currentPlayer = new Player("Mark Selby");

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
                    currentPlayer.Aiming(touchPoint);
                    return true;

                case MotionEvent.ACTION_MOVE:
                    // Update aim direction
                    currentPlayer.Aiming(touchPoint);
                    return true;

                case MotionEvent.ACTION_UP:
                    // Finish aiming
                    currentPlayer.setCurrentState(Player.GameState.FEATHERING);
                    return true;
            }
        } else if (currentPlayer.getCurrentState() == Player.GameState.FEATHERING) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // Start feathering
                    currentPlayer.feathering(touchPoint, cueBall.GetPosition());
                    return true;

                case MotionEvent.ACTION_MOVE:
                    // Update cue power
                    currentPlayer.feathering(touchPoint, cueBall.GetPosition());
                    return true;

                case MotionEvent.ACTION_UP:
                    // Take the shot
                    currentPlayer.TakeShot(cueBall);
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

        // 2.1 Check potted balls in the last frame
        table.CheckPottedBalls(allBalls);

        // 2.2 Check if ball has stopped moving
        if ((currentPlayer.getCurrentState() == Player.GameState.MOVING) && isAllBallStopped()) {
            currentPlayer.setCurrentState(Player.GameState.AIMING);
        }

        // 3. Draw the table
        table.draw(canvas);

        // 4. Draw balls
        for (Ball ball : allBalls) {
            ball.draw(canvas);
        }

        // 5. Draw player with aiming UI (only when not shooting)
        if (currentPlayer.getCurrentState() != Player.GameState.MOVING) {
            currentPlayer.draw(canvas, redBalls, table, cueBall.GetPosition());
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
}