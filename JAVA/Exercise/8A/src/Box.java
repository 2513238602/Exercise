public interface Box {
    void print();

    /** 3 <= width <= 40, 3 <= height <= 10 */
    void setSize(int width, int height);

    void setFrameChar(char frame);
}
