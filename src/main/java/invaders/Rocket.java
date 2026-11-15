package invaders;

import java.awt.*;

/** Ракета игрока: летит вверх, взрывается с уроном по площади. */
public class Rocket {
    public static final int SPEED = 7;
    public static final int BLAST_RADIUS = 40;

    int x, y;
    boolean active = true;

    public Rocket(int x, int y) { this.x = x; this.y = y; }

    void update(long dt) {
        if (!active) return;
        y -= SPEED;
        if (y < -20) active = false;
    }

    void deactivate() { active = false; }

    /** Возвращает центр взрыва, если ракета улетела или её пора детонировать. */
    boolean explodeAt(int px, int py) {
        // детонирует при попадании в пришальца — вызывает Game
        return false;
    }

    void draw(Graphics2D g) {
        if (!active) return;
        // пламя
        g.setColor(new Color(255, 160, 40, 180));
        Polygon flame = new Polygon();
        flame.addPoint(x, y + 14);
        flame.addPoint(x + 6, y + 14);
        flame.addPoint(x + 3, y + 22);
        g.fillPolygon(flame);
        // корпус
        g.setColor(new Color(220, 220, 230));
        g.fillRect(x + 1, y + 4, 4, 10);
        // нос
        g.setColor(new Color(255, 90, 60));
        Polygon nose = new Polygon();
        nose.addPoint(x + 1, y + 4);
        nose.addPoint(x + 5, y + 4);
        nose.addPoint(x + 3, y);
        g.fillPolygon(nose);
    }
}
