import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.NoSuchElementException;
import java.util.ConcurrentModificationException;
import java.util.AbstractList;


public class ConsListList<T> implements java.util.List<T> {

    /* ========= Internal representation | 内部表示 ========= */
    private static final class Node<E> {
        E v;
        Node<E> next;
        Node(E v) { this.v = v; }
    }
    private Node<T> head;
    private Node<T> tail;
    private int size;

    /** 结构性修改计数（fail-fast 迭代器用） */
    private int modCount;

    /** Creates a new empty ConsListList */
    public ConsListList() { head = tail = null; size = 0; modCount = 0; }

    /* ========= helpers ========= */
    private void checkIndex(int index) {
        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException("index=" + index + ", size=" + size);
    }
    private void checkInsertIndex(int index) {
        if (index < 0 || index > size)
            throw new IndexOutOfBoundsException("index=" + index + ", size=" + size);
    }
    private Node<T> nodeAt(int index) {
        Node<T> cur = head;
        for (int i = 0; i < index; i++) cur = cur.next;
        return cur;
    }

    /* ====== 已实现的方法：add(T), size, isEmpty, get, set, remove(Object) ====== */
    @Override public boolean add(T t) {
        Node<T> n = new Node<>(t);
        if (head == null) head = tail = n;
        else { tail.next = n; tail = n; }
        size++; modCount++; // 结构性修改
        return true;
    }
    @Override public int size() { return size; }
    @Override public boolean isEmpty() { return size == 0; }
    @Override public T get(int index) { checkIndex(index); return nodeAt(index).v; }
    @Override public T set(int index, T t) { checkIndex(index); Node<T> n = nodeAt(index); T old = n.v; n.v = t; return old; }
    @Override public boolean remove(Object o) {
        Node<T> prev = null, curr = head;
        while (curr != null) {
            if (Objects.equals(curr.v, o)) {
                if (prev == null) head = curr.next; else prev.next = curr.next;
                if (curr == tail) tail = prev;
                size--; modCount++; // 结构性修改
                return true;
            }
            prev = curr; curr = curr.next;
        }
        return false;
    }

    /* ===================== contains / 批量操作 / 清空 / 按位增删 ===================== */
    @Override
    public boolean contains(Object o) {
        for (Node<T> cur = head; cur != null; cur = cur.next) {
            if (Objects.equals(cur.v, o)) return true;
        }
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        if (c == null) throw new NullPointerException("collection is null");
        for (Object e : c) if (!contains(e)) return false;
        return true;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        if (c == null) throw new NullPointerException("collection is null");
        if (c == this) { // 删除“本表里所有本表元素”== 清空
            if (size == 0) return false;
            clear(); return true;
        }
        boolean changed = false;
        Node<T> prev = null, curr = head;
        while (curr != null) {
            if (c.contains(curr.v)) {
                if (prev == null) head = curr.next; else prev.next = curr.next;
                if (curr == tail) tail = prev;
                size--; changed = true;
                curr = (prev == null) ? head : prev.next; // 不前移 prev
            } else {
                prev = curr; curr = curr.next;
            }
        }
        if (changed) modCount++; // 一次性记修改（足以触发 fail-fast）
        return changed;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        if (c == null) throw new NullPointerException("collection is null");
        if (c == this) return false;
        if (c.isEmpty()) {
            if (size == 0) return false;
            clear(); return true;
        }
        boolean changed = false;
        Node<T> prev = null, curr = head;
        while (curr != null) {
            if (!c.contains(curr.v)) {
                if (prev == null) head = curr.next; else prev.next = curr.next;
                if (curr == tail) tail = prev;
                size--; changed = true;
                curr = (prev == null) ? head : prev.next;
            } else {
                prev = curr; curr = curr.next;
            }
        }
        if (changed) modCount++;
        return changed;
    }

    @Override
    public void clear() {
        if (size != 0) { // 仅在有改动时增加 modCount
            head = tail = null;
            size = 0;
            modCount++;
        }
    }

