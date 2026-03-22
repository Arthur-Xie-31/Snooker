package com.example.snooker;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import com.example.snooker.model.Ball;
import com.example.snooker.model.ColorBall;
import com.example.snooker.model.CueBall;
import com.example.snooker.model.RedBall;
import com.example.snooker.model.Table;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;

import java.util.LinkedList;
import java.util.List;

public class GameView extends View {

    public static final float WORLD_SCALE = 0.35f;

    // JBox2D World
    private final World world;
    // The table and cushions
    private Table table;
    // A single red ball for now
    //private RedBall redBall;
    // The cue ball
    private CueBall cueBall;
    private List<RedBall> targetBalls = new LinkedList<>();
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
        // 2.3 Create the cue ball
        cueBall = new CueBall(world, Table.WIDTH / 12 * 5, Table.LENGTH / 5);  // Cue ball

        // 3. Give it an initial push (like a cue shot)
        // maximum (100, 100)
        cueBall.CueAction(-2.2f, 55f);
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

        // 1. Update the physics simulation
        // Time step = 1/60 seconds, 10 velocity iterations, 10 position iterations
        world.step(1.0f / 60.0f, 10, 10);

        // 2. Check potted balls in the last frame
        //table.CheckPottedBalls(targetBalls);

        // 3. Draw the table
        table.draw(canvas);

        // 4. Draw balls
        cueBall.draw(canvas);
        for (RedBall ball : targetBalls) {
            ball.draw(canvas);
        }

        // 5. Redraw continuously for animation
        invalidate();
    }
}