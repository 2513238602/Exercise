// 定义 ConsList（不可变链表）
abstract class ConsList<T> {
    abstract boolean isEmpty();
    abstract T head();
    abstract ConsList<T> tail();

    // 工厂方法：创建空表
    static <T> ConsList<T> empty() {
        return new Nil<>();
    }

    // 工厂方法：构造新节点
    static <T> ConsList<T> cons(T head, ConsList<T> tail) {
        return new Cons<>(head, tail);
    }
}

// 空表
class Nil<T> extends ConsList<T> {
    boolean isEmpty() { return true; }
    T head() { throw new RuntimeException("Empty list has no head"); }
    ConsList<T> tail() { throw new RuntimeException("Empty list has no tail"); }
}

// 非空表
class Cons<T> extends ConsList<T> {
    private final T head;
    private final ConsList<T> tail;

    Cons(T head, ConsList<T> tail) {
        this.head = head;
        this.tail = tail;
    }

    boolean isEmpty() { return false; }
    T head() { return head; }
    ConsList<T> tail() { return tail; }
}

// Student 类
public class Student {
    String name;
    int id;
    ConsList<Double> grades;

    // 构造器
    Student(String name, int id) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty.");
        }
        if (id < 0) {
            throw new IllegalArgumentException("ID must be non-negative.");
        }
        this.name = name;
        this.id = id;
        this.grades = ConsList.empty(); // 初始为空表
    }

    // 添加成绩
    void addGrade(double grade) {
        if (grade < 0) {
            throw new IllegalArgumentException("Grade must be non-negative.");
        }
        this.grades = ConsList.cons(grade, this.grades);
    }

    // 计算平均分（迭代实现）
    double computeAverageGrade() {
        double sum = 0.0;
        int count = 0;
        ConsList<Double> current = this.grades;
        while (!current.isEmpty()) {
            sum += current.head();
            count++;
            current = current.tail();
        }
        return sum / count;
    }

    // main 方法
    public static void main(String[] args) {
        Student s = new Student("Alice", 1001);
        s.addGrade(85.0);
        s.addGrade(92.5);
        s.addGrade(78.0);

        System.out.println("Student name: " + s.name);
        System.out.println("Student id: " + s.id);
        System.out.println("Average grade: " + s.computeAverageGrade());
    }
}
