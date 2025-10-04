# Exercise — Java & Dafny Practice / Java 与 Dafny 练习项目

A personal practice repo that collects small exercises in **Java** (OOP, collections, testing) and **Dafny** (foundations).  
这是一个个人练习仓库，包含 **Java**（面向对象、集合与测试）与 **Dafny**（基础练习）的小项目集合。

---

## 📁 Repository Layout / 仓库结构

Exercise/
├─ JAVA/
│ └─ Exercise/
│ ├─ 7A/
│ │ └─ src/
│ │ ├─ HelloWorld.java
│ │ ├─ HelloSomeone.java
│ │ ├─ Car.java
│ │ ├─ Rectangle1.java
│ │ ├─ Shapes.java
│ │ └─ Student.java
│ ├─ 8A/
│ │ └─ src/
│ │ ├─ Shape.java, Rectangle.java, Circle.java, RightTriangle.java
│ │ ├─ Box.java, BlackBox.java, GreyBox.java, WhiteBox.java, ColouredBox.java
│ │ ├─ Bucket.java, StackableBucket.java
│ │ ├─ Countable.java, Counter.java, ResettableCounter.java
│ │ └─ Medians.java
│ └─ 8B/
│ ├─ src/ ConsListList.java
│ └─ test/ ConsListListSubListTest.java
└─ Foundation/
└─ Assignment/
└─ Mini_assignment6.dfy


> 文件名/结构可能会随后续提交变化；以上为当前快照。

---

## ✨ Highlights / 亮点

- **Java 7A：入门与类设计**  
  “Hello” 系列，及 `Car`/`Rectangle`/`Student` 等基础类，练习字段、构造器、方法与基本不变式。  
  Basics like hello programs and simple classes (`Car`, `Rectangle`, `Student`) to practice fields, constructors, methods, and invariants.

- **Java 8A：接口与建模**  
  `Shape` 层次与几何计算；`Box` 字符画边框；`Bucket/StackableBucket` 容量约束；`Medians` 实现若干整数的中位数。  
  Shape hierarchy & geometry; printable box frames; capacity-constrained buckets; median-of-N utilities.

- **Java 8B：集合与视图**  
  `ConsListList<T>` 自定义 `List<T>`，并提供 **backed `subList` 视图**（与父表共享存储），包含边界校验与并发修改检查；配套 **JUnit 5** 测试覆盖清空/插入/设置/删除/越界/并发修改等行为。  
  A custom `List<T>` with a backed `subList` view, bounds validation and concurrent-mod checks; JUnit 5 tests cover clear/insert/set/remove/out-of-range/concurrent-modification.

- **Dafny：验证式编程练习**  
  `Mini_assignment6.dfy` 用于 Dafny 规格说明/验证的基础练习。  
  Dafny practice for specification and verification basics.

---

## 🛠 Prerequisites / 先决条件

- **JDK 17+**（建议） / Recommended JDK 17+  
- **JUnit 5 (Jupiter)** 用于测试 / for tests  
- **Dafny 4.x**（可选，仅运行 `.dfy`）/ optional for `.dfy`

> 测试使用 `org.junit.jupiter.api.Test`（JUnit Jupiter）。

---

## 🚀 Quick Start / 快速开始

### Run a Java sample / 运行 Java 示例
```bash
# From repo root / 在仓库根目录
javac JAVA/Exercise/7A/src/HelloWorld.java
java -cp JAVA/Exercise/7A/src HelloWorld
Run the JUnit tests / 运行 JUnit 测试

Option A（IDE 推荐 / Recommended）
用 IntelliJ IDEA / VS Code 导入 JAVA/Exercise，将 8B/test 标记为 Test Sources，添加 JUnit 5 依赖后运行 ConsListListSubListTest。

Option B（命令行 / CLI）
下载 JUnit 5 平台可执行包（如 junit-platform-console-standalone-1.10.0.jar），然后：

# 假设 junit-platform-console-standalone-1.10.0.jar 位于 libs/
javac -cp libs/junit-platform-console-standalone-1.10.0.jar \
  JAVA/Exercise/8B/src/ConsListList.java \
  JAVA/Exercise/8B/test/ConsListListSubListTest.java

java -jar libs/junit-platform-console-standalone-1.10.0.jar \
  -cp "JAVA/Exercise/8B/src:JAVA/Exercise/8B/test" \
  -c ConsListListSubListTest

# 需已安装 Dafny 4.x
dafny verify Foundation/Assignment/Mini_assignment6.dfy
🧭 Coding Notes / 代码说明

ConsListList<T>#subList(from, to) 返回与父表共享存储的视图；非法区间抛 IndexOutOfBoundsException；父表结构性修改将触发 fail-fast ConcurrentModificationException。
Backed subList shares storage with the parent; invalid ranges throw IndexOutOfBoundsException; structural parent changes trigger fail-fast ConcurrentModificationException.

测试覆盖：clear 删除父表区间、subList(from, from).add(x) 在父表插入、set/remove 的同步更新、越界与并发修改检测等。
Tests cover clear/insert via zero-length subList, set/remove propagation, bounds checks, and concurrent-mod detection.

🧩 Roadmap / 后续改进建议

引入 Maven/Gradle 管理依赖与构建，简化测试运行。

配置 GitHub Actions，在提交/PR 时自动运行 JUnit 测试。

添加 LICENSE（如 MIT/Apache-2.0）与更详细的项目简介。

在 README 中加入示例截图/输出，提升可读性。

📄 License

No license yet — consider adding one (MIT/Apache-2.0). / 暂无开源许可证，建议补充（MIT/Apache-2.0）。
::contentReference[oaicite:0]{index=0}