    @Override
    public void add(int index, T element) throws IndexOutOfBoundsException {
        checkInsertIndex(index);
        if (index == size) { add(element); return; }
        if (index == 0) {
            Node<T> n = new Node<>(element);
            n.next = head;
            head = n;
            if (tail == null) tail = n;
            size++; modCount++;
            return;
        }
        Node<T> prev = nodeAt(index - 1);
        Node<T> n = new Node<>(element);
        n.next = prev.next;
        prev.next = n;
        size++; modCount++;
    }

    @Override
    public T remove(int index) throws IndexOutOfBoundsException {
        checkIndex(index);
        T old;
        if (index == 0) {
            Node<T> r = head;
            old = r.v;
            head = r.next;
            if (r == tail) tail = null;
        } else {
            Node<T> prev = nodeAt(index - 1);
            Node<T> r = prev.next;
            old = r.v;
            prev.next = r.next;
            if (r == tail) tail = prev;
        }
        size--; modCount++;
        return old;
    }

    @Override public int indexOf(Object o) {
        int i = 0;
        for (Node<T> cur = head; cur != null; cur = cur.next, i++) {
            if (Objects.equals(cur.v, o)) return i;
        }
        return -1;
    }
    @Override public int lastIndexOf(Object o) {
        int i = 0, last = -1;
        for (Node<T> cur = head; cur != null; cur = cur.next, i++) {
            if (Objects.equals(cur.v, o)) last = i;
        }
        return last;
    }

    @Override
    public java.util.List<T> subList(int fromIndex, int toIndex) throws IndexOutOfBoundsException {
        if (fromIndex < 0 || toIndex < fromIndex || toIndex > size)
            throw new IndexOutOfBoundsException("from=" + fromIndex + ", to=" + toIndex + ", size=" + size);
        return new SubList(fromIndex, toIndex);
    }

    /* ---------- Inner backed view ---------- */
    private final class SubList extends AbstractList<T> {
        private int offset;              // 在父表中的起始位置
        private int length;              // 视图长度
        private int expectedModCount;    // 捕获创建时父表的 modCount，用于 fail-fast

        SubList(int from, int to) {
            this.offset = from;
            this.length = to - from;
            this.expectedModCount = modCount;
        }

        /* 小工具：并发修改检测 */
        private void checkForComodification() {
            if (expectedModCount != modCount) throw new ConcurrentModificationException();
        }
        private void rangeCheckIndex(int index) {
            if (index < 0 || index >= length) throw new IndexOutOfBoundsException("index=" + index + ", length=" + length);
        }
        private void rangeCheckIndexForAdd(int index) {
            if (index < 0 || index > length) throw new IndexOutOfBoundsException("index=" + index + ", length=" + length);
        }

        /* Design Recipe — size()
         * Purpose: 返回视图中的元素个数
         * Strategy: 返回 length，先做 fail-fast 检查
         */
        @Override
        public int size() {
            checkForComodification();
            return length;
        }

        /* Design Recipe — get(int)
         * Purpose: 读取区间内第 index 个元素
         * Strategy: 越界检查 → fail-fast → 读取父表 offset+index 位置
         */
        @Override
        public T get(int index) {
            rangeCheckIndex(index);
            checkForComodification();
            return ConsListList.this.get(offset + index);
        }

        /* Design Recipe — set(int, T)
         * Purpose: 就地修改区间内元素；非结构性修改（不改变长度）
         * Strategy: 越界检查 → fail-fast → 委托父表 set
         */
        @Override
        public T set(int index, T element) {
            rangeCheckIndex(index);
            checkForComodification();
            // 父表 set 不改变结构，因此父表 modCount 不变，无需刷新 expectedModCount
            return ConsListList.this.set(offset + index, element);
        }

        /* Design Recipe — add(int, T)
         * Purpose: 在视图 index 处插入（结构性修改）
         * Strategy: 检查/失败快 → 调用父表 add(offset+index, e)
         * Effect: 视图长度 +1；父表 modCount 变动 → 刷新 expectedModCount
         */
        @Override
        public void add(int index, T element) {
            rangeCheckIndexForAdd(index);
            checkForComodification();
            ConsListList.this.add(offset + index, element);
            length++;
            expectedModCount = modCount; // 父表已变动，刷新
        }

