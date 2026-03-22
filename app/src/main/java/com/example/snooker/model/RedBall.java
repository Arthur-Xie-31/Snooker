package com.example.snooker.model;

import android.graphics.Color;

import org.jbox2d.dynamics.World;

public class RedBall extends Ball{

    protected int score = 1;

    public RedBall (World world, float positionX, float positionY) {
        super(world, positionX, positionY);
        paint.setColor(Color.RED);
    }

    public int GetScore () {
        return score;
    }
}
