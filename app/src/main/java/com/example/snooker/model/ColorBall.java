package com.example.snooker.model;

import android.graphics.Color;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;

import java.util.Set;

// Color balls can be regarded as special red balls with the ability to replace
public class ColorBall extends RedBall{

    public ColorBall(World world, float positionX, float positionY, int score) {
        super(world, positionX, positionY);
        this.score = score;
        switch (score) {
            case 2:
                paint.setColor(Color.YELLOW);
                break;
            case 3:
                paint.setColor(Color.rgb(5, 71, 5));
                break;
            case 4:
                paint.setColor(Color.rgb(110, 74, 21));
                break;
            case 5:
                paint.setColor(Color.BLUE);
                break;
            case 6:
                paint.setColor(Color.rgb(250, 132, 217));
                break;
            case 7:
                paint.setColor(Color.BLACK);
            default:
                break;
        }
    }

    public void Replace(Set<Ball> allRemainingBalls) {
        // TODO: Consider if the default position has been placed
        ResetToDefaultPlace();
    }
}
