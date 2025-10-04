# 📚 COMP1110/1140/6710 Core Knowledge Q&A | 核心知识点问答

> Based on workshop8a-slides.pdf & workshop8b-slides.pdf  
> Australian National University Course Materials · Suitable for project reference and review  
> 
> 基于 workshop8a-slides.pdf & workshop8b-slides.pdf  
> 澳大利亚国立大学课程资料整理 · 适用于项目学习参考与复习

---

## 🎯 一、Abstract Data Types (ADT) Core Concepts | 抽象数据类型核心概念

### 🔍 Q1: Three Components of Stack ADT | Stack ADT 的三大组成

**English**: What are the "possible values", "set of operations", and "operation semantics" of the Stack ADT? Why doesn't the implementation affect its nature?  
**中文**: Stack ADT的"可能值""操作集合""操作语义"分别是什么？为什么实现不影响本质？

#### 💡 Answer | 答案

- **Possible Values | 可能值**: Finite ordered sequence following LIFO rule | 有限的、遵循"后进先出"规则的有序元素序列

- **Set of Operations | 操作集合**:
```java
push(E e)    // Add to top | 向栈顶添加元素
pop()        // Remove and return top | 移除并返回栈顶元素
peek()       // Return top without removal | 返回栈顶元素但不移除
isEmpty()    // Check if empty | 判断栈是否为空
```

- **Implementation Independence | 实现不影响本质**: ADT defines behavior, not implementation details | ADT定义行为而非实现细节

---

### 🔍 Q2: List ADT vs Implementations | List ADT 与具体实现

**English**: Relationship between List ADT and array/linked list implementations? Why "ADT is blueprint"?  
**中文**: List ADT和数组/链表实现是什么关系？为什么说"ADT是设计蓝图"？

#### 💡 Answer | 答案

**Relationship | 关系**:
- List ADT = Abstract model (design blueprint) | 抽象模型（设计蓝图）
- Array/Linked List = Concrete implementations (construction plans) | 具体实现（施工方案）

**Reason | 原因**: ADT defines behavior without memory layout | ADT定义行为而不涉及内存布局

---

### 🔍 Q3: ADT Engineering Value | ADT 的工程价值

**English**: If Team A changes List implementation from linked list to array, does Team B's code need changes? Why?  
**中文**: 如果团队A把List实现从链表改为数组，团队B的代码需要修改吗？为什么？

#### 💡 Answer | 答案

- **Need Modification? | 是否需要修改**: ❌ No | 不需要
- **Reason | 原因**: Team B depends on operation semantics, not implementation | 团队B依赖操作语义而非实现
- **Advantage | 优势**: Decouples user code from implementations | 解耦用户代码与底层实现

---

## ⚙️ 二、Java OOP & ADT Implementation | Java 面向对象与 ADT 实现

### 🔍 Q1: List<T> Generics Necessity | List<T> 泛型必要性

**English**: Why is List<T> designed as generic interface? Problems without generics?  
**中文**: 为什么List<T>要设计成泛型接口？不用泛型的问题？

#### 💡 Answer | 答案

```java
// ✅ With Generics | 泛型方案 - Type safe | 类型安全
List<String> names = new ArrayList<>();
names.add("Alice");
String name = names.get(0);

// ❌ Without Generics | 非泛型方案 - Type unsafe | 类型不安全
List names = new ArrayList();
names.add("Alice");
names.add(123);  // Mixing types | 混合类型
String name = (String) names.get(0);  // Casting needed | 需要强制转换
```

**Necessity | 必要性**: Solve type safety and casting issues | 解决类型安全和强制转换问题

---

### 🔍 Q2: Interface vs Abstract Class | 接口 vs 抽象类

**English**: When to choose interface vs abstract class for Shape?  
**中文**: Shape该选接口还是抽象类？

#### 💡 Answer | 答案

```java
// ✅ Need fields → Abstract Class | 需要字段 → 抽象类
abstract class Shape {
    protected String color = "white";
    abstract double getArea();
}

// ✅ Only behavior → Interface | 仅行为 → 接口
interface Shape {
    double getArea();
}
```

