public class ColouredBox implements Box {
    private int width, height;
    private char frame;
    private char fill;

    public ColouredBox(int width, int height, char frame, char fill) {
        setSize(width, height);
        setFrameChar(frame);
        this.fill = fill;
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
        if (width < 3 || width > 40 || height < 3 || height > 10) throw new IllegalArgumentException("size out of range");
        this.width = width; this.height = height;
    }

    @Override public void setFrameChar(char frame) { this.frame = frame; }

    /** 允许运行中修改内部填充字符 */
    public void setFillChar(char fill) { this.fill = fill; }
}
