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

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;

public abstract class Ball extends Drawable {

    public static final float RADIUS = 4.2f;

    protected Body body;

    protected Paint paint;

    protected boolean isPotted = false;

    public Ball(World world, float positionX, float positionY) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyType.DYNAMIC;
        bodyDef.position.set(positionX, positionY);
        // Damping properties for friction
        bodyDef.linearDamping = 0.2f;    // Slows down linear movement (critical for snooker)
        bodyDef.angularDamping = 0.3f;    // Slows down rotation (spin)
        bodyDef.allowSleep = false;
        // Set these to prevent tunneling
        bodyDef.fixedRotation = false;  // Allow rotation for spin
        bodyDef.bullet = true;  // This enables continuous collision detection!

        body = world.createBody(bodyDef);

        CircleShape circleShape = new CircleShape();
        circleShape.m_radius = RADIUS;

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circleShape;
        fixtureDef.density = 1.0f;
        fixtureDef.friction = 0.3f;
        fixtureDef.restitution = 0.8f;

        body.createFixture(fixtureDef);

        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        // Do not draw potted ball
        if (isPotted) return;

        // Get the ball's position from the physics engine
        Vec2 position = body.getPosition();

        // Convert world coordinates to screen pixel coordinates
        float scale = GameView.GetScale();
        float screenX = position.x * scale;
        float screenY = position.y * scale;
        float radiusInPixels = RADIUS * scale;

        // Draw the ball
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

    public void SetAsPotted() {
        isPotted = true;
        body.setTransform(new Vec2(-100, -100), 0);
        body.setLinearVelocity(new Vec2(0, 0));
        body.setAngularVelocity(0);
    }

    public boolean IsPotted() {
        return isPotted;
    }

    public Vec2 GetPosition() {
        return body.getPosition();
    }
}