**Selection Criteria | 选择标准**:
- Need fields/default implementation → Abstract Class | 需要字段/默认实现 → 抽象类
- Only behavior contract → Interface | 仅行为契约 → 接口

---

### 🔍 Q3: Liskov Substitution Principle | 里氏替换原则

**English**: How to make PosInt conform to LSP? Does negative int violate LSP?  
**中文**: 如何让PosInt符合LSP？负int是否违反LSP？

#### 💡 Answer | 答案

```java
class PosInt {
    private final int value;
    
    private PosInt(int value) { 
        this.value = value; 
    }
    
    // Factory method with validation | 带校验的工厂方法
    public static PosInt of(int value) {
        if (value <= 0) 
            throw new IllegalArgumentException("Must be positive");
        return new PosInt(value);
    }
}
```

- **Violates LSP? | 违反LSP**: ✅ Yes | 是
- **Reason | 原因**: Negative int breaks "num>0" semantics | 负int违反"num>0"语义

---

## 📊 三、Data Structure Implementation & Performance | 数据结构实现与性能

### 🔍 Q1: Array vs Linked List Insertion | 数组与链表插入性能

**English**: Why O(n) for array head insertion vs O(1) for linked list? What about middle insertion?  
**中文**: 为什么数组头部插入O(n)而链表O(1)？中间插入呢？

#### 💡 Answer | 答案

**Performance Comparison | 性能对比**:

| Operation | Array | Linked List |
|-----------|-------|-------------|
| Head Insert | O(n) - Shift elements | O(1) - Update pointers |
| Middle Insert | O(n) - Shift后半元素 | O(n) - Traverse to position |
| Random Access | O(1) - Direct addressing | O(n) - Sequential traversal |

**Middle Insertion | 中间插入**: Not O(1), but O(n) for linked list | 链表不是O(1)，是O(n)

---

### 🔍 Q2: Doubly Linked List Deletion | 双向链表删除逻辑

**English**: After deleting element 7, how many references point to its node? Problems if only update predecessor's next?  
**中文**: 删除元素7后，原节点有多少引用指向它？只改前驱节点的next有什么问题？

#### 💡 Answer | 答案

- **Reference Count | 引用数量**: 0 references | 0个引用
- **Java Recycling | Java回收**: Garbage collection collects unreferenced objects | 垃圾回收机制回收无引用对象
- **Problems | 问题**:
  1. 🚨 Memory leak (node still referenced) | 内存泄漏（节点仍被引用）
  2. 🚨 Broken list structure | 破坏链表结构

---

### 🔍 Q3: ArrayList vs LinkedList Get Performance | get 性能对比

**English**: Why ArrayList.get() is O(1) but LinkedList.get() is O(n)? Which for frequent get(1000)?  
**中文**: 为什么ArrayList.get()是O(1)而LinkedList.get()是O(n)？频繁调用get(1000)选哪种？

#### 💡 Answer | 答案

```java
// ArrayList - O(1) Random Access | 随机访问
// Memory: [elem0][elem1][elem2]...
// Address: base + index × element size

// LinkedList - O(n) Sequential Access | 顺序访问  
// Memory: head → node1 → ... → nodeN
// Access: Must traverse from head to index
```

**Choice for get(1000) | get(1000)选择**: ✅ ArrayList  
**Reason | 原因**: O(1) vs O(1000) traversals | O(1) 对比 O(1000)次遍历

---

## 🔒 四、Access Control & Encapsulation | 访问控制与封装

### 🔍 Q1: User Class Field Modifiers | User 类字段修饰符

**English**: Why name not public? How can Student subclass access private name?  
**中文**: 为什么name不用public？Student子类如何访问private name？

#### 💡 Answer | 答案

```java
class User {
    protected String name;    // ❌ Not public: avoid direct external modification
    public final String id;   // ✅ final: assign once, immutable
    
    // Access with private name | private name的访问方式
    private String name;
    public String getName() { return name; } // Controlled access
}

class Student extends User {
    void printName() {
        System.out.println(getName()); // Access via method
    }
}
```

