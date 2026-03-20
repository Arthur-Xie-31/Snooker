package com.example.snooker.model;

import android.graphics.Color;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;

public class CueBall extends Ball{
    public CueBall(World world, float positionX, float positionY) {
        super(world, positionX, positionY);
        paint.setColor(Color.WHITE);
    }

    public void CueAction(final float x, final float y) {
        body.setLinearVelocity(new Vec2(x, y));
    }
}
