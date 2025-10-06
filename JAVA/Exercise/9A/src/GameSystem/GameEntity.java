package GameSystem;

public abstract class GameEntity {
    protected int x;
    protected int y;

    // 构造函数
    public GameEntity(int x, int y) {
        this.x = x;
        this.y = y;
    }

    // 抽象渲染方法
    public abstract void render();

    // 移动到指定坐标
    public void move(int x, int y) {
        this.x = x;
        this.y = y;
    }

    // 沿特定方向移动一定距离
    public void move(Direction direction, int distance) {
        switch (direction) {
            case NORTH:
                y -= distance;
                break;
            case NORTHEAST:
                x += distance;
                y -= distance;
                break;
            case EAST:
                x += distance;
                break;
            case SOUTHEAST:
                x += distance;
                y += distance;
                break;
            case SOUTH:
                y += distance;
                break;
            case SOUTHWEST:
                x -= distance;
                y += distance;
                break;
            case WEST:
                x -= distance;
                break;
            case NORTHWEST:
                x -= distance;
                y -= distance;
                break;
        }
    }

    // 向目标实体移动
    public void move(GameEntity target) {
        // 简单实现：向目标移动1单位
        if (x < target.x) x++;
        else if (x > target.x) x--;

        if (y < target.y) y++;
        else if (y > target.y) y--;
    }
}