**Encapsulation Principle | 封装原则**: Hide unnecessary details, provide controlled access | 隐藏不必要细节，提供受控访问

---

### 🔍 Q2: Inner Class Access & Creation | 内部类访问与创建

**English**: Why can non-static inner class access outer private fields directly?  
**中文**: 为什么非静态内部类能直接访问外部类私有字段？

#### 💡 Answer | 答案

```java
class B {
    private String str;
    
    // Non-static inner class - Associated with B instance
    class C {
        void access() { 
            System.out.println(str); // ✅ Direct access
        }
    }
    
    // Static inner class - Belongs to B class
    static class D {
        void access(B b) {
            System.out.println(b.str); // ✅ Need B instance
        }
    }
}

// Create C instance outside B
B b = new B();
B.C c = b.new C();  // Need B instance first
```

---

### 🔍 Q3: Final Modifier Semantics | final 修饰符语义

**English**: Can elements be added to final List<String> hobbies? Why?  
**中文**: final的List<String> hobbies能否添加元素？为什么？

#### 💡 Answer | 答案

```java
class User {
    final String id;                    // ✅ Primitive: value immutable
    final List<String> hobbies;         // ✅ Reference: reference immutable
    
    public User() {
        id = "123";
        hobbies = new ArrayList<>();
        hobbies.add("reading");         // ✅ Can modify object content
        // hobbies = new LinkedList();  // ❌ Cannot change reference
    }
}
```

**Final Semantics | final语义**:
- Primitive types: Value immutable | 基本类型：值不可变
- Reference types: Reference immutable (content mutable) | 引用类型：引用不可变（内容可变）

---

## 🔄 五、Iterators & Traversal Logic | 迭代器与遍历逻辑

### 🔍 Q1: LinkedList Traversal Efficiency | LinkedList 遍历效率

**English**: Which traversal method is more efficient? Why?  
**中文**: 哪种遍历方式更高效？为什么？

#### 💡 Answer | 答案

```java
// ❌ Method ①: O(n²) - Inefficient
for(int i = 0; i < list.size(); i++) {
    list.get(i);  // Each get(i) traverses from head
}

// ✅ Method ②: O(n) - Efficient  
Iterator iter = list.iterator();
while(iter.hasNext()) {
    iter.next();  // Iterator remembers position
}
```

**Time Complexity | 时间复杂度**:
- Method ①: 1 + 2 + 3 + ... + n = O(n²)
- Method ②: n operations = O(n)

---

### 🔍 Q2: Iterator Design Principles | 迭代器设计原则

**English**: Problems if iterator returns node pointers directly? Which ADT principle violated?  
**中文**: 迭代器直接返回节点指针有什么问题？违反什么原则？

#### 💡 Answer | 答案

```java
// ❌ Dangerous design - Exposes internal structure
class BadIterator {
    public Node getCurrentNode() { 
        return currentNode;  // Exposes node reference
    }
}

// ✅ Safe design - Hides implementation  
class GoodIterator {
    public E next() { 
        E data = currentNode.data;
        currentNode = currentNode.next;
        return data;  // Returns only data
    }
}
```

**Violated Principle | 违反原则**: ADT "Hide Implementation Details" principle | ADT"隐藏实现细节"原则

---

<div align="center">
📝 <em>Document organized · Happy learning! | 文档整理完成 · 祝学习顺利!</em>
</div>

---

## 🚀 Quick Navigation | 快速导航

| Section | 章节 | Key Topics | 关键主题 |
|---------|------|------------|----------|
| 🎯 One | 一 | ADT Core Concepts | ADT核心概念 |
| ⚙️ Two | 二 | Java OOP & ADT | Java面向对象 |
| 📊 Three | 三 | Performance Analysis | 性能分析 |
| 🔒 Four | 四 | Access Control | 访问控制 |
| 🔄 Five | 五 | Iterators | 迭代器 |

**🔗 Connect**: Feel free to contribute or raise issues! | 欢迎贡献或提出问题！

---

*Last updated: 2024 | 最后更新: 2024年*
