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
import org.jbox2d.dynamics.World;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Table extends Drawable {

    // snooker table length
    public static final float LENGTH = 366f * WORLD_SCALE;
    // snooker table width
    public static final float WIDTH = 183f * WORLD_SCALE;

    // Cushions
    private final Set<Cushion> cushions = new HashSet<>();
    // Pockets
    private final Set<Pocket> pockets = new HashSet<>();
    
    public Table(World world) {
        // Create cushions
        cushions.add(new Cushion(world, 0, 0, WIDTH, Cushion.THICKNESS, "top"));
        cushions.add(new Cushion(world, 0, LENGTH - Cushion.THICKNESS, WIDTH, LENGTH, "bottom"));
        cushions.add(new Cushion(world, 0, 0, Cushion.THICKNESS, LENGTH, "left"));
        cushions.add(new Cushion(world, WIDTH - Cushion.THICKNESS, 0, WIDTH, LENGTH, "right"));

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

        // Draw the table
        canvas.drawColor(Color.WHITE);   // White background
        Paint tablePaint = new Paint();
        tablePaint.setColor(Color.rgb(34, 139, 34));    // Green table surface
        tablePaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(0, 0, WIDTH * scale, LENGTH * scale, tablePaint);

        tablePaint.setColor(Color.WHITE);    // White lines
        tablePaint.setStyle(Paint.Style.STROKE);
        float dAreaCenterX = (WIDTH / 2f) * scale;
        float dAreaCenterY = (LENGTH / 5f) * scale;
        float dAreaRadius = (WIDTH / 6f) * scale;
        // Draw the D semicircle (above the baulk line)
        // Using arc - starting at 0 degrees (right) to 180 degrees (left)
        // Since canvas angles: 0° = 3 o'clock, 180° = 9 o'clock
        android.graphics.RectF dAreaRect = new android.graphics.RectF(
                dAreaCenterX - dAreaRadius,
                dAreaCenterY - dAreaRadius,
                dAreaCenterX + dAreaRadius,
                dAreaCenterY + dAreaRadius
        );
        // Draw the D outline
        canvas.drawArc(dAreaRect, 180, 180, false, tablePaint);
        canvas.drawLine(0, dAreaCenterY, WIDTH * scale, dAreaCenterY, tablePaint);

        // Draw cushions
        for (Cushion cushion : cushions) {
            cushion.draw(canvas);
        }

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

    public Vec2 getCushionHitPoint(Vec2 startPoint, Vec2 direction) {
        Vec2 hitPoint = new Vec2(0, 0);
        for (Cushion cushion : cushions) {
            // It's guaranteed that only one cushion will be hit
            if (cushion.willHitCushion(startPoint, direction, hitPoint)) {
                return hitPoint;
            }
        }
        return hitPoint;
    }
}