        /* Design Recipe — remove(int)
         * Purpose: 删除并返回视图 index 处元素（结构性修改）
         * Strategy: 检查/失败快 → 调用父表 remove(offset+index)
         * Effect: 视图长度 -1；父表 modCount 变动 → 刷新 expectedModCount
         */
        @Override
        public T remove(int index) {
            rangeCheckIndex(index);
            checkForComodification();
            T old = ConsListList.this.remove(offset + index);
            length--;
            expectedModCount = modCount;
            return old;
        }

        /* Design Recipe — clear()
         * Purpose: 清空本视图对应的区间
         * Strategy: 反复从 offset 开始删除 length 次（每次父表结构变更）
         * Effect: length 置 0，刷新 expectedModCount
         */
        @Override
        public void clear() {
            checkForComodification();
            for (int i = 0; i < length; i++) {
                ConsListList.this.remove(offset); // 总是删“当前区间起点”
            }
            length = 0;
            expectedModCount = modCount;
        }

        /* 可选：用自定义迭代器，只遍历区间并做 fail-fast */
        @Override
        public Iterator<T> iterator() {
            checkForComodification();
            return new Iterator<T>() {
                private int i = 0;
                private final int startMod = modCount;
                @Override public boolean hasNext() { return i < length; }
                @Override public T next() {
                    if (startMod != modCount) throw new ConcurrentModificationException();
                    if (i >= length) throw new NoSuchElementException();
                    return ConsListList.this.get(offset + i++);
                }
                @Override public void remove() {
                    throw new UnsupportedOperationException("iterator.remove not supported");
                }
            };
        }
    }

    /* ===================== 重点：迭代器 ===================== */

    /**
     * Design Recipe — Signature & Purpose / 签名与用途：
     *   Iterator<T> iterator()
     *   Return an iterator over this list from head to tail.
     *   返回从头到尾的迭代器。
     *
     * Examples / 例子：
     *   for (T x : list) { ... } // uses this iterator
     *
     * Strategy / 策略：
     *   使用内部类保存当前节点指针；创建时捕获 expectedModCount 并在 next() 前做 fail-fast。
     */
    @Override
    public Iterator<T> iterator() {
        return new ConsListIterator();
    }

    /**
     * 内部迭代器 | Inner iterator (fail-fast)
     *
     * Design Recipe — Invariants / 不变量：
     *   - cursor 指向下一个要返回的节点（可能为 null）
     *   - expectedModCount == 外部 modCount 才是有效迭代器
     */
    private class ConsListIterator implements Iterator<T> {
        private Node<T> cursor = head;
        private final int expectedModCount = modCount;

        /**
         * hasNext()
         * Purpose：
         *   是否还有元素可迭代（cursor != null）
         * Strategy：
         *   O(1) 判断指针是否为 null。
         */
        @Override
        public boolean hasNext() {
            return cursor != null;
        }

        /**
         * next() throws NoSuchElementException
         * Purpose：
         *   返回下一个元素，并将 cursor 向前推进一格。
         * Strategy：
         *   先做 fail-fast 并发检测；若无下一个元素抛 NoSuchElementException；否则返回值并前移。
         *
         * Note：
         *   允许在并发修改时抛 ConcurrentModificationException（无需在 throws 子句声明）。
         */
        @Override
        public T next() {
            if (expectedModCount != modCount) throw new ConcurrentModificationException();
            if (cursor == null) throw new NoSuchElementException();
            T val = cursor.v;
            cursor = cursor.next;
            return val;
        }

        /** 非作业要求：不支持通过迭代器删除 */
        @Override
        public void remove() {
            throw new UnsupportedOperationException("iterator.remove not supported");
        }
    }

    /* ===== 其余接口方法（未在作业中要求）可保留占位或按需实现 ===== */
    @Override public Object[] toArray() { return new Object[0]; }
    @Override public <T1> T1[] toArray(T1[] a) { return null; }
    @Override public boolean addAll(Collection<? extends T> c) { return false; }
    @Override public boolean addAll(int index, Collection<? extends T> c) { return false; }
    @Override public ListIterator<T> listIterator() { return null; }
    @Override public ListIterator<T> listIterator(int index) { return null; }

}
