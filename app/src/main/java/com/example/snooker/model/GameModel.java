package com.example.snooker.model;

import android.graphics.Canvas;

import java.util.HashSet;
import java.util.Set;

public class GameModel {
    // The table and cushions
    private Table table;
    // The cue ball
    private CueBall cueBall;
    // All balls
    private Set<Ball> balls = new HashSet<>();
    private Player player;

    public GameModel(Table table, CueBall cueBall, Set<Ball> balls, Player player) {
        this.table = table;
        this.cueBall = cueBall;
        this.balls.addAll(balls);
        this.player = player;
    }

    public void draw(Canvas canvas) {
        // 1. Draw the table
        table.draw(canvas);

        // 2. Draw balls
        for (Ball ball : balls) {
            ball.draw(canvas);
        }

        // 3. Draw player with aiming UI (Do not draw if the cue action is done)
        if (player.getCurrentState() != Player.GameState.MOVING) {
            player.drawCue(canvas, balls, table, cueBall.GetPosition());
        }
        player.printScore(canvas);
    }
}
