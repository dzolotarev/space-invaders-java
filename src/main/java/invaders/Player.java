package invaders;

import java.awt.*;

/** Корабль игрока в нижней части экрана. */
public class Player {
    public static final int WIDTH = 34;
    public static final int HEIGHT = 18;
    private static final int SPEED = 5;

    int x, y;
    private boolean movingLeft, movingRight;

    public Player(int x, int y) { this.x = x; this.y = y; }

    void moveLeft()  { movingLeft = true; }
    void moveRight() { movingRight = true; }
    void stopMoving() { movingLeft = movingRight = false; }

    void update(long dt) {
        if (movingLeft)  x -= SPEED;
        if (movingRight) x += SPEED;
        x = Math.max(0, Math.min(Game.WIDTH - WIDTH, x));
    }

    void reset(int x, int y) {
        this.x = x; this.y = y;
        movingLeft = movingRight = false;
    }

    boolean hit(int px, int py) {
        return px >= x && px <= x + WIDTH && py >= y && py <= y + HEIGHT;
    }

    void draw(Graphics2D g) {
        // свечение под кораблём
        g.setColor(new Color(60, 180, 255, 60));
        g.fillOval(x + 4, y + HEIGHT - 2, WIDTH - 8, 8);

        // корпус — трапеция
        g.setColor(new Color(120, 220, 255));
        Polygon body = new Polygon();
        body.addPoint(x + 2, y + HEIGHT);
        body.addPoint(x + WIDTH - 2, y + HEIGHT);
        body.addPoint(x + WIDTH - 8, y + 6);
        body.addPoint(x + 8, y + 6);
        g.fillPolygon(body);

        // «башня»
        g.setColor(new Color(180, 240, 255));
        g.fillRect(x + 12, y + 2, 10, 8);

        // пушка
        g.setColor(Color.WHITE);
        g.fillRect(x + 15, y - 2, 4, 6);

        // огни
        g.setColor(new Color(255, 120, 120));
        g.fillRect(x + 4, y + HEIGHT - 4, 3, 3);
        g.setColor(new Color(120, 255, 120));
        g.fillRect(x + WIDTH - 7, y + HEIGHT - 4, 3, 3);
    }
}
