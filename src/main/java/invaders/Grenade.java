package invaders;

import java.awt.*;

/** Граната: летит вверх, взрывается по площади (меньше, чем ракета). */
public class Grenade {
    public static final int SPEED = 6;
    public static final int BLAST_RADIUS = 30;

    int x, y;
    boolean active = true;

    public Grenade(int x, int y) { this.x = x; this.y = y; }

    void update(long dt) {
        if (!active) return;
        y -= SPEED;
        if (y < -20) active = false;
    }

    void deactivate() { active = false; }

    void draw(Graphics2D g) {
        if (!active) return;
        // корпус
        g.setColor(new Color(120, 200, 90));
        g.fillOval(x, y, 8, 10);
        // «зажигалка»
        g.setColor(Color.YELLOW);
        g.fillRect(x + 3, y - 4, 2, 5);
        g.setColor(new Color(255, 120, 40));
        g.fillRect(x + 2, y - 6, 4, 3);
    }
}
