package invaders;

import java.awt.*;

/** Щит (бункер) — разрушаемый блок из пикселей. */
public class Shield {
    private static final int W = 52;
    private static final int H = 34;
    private static final int CELL = 6;
    private final boolean[][] cells;

    public Shield(int x, int y) {
        this.x = x; this.y = y;
        cells = new boolean[H / CELL][W / CELL];
        reset();
    }

    private int x, y;

    void reset() {
        for (int r = 0; r < cells.length; r++) {
            for (int c = 0; c < cells[0].length; c++) {
                // вырезаем арку снизу
                cells[r][c] = !(r > cells.length - 4 && c > 1 && c < cells[0].length - 2);
            }
        }
    }

    /** Возвращает true, если пуля попала и часть щита разрушена. */
    boolean hit(int px, int py) {
        if (px < x || px >= x + W || py < y || py >= y + H) return false;
        int c = (px - x) / CELL;
        int r = (py - y) / CELL;
        if (r < 0 || r >= cells.length || c < 0 || c >= cells[0].length) return false;
        if (cells[r][c]) {
            // разрушаем 2x2 вокруг попадания
            for (int dr = -1; dr <= 1; dr++)
                for (int dc = -1; dc <= 1; dc++) {
                    int rr = r + dr, cc = c + dc;
                    if (rr >= 0 && rr < cells.length && cc >= 0 && cc < cells[0].length)
                        cells[rr][cc] = false;
                }
            return true;
        }
        return false;
    }

    void draw(Graphics2D g) {
        // контур-свечение
        g.setColor(new Color(80, 220, 120, 50));
        g.fillRect(x - 2, y - 2, W + 4, H + 4);
        // пиксели
        g.setColor(new Color(90, 230, 130));
        for (int r = 0; r < cells.length; r++)
            for (int c = 0; c < cells[0].length; c++)
                if (cells[r][c])
                    g.fillRect(x + c * CELL, y + r * CELL, CELL - 1, CELL - 1);
    }
}
