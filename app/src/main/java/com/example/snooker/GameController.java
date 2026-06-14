package com.example.snooker;

import com.example.snooker.model.Ball;
import com.example.snooker.model.ColorBall;
import com.example.snooker.model.CueBall;
import com.example.snooker.model.Player;
import com.example.snooker.model.RedBall;
import com.example.snooker.model.Table;
import com.example.snooker.util.ShotResult;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class GameController {
    private GameView gameView;

    // JBox2D World
    private final World world;
    // The table and cushions
    private Table table;
    // The cue ball
    private CueBall cueBall;
    // All the balls on table
    private Set<Ball> allRemainingBalls = new HashSet<>();
    // Still store potted balls so we can replace them on table when restarting a new game
    private Set<Ball> pottedBalls = new HashSet<>();
    // Red balls are not required to be ordered
    private Set<RedBall> redBalls = new HashSet<>();
    // Color balls are always ordered by their points
    private TreeSet<RedBall> colorBalls = new TreeSet<>(Comparator.comparingInt(RedBall::GetScore));
    // Whether current round to pot red ball or color ball
    private boolean isTargetRed = true;
    private boolean isCleanColor = false;
    private Player currentPlayer;

    GameController(GameView gameView) {
        this.gameView = gameView;
        // 0. Create physical world
        world = new World(new Vec2(0.0f, 0.0f)); // Zero gravity
        // 1. Create the table and cushions
        table = new Table(world);

        // 2. Create balls
        // 2.1 Create red balls
        float currentX = Table.WIDTH / 2;
        float currentY = (Table.LENGTH / 4 * 3) + (Ball.RADIUS * 2);
        for (int i = 1; i <= 5; i++) {
            for (int j = 0; j < i; j++) {
                redBalls.add(new RedBall(world, currentX, currentY));
                currentX += (Ball.RADIUS * 2);
            }
            currentY += (float) (Ball.RADIUS * Math.sqrt(3.1d));
            currentX = (Table.WIDTH / 2) - (Ball.RADIUS * i);
        }
        allRemainingBalls.addAll(redBalls);
        // 2.2 Create color balls
        colorBalls.add(new ColorBall(world, Table.WIDTH / 3, Table.LENGTH / 5, 2));  // Yellow ball
        colorBalls.add(new ColorBall(world, Table.WIDTH / 3 * 2, Table.LENGTH / 5, 3));  // Green ball
        colorBalls.add(new ColorBall(world, Table.WIDTH / 2, Table.LENGTH / 5, 4));  // Brown ball
        colorBalls.add(new ColorBall(world, Table.WIDTH / 2, Table.LENGTH / 2, 5));  // Blue ball
        colorBalls.add(new ColorBall(world, Table.WIDTH / 2, Table.LENGTH / 4 * 3, 6));  // Pink ball
        colorBalls.add(new ColorBall(world, Table.WIDTH / 2, Table.LENGTH / 11 * 10, 7));  // Black ball
        allRemainingBalls.addAll(colorBalls);
        // 2.3 Create the cue ball
        cueBall = new CueBall(world, Table.WIDTH / 12 * 5, Table.LENGTH / 5);  // Cue ball
        allRemainingBalls.add(cueBall);

        // 3. Create player
        currentPlayer = new Player("Mark Selby", world, cueBall.GetPosition());

        // 4. Set GameModel for GameView to draw
        gameView.setGameModel(table, allRemainingBalls, currentPlayer);
    }

    public void updatePhysics(float deltaTime) {
        playOneStep(deltaTime);
        gameView.invalidate();
    }

    private void RestartGame() {
        for (Ball ball: pottedBalls) {
            if (ball instanceof ColorBall) {
                colorBalls.add((ColorBall) ball);
            } else if (ball instanceof RedBall) {
                redBalls.add((RedBall) ball);
            }
        }
        allRemainingBalls.addAll(pottedBalls);
        pottedBalls.clear();

        for (Ball ball : allRemainingBalls) {
            ball.ResetToDefaultPlace();
        }

        currentPlayer.toBreak();
    }

    public boolean HandleTouchEvent(Vec2 touchPoint, boolean isTouchFinished) {
        switch (currentPlayer.getCurrentState()) {
            case PLACING_CUE_BALL:
                if (!isTouchFinished) {
                    // Update cue ball position
                    cueBall.PlaceInDArea(touchPoint, allRemainingBalls);
                } else {
                    // Finish placing cue ball
                    currentPlayer.onActionFinish();
                }
                return true;
            case AIMING:
                if (!isTouchFinished) {
                    // Update aim direction
                    currentPlayer.Aiming(touchPoint);
                } else {
                    // Finish aiming
                    currentPlayer.onActionFinish();
                }
                return true;
            case FEATHERING:
                if (!isTouchFinished) {
                    // Update cue power
                    currentPlayer.feathering(touchPoint, cueBall.GetPosition());
                } else {
                    // decided aiming direction and cueing power, start to cue
                    currentPlayer.decideCue();
                }
                return true;
            case WON:
                // User tap the screen to kick off a new game
                if (isTouchFinished) {
                    RestartGame();
                }
                return true;
            default:
                // Other situations do not required user to interact
                return false;
        }
    }

    private void playOneStep(float deltaTime) {
        // 1. Update the physics simulation
        world.step(deltaTime, 10, 10);

        // 2.0 Check if cue tip hit the cue ball
        if (currentPlayer.getCurrentState() == Player.PlayerState.CUEING) {
            currentPlayer.Cueing(cueBall);
        }

        // 2.1 Check potted balls in the last frame
        table.CheckPottedBalls(allRemainingBalls);

        // 2.2 Check if all balls have stopped moving, if so, check foul or not
        if ((currentPlayer.getCurrentState() == Player.PlayerState.MOVING) && isAllBallStopped()) {
            // TODO: Support foul and miss
            // TODO: Consider if the first hit ball is not the target
            ShotResult shotResult = CheckFoulAndCollectScore();
            switch (shotResult.GetShotState()) {
                case VALID_POT:
                    currentPlayer.AddBreak(shotResult.GetShotScore());
                    isTargetRed = !isTargetRed;
                    break;
                case NO_BALL_IN:
                    currentPlayer.onNoBallPotted();
                    isTargetRed = true;
                    break;
                case CUE_BALL_FOUL:
                    currentPlayer.onCueBallFoul();
                    isTargetRed = true;
                    break;
                case FOUL:
                    currentPlayer.onFoul();
                    isTargetRed = true;
                    break;
                default:
                    // Should not hit there...
            }

            // Check if won the frame
            if (redBalls.isEmpty() && colorBalls.isEmpty()) {
                currentPlayer.onWin();
            }
            // Check if it is color cleaning now
            if (redBalls.isEmpty() && isTargetRed) {
                isTargetRed = false;
                isCleanColor = true;
            }
        }
    }

    private boolean isAllBallStopped() {
        if (!cueBall.IsStopped()) return false;
        for (Ball ball: allRemainingBalls) {
            if (!ball.IsStopped()) return false;
        }
        return true;
    }

    private ShotResult CheckFoulAndCollectScore() {
        Set<RedBall> targetBalls = isTargetRed ? redBalls
                : isCleanColor ? Set.of(colorBalls.first()) : colorBalls;
        boolean isFoul = false;
        boolean isCueBallFoul = false;
        int score = 0;
        Set<RedBall> pottedBallsInThisShot = new HashSet<>();

        // 1. Check foul and collect score first
        for (Ball ball : allRemainingBalls) {
            if (ball.IsPotted()) {
                // 1.1 Cue ball fall, foul and replace cue ball to D area;
                if (ball instanceof CueBall) {
                    isFoul = true;
                    isCueBallFoul = true;
                    cueBall.ResetToDefaultPlace();
                } else if (ball instanceof RedBall) {
                    RedBall pottedBall = (RedBall) ball;
                    if (!targetBalls.contains(pottedBall)) {
                        // 1.2 color ball fall when target red ball, or red ball fall when target color ball
                        isFoul = true;
                    } else {
                        // 1.3 Valid goal
                        score += pottedBall.GetScore();
                    }
                    pottedBallsInThisShot.add(pottedBall);
                }
            }
        }

        // 2. replace potted balls as needed
        for (RedBall pottedBall : pottedBallsInThisShot) {
            if (pottedBall instanceof ColorBall) {
                ColorBall pottedColorBall = (ColorBall) pottedBall;
                if (isFoul || !isCleanColor) {
                    // TODO: Consider if the default position has been placed
                    pottedColorBall.Replace(allRemainingBalls);
                } else {
                    pottedBalls.add(pottedColorBall);
                    colorBalls.remove(pottedColorBall);
                }
            } else {
                pottedBalls.add(pottedBall);
                redBalls.remove(pottedBall);
            }
        }
        allRemainingBalls.removeAll(pottedBalls);

        // 3. Decide whether it's a foul or valid shot
        if (isCueBallFoul) {
            return new ShotResult(ShotResult.ShotState.CUE_BALL_FOUL);
        } else if (isFoul){
            return new ShotResult(ShotResult.ShotState.FOUL);
        } else {
            return new ShotResult(score);
        }
    }
}
