package ru.invictus.game;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class Controller extends KeyAdapter {
    private Model model;
    private View view;
    private static final int WINNING_TILE = 2048;

    public Tile[][] getGameTiles() {
        return model.getGameTiles();
    }

    public int getScore() {
        return model.score;
    }

    public View getView() {
        return view;
    }

    public Controller(Model model) {
        this.model = model;
        this.view = new View(this);
    }

    public void resetGame() {
        model.score = 0;
        view.isGameWon = false;
        view.isGameLost = false;
        model.resetGameTiles();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        // получаем код нажатой клавиши
        int key = e.getKeyCode();
        // если ESC обновляем игру
        if (key == KeyEvent.VK_ESCAPE) resetGame();
        // если не возможно сделать ход - проигрыш
        if (key == KeyEvent.VK_Z) model.rollback();
        if (key == KeyEvent.VK_R) model.randomMove();
        if (key == KeyEvent.VK_A) model.autoMove();
        if (!model.canMove()) view.isGameLost = true;
        //проверка хода, если возможен
        if (!view.isGameLost && !view.isGameWon) {
            if (key == KeyEvent.VK_LEFT) model.left();
            else if (key == KeyEvent.VK_RIGHT) model.right();
            else if (key == KeyEvent.VK_UP) model.up();
            else if (key == KeyEvent.VK_DOWN) model.down();
        }
        //если получили плитку нужного размера (2048) - победа
        if (model.maxTile == WINNING_TILE) view.isGameWon = true;
        //отрисовываем изменения
        view.repaint();
    }
}
