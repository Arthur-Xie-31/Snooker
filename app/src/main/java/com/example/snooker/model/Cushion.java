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

import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;

public class Cushion extends Drawable {

    // snooker table cushion thickness
    public static final float THICKNESS = 10f * WORLD_SCALE;
    // cushion fixture parameters
    private static final float DENSITY = 1.0f;
    private static final float FRICTION = 0.1f;    // friction when ball hits cushion
    private static final float RESTITUTION = 0.6f;    // bounciness (snooker cushions are quite bouncy)
    // cushion color in RBG
    private static final int COLOR = Color.rgb(82, 27, 7);

    private Body body;
    private Paint paint;
    private final float left, top, right, bottom;
    private final String name;

    public Cushion(World world, float left, float top, float right, float bottom, String name) {
        this.name = name;
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

    public boolean willHitCushion(Vec2 startPoint, Vec2 direction, Vec2 hitPoint) {
        float slope = direction.y / direction.x;
        switch (name) {
            case "top" : {
                if ((direction.y >= 0) || (startPoint.y <= (THICKNESS + Ball.RADIUS)))
                    return false;
                float deltaY = (THICKNESS + Ball.RADIUS) - startPoint.y;
                float deltaX = deltaY / slope;
                float hitX = startPoint.x + deltaX;
                if ((hitX > THICKNESS) && (hitX < Table.WIDTH - THICKNESS)) {
                    hitPoint.x = hitX;
                    hitPoint.y = THICKNESS + Ball.RADIUS;
                    return true;
                } else {
                    return false;
                }
            }
            case "bottom" : {
                if ((direction.y <= 0) || (startPoint.y >= (Table.LENGTH - THICKNESS - Ball.RADIUS)))
                    return false;
                float deltaY = (Table.LENGTH - THICKNESS - Ball.RADIUS) - startPoint.y;
                float deltaX = deltaY / slope;
                float hitX = startPoint.x + deltaX;
                if ((hitX > THICKNESS) && (hitX < Table.WIDTH - THICKNESS)) {
                    hitPoint.x = hitX;
                    hitPoint.y = Table.LENGTH - THICKNESS - Ball.RADIUS;
                    return true;
                } else {
                    return false;
                }
            }
            case "left" : {
                if ((direction.x >= 0) || (startPoint.x <= (THICKNESS + Ball.RADIUS)))
                    return false;
                float deltaX = (THICKNESS + Ball.RADIUS) - startPoint.x;
                float deltaY = deltaX * slope;
                float hitY = startPoint.y + deltaY;
                if ((hitY > THICKNESS) && (hitY < Table.LENGTH - THICKNESS)) {
                    hitPoint.x = THICKNESS + Ball.RADIUS;
                    hitPoint.y = hitY;
                    return true;
                } else {
                    return false;
                }
            }
            case "right" : {
                if ((direction.x <= 0) || (startPoint.x >= (Table.WIDTH - THICKNESS - Ball.RADIUS)))
                    return false;
                float deltaX = (Table.WIDTH - THICKNESS - Ball.RADIUS) - startPoint.x;
                float deltaY = deltaX * slope;
                float hitY = startPoint.y + deltaY;
                if ((hitY > THICKNESS) && (hitY < Table.LENGTH - THICKNESS)) {
                    hitPoint.x = Table.WIDTH - THICKNESS - Ball.RADIUS;
                    hitPoint.y = hitY;
                    return true;
                } else {
                    return false;
                }
            }
            default:
                return false;
        }
    }
}
