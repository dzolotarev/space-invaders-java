package invaders;

import java.awt.*;

/** Выстрел пришальца, летит вниз. */
public class EnemyShot {
    public static final int SPEED = 5;
    int x, y;
    boolean active = true;

    public EnemyShot(int x, int y) { this.x = x; this.y = y; }

    void update(long dt) {
        if (!active) return;
        y += SPEED;
        if (y > Game.HEIGHT) active = false;
    }

    void deactivate() { active = false; }

    void draw(Graphics2D g) {
        // хвост
        g.setColor(new Color(255, 80, 200, 70));
        g.fillRect(x - 1, y - 6, 5, 6);
        // ядро — зигзаг
        g.setColor(new Color(255, 120, 220));
        g.fillRect(x, y, 3, 4);
        g.fillRect(x + 2, y + 4, 3, 4);
        g.fillRect(x, y + 8, 3, 4);
    }
}
