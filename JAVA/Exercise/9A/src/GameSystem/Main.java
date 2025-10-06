package GameSystem;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // 创建Player和Enemy实例
        Player player = new Player(100, 200, 98);
        Enemy enemy = new Enemy(-50, 38, 5);

        // 调用render方法
        System.out.println("首次调用render:");
        player.render();
        enemy.render();

        // 创建GameEntity列表并添加实例
        List<GameEntity> entities = new ArrayList<>();
        entities.add(player);
        entities.add(enemy);
        // 这些实例可以完美添加到列表中，因为Player和Enemy都是GameEntity的子类，
        // 符合里氏替换原则，可以用父类引用指向子类对象

        // 使用增强型for循环遍历列表并调用render方法
        System.out.println("\n从列表中渲染:");
        for (GameEntity entity : entities) {
            entity.render();
            // 循环变量的静态类型是GameEntity，动态类型是实际的子类（Player或Enemy）
            // 每次迭代调用的是动态类型的render方法实现，这是多态的体现
        }

        // 测试move方法的三个变体
        System.out.println("\n测试移动方法:");

        // 测试Player的move方法
        System.out.println("玩家移动:");
        player.move(150, 250);
        player.render();

        player.move(Direction.NORTH, 50);
        player.render();

        player.move(enemy);
        player.render();

        // 测试Enemy的move方法
        System.out.println("\n敌人移动:");
        enemy.move(-100, 100);
        enemy.render();

        enemy.move(Direction.SOUTHEAST, 30);
        enemy.render();

        enemy.move(player);
        enemy.render();

        // 我们可以通过GameEntity类型的变量调用move方法，
        // 因为move方法是在GameEntity中定义的，子类继承了这些方法
        System.out.println("\n通过GameEntity引用移动:");
        for (GameEntity entity : entities) {
            entity.move(Direction.EAST, 10);
            entity.render();
        }
    }
}
