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

public class Pocket extends Drawable {

    // snooker pocket radius
    public static final float RADIUS = 8.4f * WORLD_SCALE;

    private Body body;
    private Paint paint;
    private final Vec2 position;

    public Pocket(World world, float positionX, float positionY) {
        position = new Vec2(positionX, positionY);

        // Create pocket body definition
        BodyDef pocketDef = new BodyDef();
        pocketDef.type = BodyType.STATIC;
        pocketDef.position.set(positionX, positionY);

        CircleShape pocketShape = new CircleShape();
        pocketShape.m_radius = RADIUS;

        FixtureDef pocketFixture = new FixtureDef();
        pocketFixture.shape = pocketShape;
        pocketFixture.isSensor = true;  // CRITICAL: This makes it a sensor!

        body = world.createBody(pocketDef);
        body.createFixture(pocketFixture);

        // Setup paint
        paint = new Paint();
        paint.setColor(Color.GRAY);
        paint.setStyle(Paint.Style.FILL);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        float scale = GameView.GetScale();
        float radiusInPixels = RADIUS * scale;
        float screenX = position.x * scale;
        float screenY = position.y * scale;
        canvas.drawCircle(screenX, screenY, radiusInPixels, paint);
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

    public boolean fallsInPocket(Ball ball) {
        Vec2 ballPosition = ball.GetPosition();
        double distance = Math.sqrt(Math.pow((ballPosition.x - position.x), 2) + Math.pow((ballPosition.y - position.y), 2));
        return distance < RADIUS;
    }
}
