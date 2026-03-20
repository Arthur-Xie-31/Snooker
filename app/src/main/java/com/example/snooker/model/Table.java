package com.example.snooker.model;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.snooker.GameView;

import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.World;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Table extends Drawable {

    // snooker table length
    public static final float LENGTH = 366f;
    // snooker table width
    public static final float WIDTH = 183f;

    // Cushions
    private Cushion leftCushion, rightCushion, topCushion, bottomCushion;
    // Store pocket bodies in case we need to reference them later
    private Body leftBottomPocket;
    private Set<Pocket> pockets = new HashSet<>();
    
    public Table(World world) {
        // Create cushions
        topCushion = new Cushion(world, 0, 0, WIDTH, Cushion.THICKNESS);
        bottomCushion = new Cushion(world, 0, LENGTH - Cushion.THICKNESS, WIDTH, LENGTH);
        leftCushion = new Cushion(world, 0, 0, Cushion.THICKNESS, LENGTH);
        rightCushion = new Cushion(world, WIDTH - Cushion.THICKNESS, 0, WIDTH, LENGTH);

        // Create pockets
        pockets.add(new Pocket(world, Cushion.THICKNESS, Cushion.THICKNESS));
        pockets.add(new Pocket(world, WIDTH - Cushion.THICKNESS, Cushion.THICKNESS));
        pockets.add(new Pocket(world, Cushion.THICKNESS, LENGTH / 2));
        pockets.add(new Pocket(world, WIDTH - Cushion.THICKNESS, LENGTH / 2));
        pockets.add(new Pocket(world, Cushion.THICKNESS, LENGTH - Cushion.THICKNESS));
        pockets.add(new Pocket(world, WIDTH - Cushion.THICKNESS, LENGTH - Cushion.THICKNESS));
    }

    public void CheckPottedBalls(final List<Ball> allBalls) {
        for (Ball ball : allBalls) {
            for (Pocket pocket : pockets) {
                if (pocket.fallsInPocket(ball)) {
                    ball.SetAsPotted();
                    break;
                }
            }
        }
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        float scale = GameView.GetScale();
        // Draw the table surface
        canvas.drawColor(Color.WHITE);   // White background
        Paint tablePaint = new Paint();
        tablePaint.setColor(Color.rgb(34, 139, 34));    // Green
        tablePaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(0, 0, WIDTH * scale, LENGTH * scale, tablePaint);

        // Draw cushions
        topCushion.draw(canvas);
        bottomCushion.draw(canvas);
        leftCushion.draw(canvas);
        rightCushion.draw(canvas);

        // Draw pockets
        for (Pocket pocket : pockets) {
            pocket.draw(canvas);
        }
    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }

    @Override
    public void setAlpha(int alpha) {
        // No-op
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        // No-op
    }
}
