package invaders;

import java.awt.*;

/** Один пришалец в сетке. Два кадра анимации. */
public class Invader {
    public static final int WIDTH = 26;
    public static final int HEIGHT = 20;

    int x, y;
    final int points;
    final Color color;
    boolean alive = true;

    public Invader(int x, int y, int row) {
        this.x = x;
        this.y = y;
        // верхние ряды — ценнее (как в оригинале)
        switch (row) {
            case 0:  points = 30; color = new Color(255, 90, 110);  break;
            case 1:  points = 20; color = new Color(255, 180, 80); break;
            default: points = 10; color = new Color(130, 255, 140); break;
        }
    }

    void move(int dx, int dy) { x += dx; y += dy; }

    boolean hitTest(int px, int py) {
        return alive && px >= x && px <= x + WIDTH && py >= y && py <= y + HEIGHT;
    }

    void draw(Graphics2D g, long frame) {
        if (!alive) return;
        boolean step = (frame / 15) % 2 == 0;

        // свечение
        g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 40));
        g.fillRect(x - 2, y - 2, WIDTH + 4, HEIGHT + 4);

        g.setColor(color);
        // тело
        g.fillRect(x + 4, y + 4, WIDTH - 8, HEIGHT - 10);
        // «головная» часть
        g.fillRect(x + 8, y, WIDTH - 16, 6);
        // глаза
        g.setColor(Color.BLACK);
        g.fillRect(x + 8, y + 7, 4, 5);
        g.fillRect(x + WIDTH - 12, y + 7, 4, 5);
        // бегущие «ноги» — два кадра
        if (step) {
            g.setColor(color);
            g.fillRect(x + 4, y + HEIGHT - 6, 5, 6);
            g.fillRect(x + WIDTH - 9, y + HEIGHT - 6, 5, 6);
            g.fillRect(x + 1, y + HEIGHT - 3, 4, 3);
            g.fillRect(x + WIDTH - 5, y + HEIGHT - 3, 4, 3);
        } else {
            g.setColor(color);
            g.fillRect(x + 6, y + HEIGHT - 6, 5, 6);
            g.fillRect(x + WIDTH - 11, y + HEIGHT - 6, 5, 6);
            g.fillRect(x + 3, y + HEIGHT - 3, 4, 3);
            g.fillRect(x + WIDTH - 7, y + HEIGHT - 3, 4, 3);
        }
    }
}
