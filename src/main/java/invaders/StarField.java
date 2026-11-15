package invaders;

import java.awt.*;

/** Звёздное небо с параллаксом — три слоя скорости. */
public class StarField {
    private final int[] xs, ys, sizes;
    private final double[] speeds;
    private final int count;

    public StarField(int width, int height, int count) {
        this.count = count;
        xs = new int[count];
        ys = new int[count];
        sizes = new int[count];
        speeds = new double[count];
        for (int i = 0; i < count; i++) {
            xs[i] = (int) (Math.random() * width);
            ys[i] = (int) (Math.random() * height);
            // 3 слоя: дальние мелкие и медленные, ближние крупные и быстрые
            int layer = i % 3;
            sizes[i] = layer + 1;
            speeds[i] = 0.15 + layer * 0.25;
        }
    }

    void draw(Graphics2D g, long frame) {
        for (int i = 0; i < count; i++) {
            int y = (int) ((ys[i] + frame * speeds[i]) % Game.HEIGHT);
            int brightness = 90 + sizes[i] * 40;
            g.setColor(new Color(brightness, brightness, brightness + 30));
            g.fillRect(xs[i], y, sizes[i], sizes[i]);
        }
    }
}
