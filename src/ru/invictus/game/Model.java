package ru.invictus.game;

import java.util.*;

public class Model {
    private static final int FIELD_WIDTH = 4;
    private Tile[][] gameTiles;
    private Stack<Tile[][]> previousStates = new Stack<>();
    private Stack<Integer> previousScores = new Stack<>();
    private boolean isSaveNeeded = true;

    protected int score;
    protected int maxTile;

    public Model() {
        resetGameTiles();
    }

    public Tile[][] getGameTiles() {
        return gameTiles;
    }

    public boolean canMove() {
        if (!getEmptyTiles().isEmpty()) {
            return true;
        } else {
            for (int k = 0; k < FIELD_WIDTH -1; k++) {
                for (int i = 0; i < FIELD_WIDTH - 1; i++) {
                    if (gameTiles[k][i].value == gameTiles[k][i + 1].value || gameTiles[k][i].value == gameTiles[k + 1][i].value) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    protected void resetGameTiles() {
        this.gameTiles = new Tile[FIELD_WIDTH][FIELD_WIDTH];
        this.score = 0;
        this.maxTile = 2;
        for (int i = 0; i < gameTiles.length; i++) {
            for (int j = 0; j < gameTiles.length; j++) {
                gameTiles[i][j] = new Tile();
            }
        }
        addTile();
        addTile();
    }

    private void addTile() {
        List<Tile> emptyTiles = getEmptyTiles();
        if (emptyTiles.isEmpty()) {
            return;
        }
        emptyTiles.get((int) (Math.random() * emptyTiles.size())).value = (Math.random() < 0.9 ? 2 : 4);
    }

    private List<Tile> getEmptyTiles() {
        ArrayList<Tile> emptyTiles = new ArrayList<>();
        for (int i = 0; i < gameTiles.length; i++) {
            for (int j = 0; j < gameTiles.length; j++) {
                if (gameTiles[i][j].isEmpty()) emptyTiles.add(gameTiles[i][j]);
            }
        }
        return emptyTiles;
    }

    private boolean compressTiles(Tile[] tiles) {
        boolean change = false;
        for (int k = 0; k < tiles.length-1; k++) {
            for (int i = 0; i < tiles.length-1; i++) {
                if (tiles[i].isEmpty() & !tiles[i+1].isEmpty()) {
                    tiles[i].value = tiles[i + 1].value;
                    tiles[i+1].value = 0;
                    change = true;
                }
            }
        }
        return change;
    }

    private boolean mergeTiles(Tile[] tiles) {
        boolean change = false;
        if (tiles[0].value == tiles[1].value && !tiles[0].isEmpty()) {
            int score = tiles[0].value * 2;
            this.score += score;
            change = true;
            if (score > maxTile) maxTile = score;
            tiles[0].value = score;
            tiles[1].value = tiles[2].value;
            tiles[2].value = tiles[3].value;
            tiles[3].value = 0;
        }
        if (tiles[1].value == tiles[2].value && !tiles[1].isEmpty()) {
            int score = tiles[1].value * 2;
            this.score += score;
            change = true;
            if (score > maxTile) maxTile = score;
            tiles[1].value = score;
            tiles[2].value = tiles[3].value;
            tiles[3].value = 0;
        }
        if (tiles[2].value == tiles[3].value && !tiles[2].isEmpty()) {
            int score = tiles[2].value * 2;
            this.score += score;
            change = true;
            if (score > maxTile) maxTile = score;
            tiles[2].value = score;
            tiles[3].value = 0;
        }
        return change;
    }

    public void left() {
        if (isSaveNeeded) saveState(gameTiles);
        boolean change = false;
        for (int i = 0; i < FIELD_WIDTH; i++) {
            if (compressTiles(gameTiles[i]) | mergeTiles(gameTiles[i])) change = true;
            }
        if (change) addTile();
        isSaveNeeded = true;
    }

    public void right() {
        saveState(gameTiles);
        rotate();
        rotate();
        left();
        rotate();
        rotate();
    }

    public void down() {
        saveState(gameTiles);
        rotate();
        left();
        rotate();
        rotate();
        rotate();
    }

    public void up() {
        saveState(gameTiles);
        rotate();
        rotate();
        rotate();
        left();
        rotate();
    }

    private void rotate() {
        Tile[][] rotateTile = new Tile[FIELD_WIDTH][FIELD_WIDTH];
        int n = FIELD_WIDTH - 1;
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                rotateTile[j][n] = gameTiles[i][j];
            }
            n--;
        }
        gameTiles = rotateTile;
    }

    private void saveState(Tile[][] tiles) {
        Tile[][] copyTile = new Tile[FIELD_WIDTH][FIELD_WIDTH];
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                copyTile[i][j] = new Tile();
                copyTile[i][j].value = tiles[i][j].value;
            }
        }
        previousStates.push(copyTile);
        previousScores.push(score);
        isSaveNeeded = false;
    }

    public void rollback() {
        if (!previousStates.empty() && !previousScores.empty()) {
            gameTiles = previousStates.pop();
            score = previousScores.pop();
        }
    }

    public void randomMove() {
        switch (((int) (Math.random() * 100)) % 4) {
            case 0:
                left();
                break;
            case 1:
                right();
                break;
            case 2:
                up();
                break;
            case 3:
                down();
                break;
        }
    }

    public boolean hasBoardChanged() {
        int a = 0;
        int b = 0;
        Tile[][] prev = previousStates.peek();
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                a += gameTiles[i][j].value;
                b += prev[i][j].value;
            }
        }
        return a != b;
    }

    public MoveEfficiency getMoveEfficiency(Move move) {
        move.move();
        if (hasBoardChanged()) {
            int moveEmpty = getEmptyTiles().size();
            int moveScore = score;
            rollback();
            return new MoveEfficiency(moveEmpty, moveScore, move);
        } else {
            rollback();
            return new MoveEfficiency(-1, 0, move);
        }
    }

    public void autoMove() {
        PriorityQueue<MoveEfficiency> priority = new PriorityQueue<>(4, Collections.reverseOrder());
        priority.add(getMoveEfficiency(this::left));
        priority.add(getMoveEfficiency(this::right));
        priority.add(getMoveEfficiency(this::up));
        priority.add(getMoveEfficiency(this::down));
        priority.poll().getMove().move();
    }
}
