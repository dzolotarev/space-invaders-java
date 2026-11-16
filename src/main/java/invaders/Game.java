package invaders;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

/**
 * Space Invaders — классическая аркада на Java Swing.
 * Корабль внизу, волны пришельцев сверху, босс в конце волн.
 * Система бонусов: сердечко, скорострельность, двойной пулемёт,
 * ракеты, гранаты, ядерное оружие.
 */
public class Game extends JPanel implements Runnable {

    static final int WIDTH = 640;
    static final int HEIGHT = 480;

    private final Player player;
    private final Bullet[] bullets = new Bullet[5];
    private final Rocket[] rockets = new Rocket[3];
    private final Grenade[] grenades = new Grenade[2];
    private final List<PowerUp> powerUps = new ArrayList<>();
    private final InvaderGrid grid;
    private final Boss boss;
    private final Shield[] shields = new Shield[4];
    private final ParticleSystem particles = new ParticleSystem();
    private final StarField stars = new StarField(WIDTH, HEIGHT, 90);

    private int score = 0;
    private int lives = 3;
    private int wave = 1;
    private boolean gameOver = false;
    private boolean paused = false;
    private long lastFrame = 0;
    private long frameCount = 0;

    // --- бонусы (таймеры в кадрах) ---
    private long rapidTimer = 0;   // скорострельность
    private long twinTimer = 0;    // двойной пулемёт
    private int rocketAmmo = 0;    // ракеты
    private int grenadeAmmo = 0;   // гранаты
    private int nuclearAmmo = 0;   // ядерное оружие
    private long fireCooldown = 0; // кулдаун выстрела

    // --- эффекты ---
    private long screenShake = 0;
    private int shakeIntensity = 0;
    private long flashAlpha = 0;   // белая вспышка (ядерный взрыв)

