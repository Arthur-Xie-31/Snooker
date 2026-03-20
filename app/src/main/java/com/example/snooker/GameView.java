package com.example.snooker;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;

import com.example.snooker.model.CueBall;
import com.example.snooker.model.RedBall;
import com.example.snooker.model.Table;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;

import java.util.List;

public class GameView extends View {

    // JBox2D World
    private World world;
    // The table and cushions
    private Table table;
    // A single red ball for now
    private RedBall redBall;
    // The cue ball
    private CueBall cueBall;
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
        cueBall = new CueBall(world, Table.WIDTH / 2, Table.LENGTH / 2);
        redBall = new RedBall(world, Table.WIDTH / 4, Table.LENGTH / 4);

        // 3. Give it an initial push (like a cue shot)
        cueBall.CueAction(-Table.WIDTH - 5f, -Table.LENGTH);
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
        table.CheckPottedBalls(List.of(cueBall, redBall));

        // 3. Draw the table
        table.draw(canvas);

        // 4. Draw balls
        cueBall.draw(canvas);
        redBall.draw(canvas);

        // 5. Redraw continuously for animation
        invalidate();
    }
}