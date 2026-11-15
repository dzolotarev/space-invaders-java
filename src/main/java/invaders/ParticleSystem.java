package invaders;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/** Система частиц для взрывов. */
public class ParticleSystem {
    private static final int MAX = 300;
    private final List<Particle> particles = new ArrayList<>();

    private static class Particle {
        int x, y, vx, vy, life, maxLife, size;
        Color color;
        Particle(int x, int y, int vx, int vy, Color c, int size) {
            this.x = x; this.y = y; this.vx = vx; this.vy = vy;
            this.color = c; this.size = size;
            this.maxLife = 35; this.life = 35;
        }
    }

    void burst(int cx, int cy, Color color) {
        for (int i = 0; i < 14 && particles.size() < MAX; i++) {
            int angle = i * 26 + (int) (Math.random() * 20);
            double speed = 1.5 + Math.random() * 2.5;
            int vx = (int) (Math.cos(Math.toRadians(angle)) * speed);
            int vy = (int) (Math.sin(Math.toRadians(angle)) * speed);
            particles.add(new Particle(cx, cy, vx, vy, color, 2 + (int) (Math.random() * 3)));
        }
    }

    void bigBurst(int cx, int cy) {
        for (int i = 0; i < 50 && particles.size() < MAX; i++) {
            int angle = i * 7;
            double speed = 1 + Math.random() * 4;
            int vx = (int) (Math.cos(Math.toRadians(angle)) * speed);
            int vy = (int) (Math.sin(Math.toRadians(angle)) * speed);
            Color c = (i % 3 == 0) ? Color.ORANGE : (i % 3 == 1) ? Color.YELLOW : new Color(255, 100, 100);
            particles.add(new Particle(cx, cy, vx, vy, c, 2 + (int) (Math.random() * 4)));
        }
    }

    /** Эффектный взрыв корабля игрока: много частиц, кольца, осколки. */
    void playerExplosion(int cx, int cy) {
        // основное облако
        for (int i = 0; i < 40 && particles.size() < MAX; i++) {
            int angle = i * 9;
            double speed = 1.5 + Math.random() * 4;
            int vx = (int) (Math.cos(Math.toRadians(angle)) * speed);
            int vy = (int) (Math.sin(Math.toRadians(angle)) * speed);
            Color c;
            if (i % 4 == 0)      c = new Color(120, 220, 255); // корпус
            else if (i % 4 == 1) c = Color.WHITE;
            else if (i % 4 == 2) c = new Color(255, 160, 60);  // огонь
            else                 c = new Color(80, 80, 100);   // дым
            particles.add(new Particle(cx, cy, vx, vy, c, 2 + (int) (Math.random() * 4)));
        }
        // «осколки» корпуса — летят медленнее и дальше
        for (int i = 0; i < 12 && particles.size() < MAX; i++) {
            int angle = i * 30 + 15;
            double speed = 0.8 + Math.random() * 1.5;
            int vx = (int) (Math.cos(Math.toRadians(angle)) * speed);
            int vy = (int) (Math.sin(Math.toRadians(angle)) * speed);
            particles.add(new Particle(cx, cy, vx, vy, new Color(100, 200, 240), 3));
        }
        // яркая вспышка в центре
        for (int i = 0; i < 8 && particles.size() < MAX; i++) {
            particles.add(new Particle(cx, cy,
                    (int) (Math.random() * 4 - 2), (int) (Math.random() * 4 - 2),
                    Color.WHITE, 5));
        }
    }

    void update(long dt) {
        for (int i = particles.size() - 1; i >= 0; i--) {
            Particle p = particles.get(i);
            p.x += p.vx;
            p.y += p.vy;
            p.vy += 0.05; // лёгкая гравитация
            if (--p.life <= 0) particles.remove(i);
        }
    }

    void draw(Graphics2D g) {
        for (Particle p : particles) {
            int alpha = (int) (255 * p.life / p.maxLife);
            g.setColor(new Color(p.color.getRed(), p.color.getGreen(), p.color.getBlue(), alpha));
            g.fillRect(p.x, p.y, p.size, p.size);
        }
    }
}
