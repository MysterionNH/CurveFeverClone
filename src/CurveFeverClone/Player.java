package CurveFeverClone;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;

import static CurveFeverClone.GUI.DEFAULT_LINE_WIDTH;

class Player {
    private static final double MAX_VELOCITY = 1.6;

    private final SimpleStringProperty name;

    private Color color = Color.TRANSPARENT;

    private Point newPosition = new Point();
    private Point oldPosition = new Point();
    private Point velocity = new Point();

    private int angle = 0;
    private KeyCode leftKeyCode;
    private KeyCode rightKeyCode;

    private int drawCount = 0;

    private boolean draw = false;
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
    }

    boolean getAlive() {
        return alive;
    }

    void setAlive(boolean _alive) {
        alive = _alive;
    }

    int updateDrawCount() {
        drawCount++;
        return drawCount;
    }

    int getDrawCount() {
        return drawCount;
    }

    void setDrawCount(int _drawCount) {
        drawCount = _drawCount;
    }

    boolean getDraw() {
        return draw;
    }

    void setDraw(boolean _draw) {
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

    KeyCode getLeftKeyCode() {
        return leftKeyCode;
    }

    KeyCode getRightKeyCode() {
        return rightKeyCode;
    }

    int getLineWidth() {
        return DEFAULT_LINE_WIDTH;
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

    Point getVelocity() {
        return velocity;
    }
}