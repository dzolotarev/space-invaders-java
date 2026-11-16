package invaders;

import java.awt.*;

/**
 * Бонус, падающий сверху после уничтожения пришельца.
 * Типы: HEART (жизнь), RAPID (скорострельность), TWIN (двойной пулемёт),
 *       ROCKET (ракеты), BOMB (граната), NUCLEAR (ядерное оружие).
 */
public class PowerUp {
    public enum Type { HEART, RAPID, TWIN, ROCKET, BOMB, NUCLEAR }

    public static final int SIZE = 18;
    private static final int SPEED = 2;

    final Type type;
    int x, y;
    boolean active = true;
    private long age = 0;

    public PowerUp(Type type, int x, int y) {
        this.type = type;
        this.x = x;
        this.y = y;
    }

    void update(long dt) {
        if (!active) return;
        age++;
        y += SPEED;
        if (y > Game.HEIGHT) active = false;
    }

    void deactivate() { active = false; }

    /** Попадание кораблём (по центру — промах, края — сбор). */
    boolean collectedBy(int px, int py, int pw, int ph) {
        if (!active) return false;
        // зона сбора — корпус корабля
        if (px + pw/2 >= x && px + pw/2 <= x + SIZE && py + ph >= y && py + ph <= y + SIZE) {
            active = false;
            return true;
        }
        return false;
    }

    Color color() {
        switch (type) {
            case HEART:   return new Color(255, 80, 120);
            case RAPID:   return new Color(255, 220, 60);
            case TWIN:    return new Color(120, 180, 255);
            case ROCKET:  return new Color(255, 140, 60);
            case BOMB:    return new Color(160, 255, 120);
            case NUCLEAR: return new Color(255, 60, 60);
            default:      return Color.WHITE;
        }
    }

    char symbol() {
        switch (type) {
            case HEART:   return '♥';
            case RAPID:   return '⚡';
            case TWIN:    return '≡';
            case ROCKET:  return '🚀';
            case BOMB:    return '💣';
            case NUCLEAR: return '☢';
            default:      return '?';
        }
    }

    void draw(Graphics2D g, long frame) {
        if (!active) return;
        boolean pulse = (frame / 8) % 2 == 0;

        // свечение
        g.setColor(new Color(color().getRed(), color().getGreen(), color().getBlue(), pulse ? 90 : 50));
        g.fillOval(x - 4, y - 4, SIZE + 8, SIZE + 8);

        // корпус — ромб
        g.setColor(color());
        Polygon diamond = new Polygon();
        diamond.addPoint(x + SIZE / 2, y);
        diamond.addPoint(x + SIZE, y + SIZE / 2);
        diamond.addPoint(x + SIZE / 2, y + SIZE);
        diamond.addPoint(x, y + SIZE / 2);
        g.fillPolygon(diamond);

        // символ
        g.setColor(Color.BLACK);
        g.setFont(new Font("SansSerif", Font.BOLD, 13));
        String s = String.valueOf(symbol());
        int sw = g.getFontMetrics().stringWidth(s);
        g.drawString(s, x + SIZE / 2 - sw / 2, y + SIZE / 2 + 5);
    }
}
