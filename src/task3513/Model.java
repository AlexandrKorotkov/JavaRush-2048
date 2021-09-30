package task3513;

import java.util.*;


//Класс Model содержит игровую логику и хранит игровое поле
public class Model {
    private final static int FIELD_WIDTH = 4; //ширина игрового поля


    private Tile[][] gameTiles = new Tile[FIELD_WIDTH][FIELD_WIDTH];
    protected int score = 0; //текущий счет
    protected int maxTile = 0; //максимальный вес плитки на игровом поле

    private Stack<Tile[][]> previousStates = new Stack();//стэк предыдущих состояний игрового поля, исп. для отмены последнего действия
    private Stack<Integer> previousScores = new Stack();//стэк предыдущих очков
    private boolean isSaveNeeded = true;


    public Model() {
        resetGameTiles();
    }


    public void resetGameTiles() {
        //заполнение игрового поля пустыми плитками
        for (int y = 0; y < FIELD_WIDTH; y++) {
            for (int x = 0; x < FIELD_WIDTH; x++) {
                gameTiles[y][x] = new Tile();
            }
        }
        //добавление 2 рандомных плиток с весом
        addTile();
        addTile();
    }

    //изменяет вес радномной свободной плитки (если таковые имеются) на вес 2 или 4 случайным образом (на 9 двоек приходится 1 четверка)
    private void addTile() {
        if (!getEmptyTiles().isEmpty()) {
            int randomTile = (int) (Math.random() * getEmptyTiles().size()) % getEmptyTiles().size();
            getEmptyTiles().get(randomTile).value = Math.random() < 0.9 ? 2 : 4;
        }
    }

    //возвращает список свободных плиток в игровом поле
    private List<Tile> getEmptyTiles() {
        List<Tile> emptyTilesList = new ArrayList<>();
        //одной строкой ради эксперимента.
        Arrays.stream(gameTiles).forEach(tiles -> Arrays.stream(tiles).filter(Tile::isEmpty).forEach(emptyTilesList::add));
        return emptyTilesList;
    }

    //сжатие плиток к левому краю. Пустые плитки всегда справа. Сначала сжатие, потом - слияние.
    //возращает true если произошло смещение
    private boolean compressTiles(Tile[] tiles) {
//        boolean wrongPlacedEmptyTiles = true; //пустые ячейки должны быть справа. Первоначально думаем что они не все справа.
//        //сортировка по убыванию перенесет все пустые ячейки вправою
//        Arrays.sort(tiles, (t1, t2) -> t2.value - t1.value);

       /* for (int i = 0; i < FIELD_WIDTH - 1; i++) {
            if (tiles[i].isEmpty()) {
                for (int j = i; j < FIELD_WIDTH - 1; j++) {
                    tiles[j] = tiles[j + 1];
                }
                tiles[tiles.length - 1].value = 0;

            }
        }*/
        Tile[] oldArr = new Tile[tiles.length];
        System.arraycopy(tiles, 0, oldArr, 0, tiles.length);

        Arrays.sort(tiles, (a, b) -> {
            if (b.value == 0) return a.value == 0 ? 0 : -1;
            else return a.value == 0 ? 1 : 0;
        });
        return !Arrays.equals(tiles, oldArr);
    }

    //слияние плиток одного номинала
    private boolean mergeTiles(Tile[] tiles) { //если не будет работать этот метод, то заменить правильным решением из 6 задачи
        boolean hasChanged = false;

        for (int i = 0; i < 3; i++) {
            //если происходит слияние, то 1 плитка в паре увеличивается в весе, а 2 плитка становится пустой.
            // Так же пропускается ход сравнения. Например, при слиянии tiles[0] и tiles[1]
            // нет смысла проверять tiles[1] и tiles[2]на предмет возможности слияния, т.к. tiles[1] будет = 0.
            // Поэтому сравнение переходит к элементу tiles[2] и tiles[3];
            if (tiles[i].value == tiles[i + 1].value) {
                tiles[i].value += tiles[i + 1].value;
                tiles[i + 1].value = 0;
                maxTile = Math.max(maxTile, tiles[i].value); //обновление максимального веса
                score += tiles[i].value;
                hasChanged = true;
            }
        }
        if (hasChanged) compressTiles(tiles);     //после слияния можно и сжать элементы
        return hasChanged;

    }

