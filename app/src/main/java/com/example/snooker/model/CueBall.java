package com.example.snooker.model;

import android.graphics.Color;
import android.util.Log;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;

import java.util.Set;

public class CueBall extends Ball{
    public CueBall(World world, float positionX, float positionY) {
        super(world, positionX, positionY);
        paint.setColor(Color.WHITE);
    }

    public void CueAction(Vec2 initialVelocity) {
        body.setLinearVelocity(new Vec2(initialVelocity));
        body.setAngularVelocity(0);
    }

    public void PlaceInDArea(Vec2 position, Set<Ball> allBalls) {
        float x = position.x;
        float y = position.y;
        if (y > Table.LENGTH / 5) {
            y = Table.LENGTH / 5;
        }

        float dx = x - (Table.WIDTH / 2);
        float dy = y - (Table.LENGTH / 5);
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        if (distance > (Table.WIDTH / 6)) {
            float ratio = (Table.WIDTH / 6) / distance;
            dx *= ratio;
            dy *= ratio;
            x = (Table.WIDTH / 2) + dx;
            y = (Table.LENGTH / 5) + dy;
        }

        Vec2 placePosition = new Vec2(x, y);
        for (Ball ball : allBalls) {
            if (ball instanceof CueBall) continue;
            if (ball.IsPotted()) continue;
            ball.CheckCoincide(placePosition);
        }

        SetPosition(placePosition);
    }
}
