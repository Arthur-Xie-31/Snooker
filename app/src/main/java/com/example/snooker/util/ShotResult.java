package com.example.snooker.util;

/**
 * A POJO java Class
 */
public class ShotResult {
    public enum ShotState {
        NO_BALL_IN,
        VALID_POT,
        FOUL,
        FOUL_AND_MISS,
        CUE_BALL_FOUL,
        TOUCHING_BALL,
        FREE_BALL
    }

    private ShotState state;
    private int shotScore = 0;

    public ShotResult(int score) {
        shotScore = score;
        if (score > 0) {
            state = ShotState.VALID_POT;
        } else {
            state = ShotState.NO_BALL_IN;
        }
    }

    public ShotResult(ShotState state) {
        shotScore = 0;
        this.state = state;
    }

    public int GetShotScore() {
        return shotScore;
    }

    public ShotState GetShotState() {
        return state;
    }
}