    public Game() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);

        player = new Player(WIDTH / 2 - Player.WIDTH / 2, HEIGHT - 44);
        grid = new InvaderGrid(0, 70);
        boss = new Boss(WIDTH / 2 - Boss.WIDTH / 2, 34);
        for (int i = 0; i < shields.length; i++) {
            shields[i] = new Shield(80 + i * 150, HEIGHT - 118);
        }
        addKeyListener(new InputHandler());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // тряска экрана
        if (screenShake > 0) {
            int dx = (int) ((Math.random() - 0.5) * shakeIntensity);
            int dy = (int) ((Math.random() - 0.5) * shakeIntensity);
            g2.translate(dx, dy);
            screenShake--;
        }

        drawBackground(g2);
        stars.draw(g2, frameCount);
        particles.draw(g2);
        for (Shield s : shields) s.draw(g2);
        player.draw(g2);
        for (Bullet b : bullets) if (b != null && b.active) b.draw(g2);
        for (Rocket r : rockets) if (r != null && r.active) r.draw(g2);
        for (Grenade gr : grenades) if (gr != null && gr.active) gr.draw(g2);
        for (PowerUp p : powerUps) if (p.active) p.draw(g2, frameCount);
        grid.draw(g2, frameCount);
        if (boss.visible) boss.draw(g2, frameCount);
        drawHUD(g2);

        // вспышка (ядерный взрыв)
        if (flashAlpha > 0) {
            g2.setColor(new Color(255, 255, 240, (int) flashAlpha));
            g2.fillRect(-20, -20, WIDTH + 40, HEIGHT + 40);
            flashAlpha -= 12;
        }

        if (gameOver) {
            drawCenteredMessage(g2, "GAME OVER", Color.RED, 48, HEIGHT / 2 - 10);
            drawCenteredMessage(g2, "Нажмите ENTER для перезапуска", new Color(200, 200, 220), 18, HEIGHT / 2 + 34);
        }
        if (paused) {
            drawCenteredMessage(g2, "PAUSE", Color.YELLOW, 32, HEIGHT / 2);
        }
    }

    private void drawBackground(Graphics2D g2) {
        GradientPaint gp = new GradientPaint(0, 0, new Color(6, 8, 24), 0, HEIGHT, new Color(16, 12, 40));
        g2.setPaint(gp);
        g2.fillRect(0, 0, WIDTH, HEIGHT);
        g2.setPaint(Color.BLACK);
    }

    private void drawCenteredMessage(Graphics2D g2, String text, Color color, int size, int y) {
        g2.setColor(color);
        g2.setFont(new Font("Monospaced", Font.BOLD, size));
        int w = g2.getFontMetrics().stringWidth(text);
        g2.drawString(text, (WIDTH - w) / 2, y);
    }

    private void drawHUD(Graphics2D g2) {
        g2.setFont(new Font("Monospaced", Font.BOLD, 16));
        g2.setColor(new Color(120, 220, 255));
        g2.drawString("SCORE " + score, 12, 24);
        g2.setColor(new Color(255, 210, 120));
        g2.drawString("WAVE " + wave, WIDTH / 2 - 34, 24);
        for (int i = 0; i < lives; i++) {
            g2.setColor(new Color(120, 220, 255));
            int lx = WIDTH - 24 - i * 22;
            g2.fillRect(lx, 14, 14, 4);
            g2.fillRect(lx + 4, 10, 6, 4);
        }
        // индикаторы бонусов
        int by = 44;
        g2.setFont(new Font("Monospaced", Font.PLAIN, 13));
        if (rapidTimer > 0) {
            g2.setColor(new Color(255, 220, 60));
            g2.drawString("RAPID " + (rapidTimer / 60) + "s", 12, by);
            by += 16;
        }
        if (twinTimer > 0) {
            g2.setColor(new Color(120, 180, 255));
            g2.drawString("TWIN  " + (twinTimer / 60) + "s", 12, by);
            by += 16;
        }
        if (rocketAmmo > 0) {
            g2.setColor(new Color(255, 140, 60));
            g2.drawString("ROCKET x" + rocketAmmo + " [R]", 12, by);
            by += 16;
        }
        if (grenadeAmmo > 0) {
            g2.setColor(new Color(160, 255, 120));
            g2.drawString("BOMB  x" + grenadeAmmo + " [G]", 12, by);
            by += 16;
        }
        if (nuclearAmmo > 0) {
            g2.setColor(new Color(255, 60, 60));
            g2.drawString("NUKE  x" + nuclearAmmo + " [N]", 12, by);
        }
    }

    private void update(long dt) {
        if (paused || gameOver) return;
        frameCount++;

        // таймеры бонусов
        if (rapidTimer > 0) rapidTimer--;
        if (twinTimer > 0) twinTimer--;
        if (fireCooldown > 0) fireCooldown--;

        player.update(dt);
        for (Bullet b : bullets) if (b != null) b.update(dt);
        for (Rocket r : rockets) if (r != null) r.update(dt);
        for (Grenade gr : grenades) if (gr != null) gr.update(dt);
        for (PowerUp p : powerUps) p.update(dt);
        powerUps.removeIf(p -> !p.active);
        grid.update(dt);
        boss.update(dt);

        // --- пули игрока ---
        for (Bullet b : bullets) {
            if (b == null || !b.active) continue;
            Invader hit = grid.hitTest(b.x, b.y);
            if (hit != null) {
                b.deactivate();
                onInvaderKilled(hit);
                continue;
            }
            if (boss.visible && boss.hitTest(b.x, b.y)) {
                b.deactivate();
                boss.damage();
                particles.burst(b.x, b.y, new Color(255, 140, 60));
                if (boss.dead()) onBossKilled();
            }
        }

        // --- ракеты ---
        for (Rocket r : rockets) {
            if (r == null || !r.active) continue;
            Invader hit = grid.hitTest(r.x + 3, r.y + 7);
            if (hit != null) {
                r.deactivate();
                explodeArea(r.x + 3, r.y + 7, Rocket.BLAST_RADIUS, new Color(255, 140, 60));
                continue;
            }
            if (boss.visible && boss.hitTest(r.x + 3, r.y + 7)) {
                r.deactivate();
                explodeArea(r.x + 3, r.y + 7, Rocket.BLAST_RADIUS, new Color(255, 140, 60));
                boss.damage();
                if (boss.dead()) onBossKilled();
            }
        }

        // --- гранаты ---
        for (Grenade gr : grenades) {
            if (gr == null || !gr.active) continue;
            Invader hit = grid.hitTest(gr.x + 4, gr.y + 5);
            if (hit != null) {
                gr.deactivate();
                explodeArea(gr.x + 4, gr.y + 5, Grenade.BLAST_RADIUS, new Color(160, 255, 120));
                continue;
            }
            if (boss.visible && boss.hitTest(gr.x + 4, gr.y + 5)) {
                gr.deactivate();
                explodeArea(gr.x + 4, gr.y + 5, Grenade.BLAST_RADIUS, new Color(160, 255, 120));
                boss.damage();
                if (boss.dead()) onBossKilled();
            }
        }

        // --- сбор бонусов ---
        for (PowerUp p : powerUps) {
            if (p.active && p.collectedBy(player.x, player.y, Player.WIDTH, Player.HEIGHT)) {
                applyPowerUp(p.type);
                particles.burst(p.x + PowerUp.SIZE / 2, p.y + PowerUp.SIZE / 2, p.color());
            }
        }

        // --- пули пришельцев ---
        for (EnemyShot es : grid.enemyShots()) {
            es.update(dt);
            if (!es.active) continue;
            for (Shield s : shields) {
                if (s.hit(es.x, es.y)) { es.deactivate(); break; }
            }
            if (es.active && player.hit(es.x, es.y)) {
                es.deactivate();
                loseLife();
            }
        }

        if (grid.reachedBottom()) loseLife();
        particles.update(dt);
    }

    /** Убийство пришельца: очки, частицы, шанс дропа бонуса. */
    private void onInvaderKilled(Invader inv) {
        score += inv.points;
        particles.burst(inv.x + Invader.WIDTH / 2, inv.y + Invader.HEIGHT / 2, inv.color);
        grid.remove(inv);
        if (!boss.visible && grid.aliveCount() <= 8) boss.setVisible(true);
        maybeDropPowerUp(inv.x + Invader.WIDTH / 2, inv.y + Invader.HEIGHT);
    }

    private void onBossKilled() {
        score += 1000;
        particles.bigBurst(boss.x + Boss.WIDTH / 2, boss.y + Boss.HEIGHT / 2);
        triggerScreenShake(20, 12);
        nextWave();
    }

    /** Взрыв по площади: убивает всех пришельцев в радиусе. */
    private void explodeArea(int cx, int cy, int radius, Color color) {
        particles.burst(cx, cy, color);
        particles.burst(cx, cy, new Color(255, 220, 120));
        triggerScreenShake(8, 5);
        for (Invader inv : grid.allAlive()) {
            int ix = inv.x + Invader.WIDTH / 2;
            int iy = inv.y + Invader.HEIGHT / 2;
            if (dist(cx, cy, ix, iy) <= radius) {
                score += inv.points;
                particles.burst(ix, iy, inv.color);
                grid.remove(inv);
            }
        }
        if (!boss.visible && grid.aliveCount() <= 8) boss.setVisible(true);
    }

    private double dist(int x1, int y1, int x2, int y2) {
        double dx = x1 - x2, dy = y1 - y2;
        return Math.sqrt(dx * dx + dy * dy);
    }

    private void maybeDropPowerUp(int x, int y) {
        // 12% шанс дропа
        if (Math.random() > 0.12) return;
        PowerUp.Type type;
        double r = Math.random();
        if (r < 0.25)      type = PowerUp.Type.HEART;
        else if (r < 0.45) type = PowerUp.Type.RAPID;
        else if (r < 0.65) type = PowerUp.Type.TWIN;
        else if (r < 0.80) type = PowerUp.Type.ROCKET;
        else if (r < 0.92) type = PowerUp.Type.BOMB;
        else               type = PowerUp.Type.NUCLEAR;
        powerUps.add(new PowerUp(type, x - PowerUp.SIZE / 2, y));
    }

    private void applyPowerUp(PowerUp.Type type) {
        switch (type) {
            case HEART:
                if (lives < 5) lives++;
                break;
            case RAPID:
                rapidTimer = 10 * 60; // 10 секунд
                break;
            case TWIN:
                twinTimer = 12 * 60;  // 12 секунд
                break;
            case ROCKET:
                rocketAmmo += 3;
                break;
            case BOMB:
                grenadeAmmo += 2;
                break;
            case NUCLEAR:
                nuclearAmmo += 1;
                break;
        }
    }

    // --- стрельба ---

    private void fire() {
        if (fireCooldown > 0) return;
        fireCooldown = (rapidTimer > 0) ? 6 : 18;

        int cx = player.x + Player.WIDTH / 2;
        if (twinTimer > 0) {
            spawnBullet(cx - 8, player.y - 10);
            spawnBullet(cx + 8, player.y - 10);
        } else {
            spawnBullet(cx - 2, player.y - 10);
        }
    }

    private void spawnBullet(int x, int y) {
        for (Bullet b : bullets) {
            if (b == null) { bullets[bulletsCount()] = new Bullet(x, y); return; }
            if (!b.active) { b.reset(x, y); return; }
        }
    }

    private int bulletsCount() {
        for (int i = 0; i < bullets.length; i++) if (bullets[i] == null) return i;
        return 0;
    }

    private void fireRocket() {
        if (rocketAmmo <= 0) return;
        rocketAmmo--;
        int cx = player.x + Player.WIDTH / 2;
        for (Rocket r : rockets) {
            if (r == null) { rockets[rocketCount()] = new Rocket(cx - 3, player.y - 16); return; }
            if (!r.active) { r = new Rocket(cx - 3, player.y - 16); return; }
        }
    }

    private int rocketCount() {
        for (int i = 0; i < rockets.length; i++) if (rockets[i] == null) return i;
        return 0;
    }

    private void fireGrenade() {
        if (grenadeAmmo <= 0) return;
        grenadeAmmo--;
        int cx = player.x + Player.WIDTH / 2;
        for (Grenade gr : grenades) {
            if (gr == null) { grenades[grenadeCount()] = new Grenade(cx - 4, player.y - 14); return; }
            if (!gr.active) { gr = new Grenade(cx - 4, player.y - 14); return; }
        }
    }

    private int grenadeCount() {
        for (int i = 0; i < grenades.length; i++) if (grenades[i] == null) return i;
        return 0;
    }

    /** Ядерный удар: уничтожает ВСЕХ пришельцев и босса. */
    private void fireNuclear() {
        if (nuclearAmmo <= 0) return;
        nuclearAmmo--;
        flashAlpha = 200;
        triggerScreenShake(40, 16);
        // уничтожаем всех
        for (Invader inv : grid.allAlive()) {
            score += inv.points;
            particles.burst(inv.x + Invader.WIDTH / 2, inv.y + Invader.HEIGHT / 2, inv.color);
            grid.remove(inv);
        }
        if (boss.visible) {
            score += 1000;
            particles.bigBurst(boss.x + Boss.WIDTH / 2, boss.y + Boss.HEIGHT / 2);
            boss.setVisible(false);
        }
        nextWave();
    }

    // --- урон игроку ---

    private void loseLife() {
        lives--;
        // эффектный взрыв корабля
        int cx = player.x + Player.WIDTH / 2;
        int cy = player.y + Player.HEIGHT / 2;
        particles.playerExplosion(cx, cy);
        triggerScreenShake(25, 10);
        if (lives <= 0) {
            gameOver = true;
        } else {
            player.reset(WIDTH / 2 - Player.WIDTH / 2, HEIGHT - 44);
        }
    }

    private void triggerScreenShake(long frames, int intensity) {
        screenShake = frames;
        shakeIntensity = intensity;
    }

    private void nextWave() {
        wave++;
        boss.setVisible(false);
        grid.reset(wave);
    }

    @Override
    public void run() {
        while (true) {
            long now = System.nanoTime();
            long dt = Math.min((now - lastFrame) / 1_000_000L, 50);
            lastFrame = now;
            update(dt);
            repaint();
            try { Thread.sleep(1000 / 60); } catch (InterruptedException ignored) {}
        }
    }

    private class InputHandler extends KeyAdapter {
        @Override public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT:  player.moveLeft(); break;
                case KeyEvent.VK_RIGHT: player.moveRight(); break;
                case KeyEvent.VK_SPACE:  fire(); break;
                case KeyEvent.VK_R:     fireRocket(); break;
                case KeyEvent.VK_G:     fireGrenade(); break;
                case KeyEvent.VK_N:     fireNuclear(); break;
                case KeyEvent.VK_P:     paused = !paused; break;
                case KeyEvent.VK_ENTER:
                    if (gameOver) restart();
                    break;
            }
        }
        @Override public void keyReleased(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT:  player.stopMoving(); break;
                case KeyEvent.VK_RIGHT: player.stopMoving(); break;
            }
        }
    }

    private void restart() {
        score = 0; lives = 3; wave = 1;
        gameOver = false; paused = false;
        rapidTimer = 0; twinTimer = 0;
        rocketAmmo = 0; grenadeAmmo = 0; nuclearAmmo = 0;
        fireCooldown = 0;
        player.reset(WIDTH / 2 - Player.WIDTH / 2, HEIGHT - 44);
        grid.reset(1);
        boss.setVisible(false);
        powerUps.clear();
        for (int i = 0; i < shields.length; i++) shields[i].reset();
        for (int i = 0; i < bullets.length; i++) if (bullets[i] != null) bullets[i].deactivate();
        for (int i = 0; i < rockets.length; i++) if (rockets[i] != null) rockets[i].deactivate();
        for (int i = 0; i < grenades.length; i++) if (grenades[i] != null) grenades[i].deactivate();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Space Invaders");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            Game game = new Game();
            frame.add(game);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            new Thread(game).start();
        });
    }
}
