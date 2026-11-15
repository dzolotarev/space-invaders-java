package invaders;

import java.awt.*;

/** Босс в самом верху: летает горизонтально, 20 HP. */
public class Boss {
    public static final int WIDTH = 96;
    public static final int HEIGHT = 44;
    private static final int SPEED = 3;
    private static final int MAX_HP = 20;

    int x, y;
    boolean visible = false;
    private int hp = MAX_HP;
    private int dirX = 1;

    public Boss(int x, int y) { this.x = x; this.y = y; }

    void setVisible(boolean v) {
        visible = v;
        if (v) { hp = MAX_HP; x = Game.WIDTH / 2 - WIDTH / 2; }
    }

    void update(long dt) {
        if (!visible) return;
        x += dirX * SPEED;
        if (x < 20 || x + WIDTH > Game.WIDTH - 20) dirX *= -1;
    }

    boolean hitTest(int px, int py) {
        return visible && px >= x && px <= x + WIDTH && py >= y && py <= y + HEIGHT;
    }

    void damage() { hp--; }

    boolean dead() { return hp <= 0; }

    void draw(Graphics2D g, long frame) {
        if (!visible) return;
        boolean pulse = (frame / 10) % 2 == 0;

        // свечение
        g.setColor(new Color(220, 80, 220, pulse ? 50 : 30));
        g.fillRect(x - 4, y - 4, WIDTH + 8, HEIGHT + 8);

        // корпус — широкий диск
        g.setColor(new Color(170, 60, 190));
        Polygon body = new Polygon();
        body.addPoint(x, y + 14);
        body.addPoint(x + WIDTH, y + 14);
        body.addPoint(x + WIDTH - 14, y + HEIGHT);
        body.addPoint(x + 14, y + HEIGHT);
        g.fillPolygon(body);

        // «корона» сверху
        g.setColor(new Color(220, 100, 230));
        for (int i = 0; i < 5; i++) {
            int sx = x + 10 + i * 18;
            g.fillRect(sx, y + 4, 10, 12);
        }

        // глаза
        g.setColor(Color.YELLOW);
        g.fillRect(x + 22, y + 18, 16, 14);
        g.fillRect(x + WIDTH - 38, y + 18, 16, 14);
        g.setColor(Color.BLACK);
        g.fillRect(x + 26, y + 22, 7, 7);
        g.fillRect(x + WIDTH - 34, y + 22, 7, 7);

        // полоса HP
        g.setColor(new Color(80, 20, 40));
        g.fillRect(x, y - 8, WIDTH, 5);
        g.setColor(pulse ? new Color(255, 80, 80) : new Color(255, 140, 60));
        g.fillRect(x, y - 8, (int) ((WIDTH * hp) / (double) MAX_HP), 5);
    }
}
