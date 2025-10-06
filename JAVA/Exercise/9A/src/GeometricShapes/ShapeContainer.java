package GeometricShapes;

import java.util.ArrayList;
import java.util.List;

// 泛型容器类ShapeContainer
public class ShapeContainer<T extends Shape> {
    private List<T> shapes;

    public ShapeContainer() {
        shapes = new ArrayList<>();
    }

    public void addShape(T shape) {
        shapes.add(shape);
    }

    public double getTotalArea() {
        double totalArea = 0.0;
        for (T shape : shapes) {
            totalArea += shape.area();
        }
        return totalArea;
    }

    // 可选：获取容器中的形状数量
    public int getShapeCount() {
        return shapes.size();
    }
}
