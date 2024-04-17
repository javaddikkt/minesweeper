import com.javarush.engine.cell.*;

import java.util.ArrayList;
import java.util.List;

public class MinesweeperGame extends Game {
    private static final int SIDE = 11;
    private static int CHANCE;
    private final GameObject[][] gameField = new GameObject[SIDE][SIDE];
    private int countMinesOnField = 0;
    private int countFlags;
    private int countClosedTiles = SIDE * SIDE;
    private int countMoves;
    private boolean isGameStopped = false;
    private boolean isLevelChoosing = true;
    private static final String MINE = "\uD83D\uDCA3";
    private static final String FLAG = "\uD83D\uDEA9";

    @Override
    public void initialize() {
        setScreenSize(SIDE, SIDE);
        chooseLevel();
    }

    private void createField(){
        countMinesOnField = 0;
        for (int y = 0; y < SIDE; y++) {
            for (int x = 0; x < SIDE; x++) {
                if (getRandomNumber(CHANCE) == 1) {
                    gameField[y][x] = new GameObject(x, y, true);
                    countMinesOnField++;
                } else {
                    gameField[y][x] = new GameObject(x, y, false);
                }
                setCellValueEx(x, y, Color.DARKGREEN, "", Color.WHITE, 50);
            }
        }
        countMoves = 0;
        countFlags = countMinesOnField;
        countMineNeighbors();
    }

    private void chooseLevel(){
        isLevelChoosing = true;
        countMoves = 0;
        for (int y = 0; y < SIDE; y++) {
            for (int x = 0; x < SIDE; x++) {
                if (y == 5 && x == 4){
                    setCellValueEx(x, y, Color.LIGHTGREEN, "easy", Color.BLACK, 20);
                }
                else if (y == 5 && x == 5){
                    setCellValueEx(x, y, Color.KHAKI, "medium", Color.BLACK, 20);
                }
                else if (y == 5 && x == 6){
                    setCellValueEx(x, y, Color.SALMON, "hard", Color.BLACK, 20);
                }
                else {
                    setCellValueEx(x, y, Color.DARKGREEN, "", Color.WHITE, 50);
                }
            }
        }
    }

    private void openTile(int x, int y) {
        GameObject object = gameField[y][x];
        if (object.isOpen || object.isFlag || isGameStopped) {
            return;
        }
        countMoves++;
        if (object.isMine) {
            if (countMoves == 1){
                createField();
                openTile(x, y);
            }
            else{
                setCellValueEx(x, y, Color.GREY, MINE, Color.DARKRED);
                gameOver();
            }
            return;
        } else if (object.countMineNeighbors == 0) {
            setCellValue(x, y, "");
            gameField[y][x].isOpen = true;
            countClosedTiles--;
            setCellColor(x, y, Color.SADDLEBROWN);
            for (GameObject neighbor : getNeighbors(object)) {
                if (!neighbor.isOpen && !neighbor.isMine && !neighbor.isFlag) {
                    openTile(neighbor.x, neighbor.y);
                }
            }
        } else {
            if (countMoves == 1){
                createField();
                openTile(x, y);
            }
            else{
                setCellValueEx(x, y, Color.SADDLEBROWN, Integer.toString(gameField[y][x].countMineNeighbors), Color.WHITE, 50);
                gameField[y][x].isOpen = true;
                countClosedTiles--;
            }
        }
        if (countClosedTiles == countMinesOnField) {
            win();
        }
    }

    private void markTile(int x, int y) {
        if (!isGameStopped) {
            GameObject object = gameField[y][x];
            if (object.isOpen || (countFlags == 0 && !object.isFlag)) {
                return;
            }
            if (!object.isFlag) {
                object.isFlag = true;
                countFlags--;
                setCellValueEx(x, y, Color.DARKGREEN, FLAG, Color.WHITE, 50);
            } else {
                object.isFlag = false;
                countFlags++;
                setCellValue(x, y, "");
            }
        }
    }

    private void openNeighbors(int x, int y) {
        GameObject object = gameField[y][x];
        int countFlagsAround = 0;
        List<GameObject> neighbors = getNeighbors(object);
        for (GameObject neighbor : neighbors){
            if (neighbor.isFlag){
                countFlagsAround++;
            }
        }
        if (countFlagsAround >= object.countMineNeighbors){
            for (GameObject neighbor : neighbors){
                openTile(neighbor.x, neighbor.y);
            }
        }
    }

    private void restart() {
        isGameStopped = false;
        countMinesOnField = 0;
        countClosedTiles = SIDE * SIDE;
        chooseLevel();
    }

    private void gameOver() {
        isGameStopped = true;
        showMessageDialog(Color.BLACK, "YOU LOSE [restart]", Color.RED, 50);
    }

    private void win() {
        isGameStopped = true;
        showMessageDialog(Color.BLACK, "YOU WIN [restart]", Color.DARKGREEN, 50);
    }

    @Override
    public void onMouseLeftClick(int x, int y) {
        if (isGameStopped) {
            restart();
        }
        else if (isLevelChoosing){
            if (y == 5 && x == 4){
                CHANCE = 10;
                isLevelChoosing = false;
                createField();
            }
            else if (y == 5 && x == 5){
                CHANCE = 8;
                isLevelChoosing = false;
                createField();
            }
            else if (y == 5 && x == 6){
                CHANCE = 6;
                isLevelChoosing = false;
                createField();
            }
        }
        else if (gameField[y][x].isOpen && gameField[y][x].countMineNeighbors > 0){
            openNeighbors(x, y);
        }
        else {
            openTile(x, y);
        }
    }

    @Override
    public void onMouseRightClick(int x, int y) {
        markTile(x, y);
    }


    private List<GameObject> getNeighbors(GameObject object) {
        List<GameObject> neighbors = new ArrayList<>();
        for (int y = object.y - 1; y < object.y + 2; y++) {
            for (int x = object.x - 1; x < object.x + 2; x++) {
                if (y < 0 || y >= SIDE) {
                    continue;
                }
                if (x < 0 || x >= SIDE) {
                    continue;
                }
                if (x == object.x && y == object.y) {
                    continue;
                }
                neighbors.add(gameField[y][x]);
            }
        }
        return neighbors;
    }

    private void countMineNeighbors() {
        for (int y = 0; y < SIDE; y++) {
            for (int x = 0; x < SIDE; x++) {
                GameObject gameObject = gameField[y][x];
                if (!gameObject.isMine) {
                    for (GameObject neighbor : getNeighbors(gameObject)) {
                        if (neighbor.isMine) {
                            gameObject.countMineNeighbors++;
                        }
                    }
                }
            }
        }
    }

}


