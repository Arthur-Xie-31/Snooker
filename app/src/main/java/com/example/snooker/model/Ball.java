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

public abstract class Ball extends Drawable {

    public static final float RADIUS = 4.2f * WORLD_SCALE;
    private static final float MOVE_FRICTION = 0.2f;
    private static final float SPIN_FRICTION = 0.3f;
    // Ball fixture parameters
    private static final float DENSITY = 1.0f;
    private static final float CONTACT_FRICTION = 0f;    // friction when ball hits ball
    private static final float RESTITUTION = 0.8f;    // bounciness when ball hits ball

    protected Body body;
    protected Paint paint;
    protected boolean isPotted = false;

    public Ball(World world, float positionX, float positionY) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyType.DYNAMIC;
        bodyDef.position.set(positionX, positionY);
        // Damping properties for friction
        bodyDef.linearDamping = MOVE_FRICTION;    // Slows down linear movement
        bodyDef.angularDamping = SPIN_FRICTION;    // Slows down rotation (spin)
        bodyDef.allowSleep = false;
        // Set these to prevent tunneling
        bodyDef.fixedRotation = false;  // Allow rotation for spin
        bodyDef.bullet = true;  // This enables continuous collision detection!

        body = world.createBody(bodyDef);

        CircleShape circleShape = new CircleShape();
        circleShape.m_radius = RADIUS;

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circleShape;
        fixtureDef.density = DENSITY;
        fixtureDef.friction = CONTACT_FRICTION;
        fixtureDef.restitution = RESTITUTION;

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

    public boolean IsStopped() {
        if (body.getLinearVelocity().length() < 0.1f) {
            body.setLinearVelocity(new Vec2(0, 0));
            return true;
        } else {
            return false;
        }
    }

    public Vec2 GetPosition() {
        return body.getPosition();
    }

    public boolean WillHit(Vec2 startPoint, Vec2 direction, Vec2 hitPoint) {
        // 1. Calculate the line vertical to the direction line and through start point
        // if the ball is located at opposite of this line, it will never hit.
        float slope = direction.y / direction.x;
        float slopeVertical = - (1f / slope);
        float cVertical = startPoint.y - slopeVertical * startPoint.x;
        // Vertical line: y = slopeVertical * x + cVertical
        float verticalLineYAtballX = slopeVertical * body.getPosition().x + cVertical;
        if (((direction.y > 0) && (body.getPosition().y <= verticalLineYAtballX))
                || ((direction.y < 0) && (body.getPosition().y >= verticalLineYAtballX))) {
            return false;
        }

        // 2. If the ball is located at the same side as direction line,
        // Calculate the distance from the ball to the direction line.
        cVertical = body.getPosition().y - slopeVertical * body.getPosition().x;
        float c = startPoint.y - slope * startPoint.x;
        float times = slopeVertical - slope;
        c = c - cVertical;
        float hitPointX = c / times;
        float hitPointY = slopeVertical * hitPointX + cVertical;
        Vec2 distance = new Vec2(body.getPosition().x - hitPointX, body.getPosition().y - hitPointY);
        if (distance.length() < Ball.RADIUS * 2) {
            float dist = distance.length();
            float delta = (float) Math.sqrt(Math.pow(Ball.RADIUS * 2, 2) - Math.pow(dist, 2));
            float deltaX = (float) Math.sqrt((delta * delta) / (slope * slope + 1));
            float deltaY = Math.abs(slope * deltaX);
            hitPoint.x = (startPoint.x > body.getPosition().x) ? hitPointX + deltaX : hitPointX - deltaX;
            hitPoint.y = (startPoint.y > body.getPosition().y) ? hitPointY + deltaY : hitPointY - deltaY;;
            return true;
        } else {
            return false;
        }
    }
}
