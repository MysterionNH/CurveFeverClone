package CurveFeverClone;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;

class Player {
    static final double MAX_VELOCITY = 1.6;
    static final int MAX_SIZE = 13;

    private Color savedSquare[][] = new Color[MAX_SIZE][MAX_SIZE];

    private final SimpleStringProperty name;

    private Color color = Color.TRANSPARENT;

    private Point newPosition = new Point();
    private Point oldPosition = new Point();
    private Point velocity = new Point();

    private int lineWidth = GUI.DEFAULT_LINE_WIDTH;

    private int angle = 0;
    private KeyCode leftKeyCode;
    private KeyCode rightKeyCode;

    private int drawCount = 0;

    private boolean draw = true;//false;
    private boolean alive = true;

    Player() {
        this("dummy", Color.GREEN, new Point(0, 0), 0, KeyCode.CLEAR, KeyCode.CLEAR);
    }

    Player(String _name, Color _color, Point _spawn, int _angle, KeyCode _leftKeyCode, KeyCode _rightKeyCode) {
        name = new SimpleStringProperty(_name);
        color = _color;

        oldPosition.copy(_spawn);
        newPosition.copy(_spawn);

        angle = _angle;
        updateVelocity();

        leftKeyCode = _leftKeyCode;
        rightKeyCode = _rightKeyCode;

        for (int x = 0; x < savedSquare.length; x++) {
            for (int y = 0; y < savedSquare[x].length; y++) {
                savedSquare[x][y] = Color.TRANSPARENT;
            }
        }
    }

    void setName(String _name) { this.name.set(_name); }

    void setLeftKeyCode(KeyCode _left) { leftKeyCode = _left; }

    void setRightKeyCode(KeyCode _right) { rightKeyCode = _right; }

    boolean getAlive() {
        return alive;
    }

    void setAlive(boolean _alive) {
        alive = _alive;
    }

    public int updateDrawCount() {
        drawCount++;
        return drawCount;
    }

    public int getDrawCount() {
        return drawCount;
    }

    public void setDrawCount(int _drawCount) {
        drawCount = _drawCount;
    }

    public boolean getDraw() {
        return draw;
    }

    public void setDraw(boolean _draw) {
        if (_draw) { // reset saved square
            for (int x = 0; x < savedSquare.length; x++) {
                for (int y = 0; y < savedSquare[x].length; y++) {
                    savedSquare[x][y] = Color.TRANSPARENT;
                }
            }
        }
        draw = _draw;
    }

    Point getOldPosition() {
        return oldPosition;
    }

    Point getNewPosition() {
        return newPosition;
    }

    void setNewPosition(Point _newPosition) {
        newPosition.copy(_newPosition);
    }

    String getName() {
        return name.get();
    }

    Color getColor() {
        return color;
    }

    void setColor(Color _color) {
        color = _color;
    }

    KeyCode getLeftKeyCode() {
        return leftKeyCode;
    }

    KeyCode getRightKeyCode() {
        return rightKeyCode;
    }

    int getLineWidth() {
        return lineWidth;
    }

    void setLineWidth(int _lineWidth) {
        lineWidth = _lineWidth;
    }

    void move() {
        oldPosition.copy(newPosition);

        newPosition.x += velocity.x;
        newPosition.y += velocity.y;
    }

    void turnLeft() {
        angle = (((angle - 10) < 0) ? 350 : (angle - 10));
        updateVelocity();
    }

    void turnRight() {
        angle += (((angle + 10) >= 360) ? (-angle) : 10);
        updateVelocity();
    }

    private void updateVelocity() {
        velocity.x = MAX_VELOCITY * Math.cos(Math.toRadians(angle));
        velocity.y = MAX_VELOCITY * Math.sin(Math.toRadians(angle));
    }

    public Point getVelocity() {
        return velocity;
    }


    public Color[][] getSavedSquare() {
        return savedSquare;
    }

    public void setSavedSquare(Color[][] _removedSquare) {
        for (int r = 0; r < _removedSquare.length; r++) {
            for (int c = 0; c < _removedSquare[r].length; c++) {
                savedSquare[r][c] = _removedSquare[r][c];
            }
        }
    }
}