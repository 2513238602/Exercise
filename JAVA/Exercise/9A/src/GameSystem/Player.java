package GameSystem;

public class Player extends GameEntity {
    private int health;

    public Player(int x, int y, int health) {
        super(x, y);
        // 确保生命值在0-100之间
        this.health = Math.max(0, Math.min(100, health));
    }

    @Override
    public void render() {
        System.out.printf("Rendering GameSystem.Player at (%d,%d) with health: %d%n", x, y, health);
    }

    public void setHealth(int health) {
        this.health = Math.max(0, Math.min(100, health));
    }

    public int getHealth() {
        return health;
    }
}
