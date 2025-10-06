package GameSystem;

public class Enemy extends GameEntity {
    private int threatLevel;

    public Enemy(int x, int y, int threatLevel) {
        super(x, y);
        // 确保威胁等级在0-100之间
        this.threatLevel = Math.max(0, Math.min(100, threatLevel));
    }

    @Override
    public void render() {
        System.out.printf("Rendering GameSystem.Enemy at (%d,%d) with threat level: %d%n", x, y, threatLevel);
    }

    public void setThreatLevel(int threatLevel) {
        this.threatLevel = Math.max(0, Math.min(100, threatLevel));
    }

    public int getThreatLevel() {
        return threatLevel;
    }
}
