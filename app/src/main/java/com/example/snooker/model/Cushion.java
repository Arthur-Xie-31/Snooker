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

import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;

public class Cushion extends Drawable {

    // snooker table cushion thickness
    public static final float THICKNESS = 10f;
    // cushion fixture parameters
    private static final float DENSITY = 1.0f;
    private static final float FRICTION = 0.2f;    // friction when ball hits cushion
    private static final float RESTITUTION = 0.6f;    // bounciness (snooker cushions are quite bouncy)
    // cushion color in RBG
    private static final int COLOR = Color.rgb(71, 28, 2);

    private Body body;
    private Paint paint;
    private final float left, top, right, bottom;

    public Cushion(World world, float left, float top, float right, float bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;

        // Cushion body definition
        BodyDef cushionBodyDef = new BodyDef();
        cushionBodyDef.type = BodyType.STATIC;  // Cushions don't move
        cushionBodyDef.position.set((left + right) / 2, (top + bottom) / 2);

        // Fixture definition
        FixtureDef cushionFixtureDef = new FixtureDef();
        cushionFixtureDef.density = DENSITY;
        cushionFixtureDef.friction = FRICTION;
        cushionFixtureDef.restitution = RESTITUTION;
        PolygonShape bottomEdgeShape = new PolygonShape();
        bottomEdgeShape.setAsBox((right - left) / 2, (bottom - top) / 2);
        cushionFixtureDef.shape = bottomEdgeShape;

        // Create cushion body
        body = world.createBody(cushionBodyDef);
        body.createFixture(cushionFixtureDef);

        // setup cushion paint
        paint = new Paint();
        paint.setColor(COLOR);  // Brown color for wooden cushions
        paint.setStyle(Paint.Style.FILL);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        float scale = GameView.GetScale();
        canvas.drawRect(left * scale, top * scale,
                right * scale, bottom * scale,
                paint);
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
