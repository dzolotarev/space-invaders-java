package invaders;

import java.awt.*;

/** Пуля игрока, летит вверх. */
public class Bullet {
    public static final int SPEED = 9;
    int x, y;
    boolean active;

    public Bullet(int x, int y) { this.x = x; this.y = y; active = true; }

    void reset(int x, int y) { this.x = x; this.y = y; active = true; }

    void update(long dt) {
        if (!active) return;
        y -= SPEED;
        if (y < 0) active = false;
    }

    void deactivate() { active = false; }

    void draw(Graphics2D g) {
        // хвост
        g.setColor(new Color(120, 220, 255, 80));
        g.fillRect(x - 1, y + 6, 6, 8);
        // ядро
        g.setColor(Color.WHITE);
        g.fillRect(x, y, 4, 10);
        g.setColor(new Color(120, 220, 255));
        g.fillRect(x + 1, y + 2, 2, 6);
    }
}
