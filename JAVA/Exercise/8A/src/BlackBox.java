public class BlackBox implements Box {
    private int width, height;
    private char frame;

    public BlackBox(int width, int height, char frame) {
        setSize(width, height);
        setFrameChar(frame);
    }

    @Override public void print() {
        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) System.out.print(frame);
            System.out.println();
        }
    }

    @Override public void setSize(int width, int height) {
        check(width, height);
        this.width = width; this.height = height;
    }

    @Override public void setFrameChar(char frame) { this.frame = frame; }

    private static void check(int w, int h) {
        if (w < 3 || w > 40 || h < 3 || h > 10) throw new IllegalArgumentException("size out of range");
    }
}