    //движение влево. Сдвиг и объединение.
    public void left() {
        if (isSaveNeeded) saveState(gameTiles);
        for (int i = 0; i < FIELD_WIDTH; i++) {
            if (compressTiles(gameTiles[i]) & mergeTiles(gameTiles[i])) {
                addTile();
            }
        }
        isSaveNeeded = true;
    }

    //поворачивает массив с игровым полем на 90 градусов по часовой стрелке
    private void turnTable() {
        Tile[][] res = new Tile[FIELD_WIDTH][FIELD_WIDTH];
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                res[i][j] = gameTiles[FIELD_WIDTH - j - 1][i];
            }
        }
        gameTiles = res;
    }


    //движение вниз. Через поворот, движение влево, и поворот до исходного положения
    public void down() {
        saveState(gameTiles);
        turnTable();
        left();
        turnTable();
        turnTable();
        turnTable();
    }

    //движение вправо. Через поворот, движение влево, и поворот до исходного положения
    public void right() {
        saveState(gameTiles);
        turnTable();
        turnTable();
        left();
        turnTable();
        turnTable();
    }

    //движение вверх. Через поворот, движение влево, и поворот до исходного положения
    public void up() {
        saveState(gameTiles);
        turnTable();
        turnTable();
        turnTable();
        left();
        turnTable();
    }

    public Tile[][] getGameTiles() {
        return gameTiles;
    }

    //true - если в текущей позиции возможно сделать ход так, что бы состояние игрового поля изменилось.
    public boolean canMove() {
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                if (gameTiles[i][j].value == 0) return true;
                //проверка соседних плиток (по вертикали и горизонтали) на возможность слияния
                if (i != 0 && gameTiles[i - 1][j].value == gameTiles[i][j].value) return true;
                if (j != 0 && gameTiles[i][j - 1].value == gameTiles[i][j].value) return true;
            }
        }
        return false;
    }

    //сохраняет текущее игровое состояние и счет в соответствующие стеки
    private void saveState(Tile[][] tiles) {
        //копия игрового поля добавляется в стэк
        Tile[][] gameFieldToStack = new Tile[FIELD_WIDTH][FIELD_WIDTH];
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                gameFieldToStack[i][j] = new Tile(tiles[i][j].value);
            }
        }
        previousStates.push(gameFieldToStack);
        previousScores.push(score);
        isSaveNeeded = false;
    }

    //откат хода. Устанавливает в текущее игрвоое состояние последнее состояние из стэка состояний и стека счета
    public void rollback() {
        if (!previousStates.empty()) {
            if (!previousScores.empty()) {
                gameTiles = previousStates.pop();
                score = previousScores.pop();
            }
        }
    }

    //рандомный ход
    public void randomMove() {
        int random = ((int) (Math.random() * 100)) % 4;
        switch (random) {
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

    //проверяет, отличается ли вес плиток в текущем состоянии игрового поля по сранению с предыдущим состоянием
    public boolean hasBoardChanged() {
        Tile[][] previousGameTable = previousStates.peek();
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                if (gameTiles[i][j].value != previousGameTable[i][j].value) return true;
            }
        }
        return false;
    }

    //определяет эффективность хода
    public MoveEfficiency getMoveEfficiency(Move move) {
        MoveEfficiency moveEfficiency = new MoveEfficiency(-1, 0, move);
        move.move();
        if (hasBoardChanged()) {
            moveEfficiency = new MoveEfficiency(getEmptyTiles().size(), score, move);
        }
        rollback();
        return moveEfficiency;
    }

    //автоматический ход на основе самого эффективного возможного хода
    public void autoMove() {
        PriorityQueue<MoveEfficiency> queue = new PriorityQueue<>(4, Collections.reverseOrder());//вверху очереди будет максимальный элемент
        queue.offer(getMoveEfficiency(this::left));
        queue.offer(getMoveEfficiency(this::up));
        queue.offer(getMoveEfficiency(this::right));
        queue.offer(getMoveEfficiency(this::down));
        queue.peek().getMove().move();
    }

}
