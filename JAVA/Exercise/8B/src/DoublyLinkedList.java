import java.util.*;
import java.util.function.Consumer;

public class DoublyLinkedList<T> implements List<T> {

    // 节点类
    private static class Node<T> {
        T data;
        Node<T> prev;
        Node<T> next;

        Node(T data) {
            this.data = data;
        }

        Node(T data, Node<T> prev, Node<T> next) {
            this.data = data;
            this.prev = prev;
            this.next = next;
        }
    }

    // 链表属性
    private Node<T> head;
    private Node<T> tail;
    private int size;

    // 构造方法
    public DoublyLinkedList() {
        head = null;
        tail = null;
        size = 0;
    }

    /**
     * Returns the number of elements in this list.
     */
    @Override
    public int size() {
        return size;
    }

    /**
     * Returns {@code true} if this list contains no elements.
     */
    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Appends the specified element to the end of this list.
     */
    @Override
    public boolean add(T t) {
        addLast(t);
        return true;
    }

    // 在链表尾部添加元素
    public void addLast(T t) {
        Node<T> newNode = new Node<>(t);
        if (isEmpty()) {
            head = tail = newNode;
        } else {
            tail.next = newNode;
            newNode.prev = tail;
            tail = newNode;
        }
        size++;
    }

    /**
     * Removes the first occurrence of the specified element from this list.
     */
    @Override
    public boolean remove(Object o) {
        if (isEmpty()) {
            return false;
        }

        Node<T> current = head;
        while (current != null) {
            if (Objects.equals(o, current.data)) {
                removeNode(current);
                return true;
            }
            current = current.next;
        }
        return false;
    }

    // 移除指定节点
    private void removeNode(Node<T> node) {
        if (node.prev != null) {
            node.prev.next = node.next;
        } else {
            head = node.next;
        }

        if (node.next != null) {
            node.next.prev = node.prev;
        } else {
            tail = node.prev;
        }

        size--;
    }

    /**
     * Returns the element at the specified position in this list.
     */
    @Override
    public T get(int index) {
        checkIndex(index, false); // false 表示不能等于size
        return getNode(index).data;
    }

    // 获取指定位置的节点
    private Node<T> getNode(int index) {
        // 优化：根据位置决定从头还是从尾开始遍历
        if (index < size / 2) {
            Node<T> current = head;
            for (int i = 0; i < index; i++) {
                current = current.next;
            }
            return current;
        } else {
            Node<T> current = tail;
            for (int i = size - 1; i > index; i--) {
                current = current.prev;
            }
            return current;
        }
    }

    // 检查索引是否合法
    private void checkIndex(int index, boolean allowEqualSize) {
        int maxIndex = allowEqualSize ? size : size - 1;
        if (index < 0 || index > maxIndex) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
    }

    /**
     * Replaces the element at the specified position in this list with the specified element.
     */
    @Override
    public T set(int index, T element) {
        checkIndex(index, false);
        Node<T> node = getNode(index);
        T oldData = node.data;
        node.data = element;
        return oldData;
    }

