package task3513;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

//Класс Controller следит за нажатием клавиш по время игры
public class Controller extends KeyAdapter {
    private final static int WINNING_TILE = 2048;//победный вес плитки

    private Model model;


    private View view;

    @Override
    public void keyPressed(KeyEvent e) {
        if (KeyEvent.VK_ESCAPE == e.getKeyCode()) resetGame();
        if (!model.canMove()) view.isGameLost = true;
        if (!(view.isGameLost || view.isGameWon)) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT:
                    model.left();
                    break;
                case KeyEvent.VK_RIGHT:
                    model.right();
                    break;
                case KeyEvent.VK_UP:
                    model.up();
                    break;
                case KeyEvent.VK_DOWN:
                    model.down();
                    break;
                case KeyEvent.VK_Z:
                    model.rollback();
                    break;
                case KeyEvent.VK_R:
                    model.randomMove();
                    break;
                case KeyEvent.VK_A:
                    model.autoMove();
                    break;
            }
        }
        if (model.maxTile == WINNING_TILE) view.isGameWon = true;
        view.repaint();

    }


    public Tile[][] getGameTiles() {
        return model.getGameTiles();
    }

    public View getView() {
        return view;
    }

    public int getScore() {
        return model.score;
    }

    public Controller(Model model) {
        this.model = model;
        view = new View(this);
    }

    public void resetGame() {
        model.score = 0;
        view.isGameWon = false;
        view.isGameLost = false;
        model.resetGameTiles();
    }
}
