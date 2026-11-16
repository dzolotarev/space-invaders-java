package invaders;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Сетка пришельцев: движется влево-вправо, опускается при развороте.
 * Периодически стреляет по игроку.
 */
public class InvaderGrid {
    private static final int COLS = 8;
    private static final int ROWS = 5;
    private static final int GAP_X = 42;
    private static final int GAP_Y = 34;

    private final List<Invader> invaders = new ArrayList<>();
    private final List<EnemyShot> shots = new ArrayList<>();

    private int dirX = 1;
    private int speed = 2;      // пикселей за кадр
    private long shootTimer = 0;
    private int wave = 1;

    public InvaderGrid(int startX, int startY) { reset(1); }

    void reset(int wave) {
        this.wave = wave;
        invaders.clear();
        shots.clear();
        dirX = 1;
        speed = Math.min(2 + (wave - 1), 6);
        int sx = (Game.WIDTH - COLS * GAP_X) / 2;
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                invaders.add(new Invader(sx + c * GAP_X, 70 + r * GAP_Y, r));
            }
        }
    }

    void update(long dt) {
        if (invaders.isEmpty()) return;

        // движение
        int dx = dirX * speed;
        boolean hitEdge = false;
        for (Invader inv : invaders) {
            if (!inv.alive) continue;
            if (inv.x + dx < 10 || inv.x + dx + Invader.WIDTH > Game.WIDTH - 10) {
                hitEdge = true; break;
            }
        }
        if (hitEdge) {
            dirX *= -1;
            for (Invader inv : invaders) if (inv.alive) inv.move(0, 12);
        } else {
            for (Invader inv : invaders) if (inv.alive) inv.move(dx, 0);
        }

        // стрельба: чем выше волна, тем чаще
        shootTimer += dt;
        long interval = Math.max(1200 - wave * 100, 400);
        if (shootTimer >= interval) {
            shootTimer = 0;
            Invader shooter = randomAlive();
            if (shooter != null) {
                shots.add(new EnemyShot(shooter.x + Invader.WIDTH / 2, shooter.y + Invader.HEIGHT));
            }
        }
    }

    private Invader randomAlive() {
        List<Invader> alive = new ArrayList<>();
        for (Invader i : invaders) if (i.alive) alive.add(i);
        if (alive.isEmpty()) return null;
        return alive.get((int) (Math.random() * alive.size()));
    }

    Invader hitTest(int px, int py) {
        for (Invader inv : invaders) if (inv.hitTest(px, py)) return inv;
        return null;
    }

    void remove(Invader inv) { inv.alive = false; }

    int aliveCount() {
        int n = 0;
        for (Invader i : invaders) if (i.alive) n++;
        return n;
    }

    List<Invader> allAlive() {
        List<Invader> result = new ArrayList<>();
        for (Invader i : invaders) if (i.alive) result.add(i);
        return result;
    }

    boolean reachedBottom() {
        for (Invader inv : invaders)
            if (inv.alive && inv.y + Invader.HEIGHT >= Game.HEIGHT - 70) return true;
        return false;
    }

    List<EnemyShot> enemyShots() { return shots; }

    void draw(Graphics2D g, long frame) {
        for (Invader inv : invaders) inv.draw(g, frame);
        for (EnemyShot s : shots) if (s.active) s.draw(g);
    }
}