    /**
     * Returns {@code true} if this list contains all of the elements of the specified collection.
     */
    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object element : c) {
            if (!contains(element)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns an iterator over the elements in this list in proper sequence.
     */
    @Override
    public Iterator<T> iterator() {
        return new DoublyLinkedListIterator();
    }

    // 迭代器实现
    private class DoublyLinkedListIterator implements Iterator<T> {
        private Node<T> current = head;
        private Node<T> lastReturned = null;

        @Override
        public boolean hasNext() {
            return current != null;
        }

        @Override
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            lastReturned = current;
            T data = current.data;
            current = current.next;
            return data;
        }

        @Override
        public void remove() {
            if (lastReturned == null) {
                throw new IllegalStateException();
            }
            removeNode(lastReturned);
            lastReturned = null;
        }
    }

    // 以下是你要求的方法之外的实现，但为了完整性我提供了一些关键方法

    /**
     * Returns {@code true} if this list contains the specified element.
     */
    @Override
    public boolean contains(Object o) {
        return indexOf(o) != -1;
    }

    /**
     * Returns the index of the first occurrence of the specified element in this list.
     */
    @Override
    public int indexOf(Object o) {
        Node<T> current = head;
        int index = 0;
        while (current != null) {
            if (Objects.equals(o, current.data)) {
                return index;
            }
            current = current.next;
            index++;
        }
        return -1;
    }

    /**
     * Removes all of the elements from this list.
     */
    @Override
    public void clear() {
        head = null;
        tail = null;
        size = 0;
    }

    /**
     * Inserts the specified element at the specified position in this list.
     */
    @Override
    public void add(int index, T element) {
        checkIndex(index, true); // true 表示可以等于size（在尾部添加）

        if (index == size) {
            addLast(element);
        } else if (index == 0) {
            addFirst(element);
        } else {
            Node<T> target = getNode(index);
            Node<T> newNode = new Node<>(element, target.prev, target);
            target.prev.next = newNode;
            target.prev = newNode;
            size++;
        }
    }

    // 在链表头部添加元素
    public void addFirst(T t) {
        Node<T> newNode = new Node<>(t);
        if (isEmpty()) {
            head = tail = newNode;
        } else {
            newNode.next = head;
            head.prev = newNode;
            head = newNode;
        }
        size++;
    }

    /**
     * Removes the element at the specified position in this list.
     */
    @Override
    public T remove(int index) {
        checkIndex(index, false);
        Node<T> node = getNode(index);
        T removedData = node.data;
        removeNode(node);
        return removedData;
    }

    // 其他方法暂时保持默认实现或简单实现
    @Override
    public Object[] toArray() {
        Object[] array = new Object[size];
        Node<T> current = head;
        for (int i = 0; i < size; i++) {
            array[i] = current.data;
            current = current.next;
        }
        return array;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T1> T1[] toArray(T1[] a) {
        if (a.length < size) {
            a = (T1[]) java.lang.reflect.Array.newInstance(
                    a.getClass().getComponentType(), size);
        }

        Object[] result = a;
        Node<T> current = head;
        for (int i = 0; i < size; i++) {
            result[i] = current.data;
            current = current.next;
        }

        if (a.length > size) {
            a[size] = null;
        }

        return a;
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        for (T element : c) {
            add(element);
        }
        return !c.isEmpty();
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        checkIndex(index, true);

        if (c.isEmpty()) {
            return false;
        }

        int i = index;
        for (T element : c) {
            add(i++, element);
        }
        return true;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean modified = false;
        Iterator<T> it = iterator();
        while (it.hasNext()) {
            if (c.contains(it.next())) {
                it.remove();
                modified = true;
            }
        }
        return modified;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        boolean modified = false;
        Iterator<T> it = iterator();
        while (it.hasNext()) {
            if (!c.contains(it.next())) {
                it.remove();
                modified = true;
            }
        }
        return modified;
    }

    @Override
    public int lastIndexOf(Object o) {
        Node<T> current = tail;
        int index = size - 1;
        while (current != null) {
            if (Objects.equals(o, current.data)) {
                return index;
            }
            current = current.prev;
            index--;
        }
        return -1;
    }

    @Override
    public ListIterator<T> listIterator() {
        return new DoublyLinkedListListIterator(0);
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        checkIndex(index, true);
        return new DoublyLinkedListListIterator(index);
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        // 简化实现 - 实际应该返回一个视图
        checkIndex(fromIndex, false);
        checkIndex(toIndex, true);
        if (fromIndex > toIndex) {
            throw new IllegalArgumentException("fromIndex > toIndex");
        }

        DoublyLinkedList<T> subList = new DoublyLinkedList<>();
        Node<T> current = getNode(fromIndex);
        for (int i = fromIndex; i < toIndex; i++) {
            subList.add(current.data);
            current = current.next;
        }
        return subList;
    }

    // 列表迭代器实现
    private class DoublyLinkedListListIterator implements ListIterator<T> {
        private Node<T> next;
        private Node<T> lastReturned = null;
        private int nextIndex;

        DoublyLinkedListListIterator(int index) {
            next = (index == size) ? null : getNode(index);
            nextIndex = index;
        }

        @Override
        public boolean hasNext() {
            return nextIndex < size;
        }

        @Override
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            lastReturned = next;
            next = next.next;
            nextIndex++;
            return lastReturned.data;
        }

        @Override
        public boolean hasPrevious() {
            return nextIndex > 0;
        }

        @Override
        public T previous() {
            if (!hasPrevious()) {
                throw new NoSuchElementException();
            }
            next = (next == null) ? tail : next.prev;
            lastReturned = next;
            nextIndex--;
            return lastReturned.data;
        }

        @Override
        public int nextIndex() {
            return nextIndex;
        }

        @Override
        public int previousIndex() {
            return nextIndex - 1;
        }

        @Override
        public void remove() {
            if (lastReturned == null) {
                throw new IllegalStateException();
            }
            Node<T> lastNext = lastReturned.next;
            removeNode(lastReturned);
            if (next == lastReturned) {
                next = lastNext;
            } else {
                nextIndex--;
            }
            lastReturned = null;
        }

        @Override
        public void set(T t) {
            if (lastReturned == null) {
                throw new IllegalStateException();
            }
            lastReturned.data = t;
        }

        @Override
        public void add(T t) {
            lastReturned = null;
            if (next == null) {
                addLast(t);
            } else {
                Node<T> newNode = new Node<>(t, next.prev, next);
                if (next.prev != null) {
                    next.prev.next = newNode;
                } else {
                    head = newNode;
                }
                next.prev = newNode;
                size++;
            }
            nextIndex++;
        }
    }
}