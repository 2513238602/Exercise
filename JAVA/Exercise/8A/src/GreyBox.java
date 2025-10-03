public class GreyBox implements Box {
    private int width, height;
    private char frame;
    private final char fill; // 不可变

    public GreyBox(int width, int height, char frame, char fill) {
        this.fill = fill;
        setSize(width, height);
        setFrameChar(frame);
    }

    @Override public void print() {
        // top
        for (int c = 0; c < width; c++) System.out.print(frame);
        System.out.println();
        // middle
        for (int r = 0; r < height - 2; r++) {
            System.out.print(frame);
            for (int c = 0; c < width - 2; c++) System.out.print(fill);
            System.out.print(frame);
            System.out.println();
        }
        // bottom
        for (int c = 0; c < width; c++) System.out.print(frame);
        System.out.println();
    }

    @Override public void setSize(int width, int height) {
        check(width, height);
        this.width = width; this.height = height;
    }

    @Override public void setFrameChar(char frame) { this.frame = frame; }

    protected static void check(int w, int h) {
        if (w < 3 || w > 40 || h < 3 || h > 10) throw new IllegalArgumentException("size out of range");
    }
}
