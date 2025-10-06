import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

class DoublyLinkedListTest {

    private DoublyLinkedList<String> list;
    private DoublyLinkedList<Integer> intList;

    @BeforeEach
    void setUp() {
        list = new DoublyLinkedList<>();
        intList = new DoublyLinkedList<>();
    }

    // ========== size() 测试 ==========
    @Test
    @DisplayName("size() - 空链表返回0")
    void size_emptyList_returnsZero() {
        assertEquals(0, list.size());
    }

    @Test
    @DisplayName("size() - 添加元素后返回正确大小")
    void size_afterAdditions_returnsCorrectSize() {
        list.add("A");
        list.add("B");
        assertEquals(2, list.size());
    }

    @Test
    @DisplayName("size() - 删除元素后返回正确大小")
    void size_afterRemovals_returnsCorrectSize() {
        list.add("A");
        list.add("B");
        list.add("C");
        list.remove("B");
        assertEquals(2, list.size());
    }

    // ========== isEmpty() 测试 ==========
    @Test
    @DisplayName("isEmpty() - 空链表返回true")
    void isEmpty_emptyList_returnsTrue() {
        assertTrue(list.isEmpty());
    }

    @Test
    @DisplayName("isEmpty() - 非空链表返回false")
    void isEmpty_nonEmptyList_returnsFalse() {
        list.add("A");
        assertFalse(list.isEmpty());
    }

    @Test
    @DisplayName("isEmpty() - 清空后返回true")
    void isEmpty_afterClear_returnsTrue() {
        list.add("A");
        list.add("B");
        list.clear();
        assertTrue(list.isEmpty());
    }

    // ========== add(T t) 测试 ==========
    @Test
    @DisplayName("add(T) - 添加元素到空链表")
    void add_toEmptyList_addsElement() {
        assertTrue(list.add("A"));
        assertEquals(1, list.size());
        assertEquals("A", list.get(0));
    }

    @Test
    @DisplayName("add(T) - 添加多个元素")
    void add_multipleElements_addsInOrder() {
        list.add("A");
        list.add("B");
        list.add("C");
        assertEquals(3, list.size());
        assertEquals("A", list.get(0));
        assertEquals("B", list.get(1));
        assertEquals("C", list.get(2));
    }

    @Test
    @DisplayName("add(T) - 添加null元素")
    void add_nullElement_addsSuccessfully() {
        assertTrue(list.add(null));
        assertTrue(list.contains(null));
    }

    // ========== remove(Object o) 测试 ==========
    @Test
    @DisplayName("remove(Object) - 从空链表移除返回false")
    void remove_fromEmptyList_returnsFalse() {
        assertFalse(list.remove("A"));
    }

    @Test
    @DisplayName("remove(Object) - 移除不存在的元素返回false")
    void remove_nonExistentElement_returnsFalse() {
        list.add("A");
        list.add("B");
        assertFalse(list.remove("C"));
        assertEquals(2, list.size());
    }

    @Test
    @DisplayName("remove(Object) - 移除第一个元素")
    void remove_firstElement_removesSuccessfully() {
        list.add("A");
        list.add("B");
        list.add("C");
        assertTrue(list.remove("A"));
        assertEquals(2, list.size());
        assertEquals("B", list.get(0));
        assertEquals("C", list.get(1));
    }

    @Test
    @DisplayName("remove(Object) - 移除中间元素")
    void remove_middleElement_removesSuccessfully() {
        list.add("A");
        list.add("B");
        list.add("C");
        assertTrue(list.remove("B"));
        assertEquals(2, list.size());
        assertEquals("A", list.get(0));
        assertEquals("C", list.get(1));
    }

    @Test
    @DisplayName("remove(Object) - 移除最后一个元素")
    void remove_lastElement_removesSuccessfully() {
        list.add("A");
        list.add("B");
        list.add("C");
        assertTrue(list.remove("C"));
        assertEquals(2, list.size());
        assertEquals("A", list.get(0));
        assertEquals("B", list.get(1));
    }

    @Test
    @DisplayName("remove(Object) - 移除null元素")
    void remove_nullElement_removesSuccessfully() {
        list.add("A");
        list.add(null);
        list.add("B");
        assertTrue(list.remove(null));
        assertEquals(2, list.size());
        assertFalse(list.contains(null));
    }

    @Test
    @DisplayName("remove(Object) - 只移除第一个匹配的元素")
    void remove_duplicateElements_removesOnlyFirst() {
        list.add("A");
        list.add("B");
        list.add("A");
        list.add("C");
        assertTrue(list.remove("A"));
        assertEquals(3, list.size());
        assertEquals("B", list.get(0));
        assertEquals("A", list.get(1));
        assertEquals("C", list.get(2));
    }

    // ========== get(int index) 测试 ==========
    @Test
    @DisplayName("get(int) - 获取有效索引的元素")
    void get_validIndex_returnsElement() {
        list.add("A");
        list.add("B");
        list.add("C");
        assertEquals("A", list.get(0));
        assertEquals("B", list.get(1));
        assertEquals("C", list.get(2));
    }

    @Test
    @DisplayName("get(int) - 负索引抛出异常")
    void get_negativeIndex_throwsException() {
        list.add("A");
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(-1));
    }

    @Test
    @DisplayName("get(int) - 超出范围的索引抛出异常")
    void get_indexOutOfBounds_throwsException() {
        list.add("A");
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(1));
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(100));
    }

    @Test
    @DisplayName("get(int) - 空链表获取索引抛出异常")
    void get_emptyList_throwsException() {
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(0));
    }

    // ========== set(int index, T t) 测试 ==========
    @Test
    @DisplayName("set(int, T) - 设置有效索引的元素")
    void set_validIndex_replacesElement() {
        list.add("A");
        list.add("B");
        list.add("C");

        String oldValue = list.set(1, "X");
        assertEquals("B", oldValue);
        assertEquals("X", list.get(1));
        assertEquals(3, list.size());
    }

    @Test
    @DisplayName("set(int, T) - 设置第一个元素")
    void set_firstElement_replacesSuccessfully() {
        list.add("A");
        list.add("B");

        assertEquals("A", list.set(0, "First"));
        assertEquals("First", list.get(0));
    }

    @Test
    @DisplayName("set(int, T) - 设置最后一个元素")
    void set_lastElement_replacesSuccessfully() {
        list.add("A");
        list.add("B");

        assertEquals("B", list.set(1, "Last"));
        assertEquals("Last", list.get(1));
    }

    @Test
    @DisplayName("set(int, T) - 设置null值")
    void set_nullValue_replacesSuccessfully() {
        list.add("A");
        list.set(0, null);
        assertNull(list.get(0));
    }

    @Test
    @DisplayName("set(int, T) - 无效索引抛出异常")
    void set_invalidIndex_throwsException() {
        list.add("A");
        assertThrows(IndexOutOfBoundsException.class, () -> list.set(-1, "X"));
        assertThrows(IndexOutOfBoundsException.class, () -> list.set(1, "X"));
    }

    // ========== containsAll(Collection<?> c) 测试 ==========
    @Test
    @DisplayName("containsAll(Collection) - 空集合返回true")
    void containsAll_emptyCollection_returnsTrue() {
        list.add("A");
        list.add("B");
        assertTrue(list.containsAll(Collections.emptyList()));
    }

    @Test
    @DisplayName("containsAll(Collection) - 所有元素都存在返回true")
    void containsAll_allElementsExist_returnsTrue() {
        list.add("A");
        list.add("B");
        list.add("C");
        assertTrue(list.containsAll(Arrays.asList("A", "C")));
    }

    @Test
    @DisplayName("containsAll(Collection) - 有元素不存在返回false")
    void containsAll_someElementsMissing_returnsFalse() {
        list.add("A");
        list.add("B");
        assertFalse(list.containsAll(Arrays.asList("A", "C")));
    }

    @Test
    @DisplayName("containsAll(Collection) - 所有元素都不存在返回false")
    void containsAll_noElementsExist_returnsFalse() {
        list.add("A");
        list.add("B");
        assertFalse(list.containsAll(Arrays.asList("X", "Y")));
    }

    @Test
    @DisplayName("containsAll(Collection) - 包含重复元素")
    void containsAll_duplicateElements_handlesCorrectly() {
        list.add("A");
        list.add("B");
        list.add("A");
        assertTrue(list.containsAll(Arrays.asList("A", "A")));
    }

    @Test
    @DisplayName("containsAll(Collection) - 空链表检查非空集合返回false")
    void containsAll_emptyListWithNonEmptyCollection_returnsFalse() {
        assertFalse(list.containsAll(Arrays.asList("A")));
    }

    @Test
    @DisplayName("containsAll(Collection) - null元素检查")
    void containsAll_nullElements_handlesCorrectly() {
        list.add("A");
        list.add(null);
        list.add("B");
        assertTrue(list.containsAll(Arrays.asList(null, "A")));
        assertFalse(list.containsAll(Arrays.asList(null, "C")));
    }

    // ========== iterator() 测试 ==========
    @Test
    @DisplayName("iterator() - 空链表迭代器")
    void iterator_emptyList_hasNoElements() {
        Iterator<String> iterator = list.iterator();
        assertFalse(iterator.hasNext());
        assertThrows(NoSuchElementException.class, iterator::next);
    }

    @Test
    @DisplayName("iterator() - 迭代所有元素")
    void iterator_nonEmptyList_iteratesAllElements() {
        list.add("A");
        list.add("B");
        list.add("C");

        Iterator<String> iterator = list.iterator();
        List<String> result = new ArrayList<>();
        while (iterator.hasNext()) {
            result.add(iterator.next());
        }

        assertEquals(Arrays.asList("A", "B", "C"), result);
    }

    @Test
    @DisplayName("iterator() - 迭代器remove方法")
    void iterator_remove_removesCurrentElement() {
        list.add("A");
        list.add("B");
        list.add("C");

        Iterator<String> iterator = list.iterator();
        iterator.next(); // A
        iterator.next(); // B
        iterator.remove(); // Remove B

        assertEquals(2, list.size());
        assertEquals("A", list.get(0));
        assertEquals("C", list.get(1));
        assertFalse(list.contains("B"));
    }

    @Test
    @DisplayName("iterator() - 连续remove抛出异常")
    void iterator_doubleRemove_throwsException() {
        list.add("A");
        list.add("B");

        Iterator<String> iterator = list.iterator();
        iterator.next();
        iterator.remove();

        assertThrows(IllegalStateException.class, iterator::remove);
    }

    @Test
    @DisplayName("iterator() - 在next之前remove抛出异常")
    void iterator_removeBeforeNext_throwsException() {
        list.add("A");
        Iterator<String> iterator = list.iterator();
        assertThrows(IllegalStateException.class, iterator::remove);
    }

    @Test
    @DisplayName("iterator() - 遍历时修改列表")
    void iterator_concurrentModification_detected() {
        list.add("A");
        list.add("B");
        list.add("C");

        Iterator<String> iterator = list.iterator();
        iterator.next(); // A

        // 通过列表直接修改（不是通过迭代器）
        list.remove("B");

        // 应该检测到并发修改
        assertThrows(ConcurrentModificationException.class, iterator::next);
    }

    // ========== 综合测试 ==========
    @Test
    @DisplayName("综合测试 - 多种操作组合")
    void comprehensiveTest_multipleOperations() {
        // 初始添加
        assertTrue(list.isEmpty());
        list.add("A");
        list.add("B");
        list.add("C");
        assertEquals(3, list.size());
        assertFalse(list.isEmpty());

        // 中间插入
        list.add(1, "X");
        assertEquals(4, list.size());
        assertEquals(Arrays.asList("A", "X", "B", "C"), toList(list));

        // 设置元素
        list.set(2, "Y");
        assertEquals("Y", list.get(2));

        // 移除元素
        assertTrue(list.remove("X"));
        assertEquals(3, list.size());

        // 通过索引移除
        assertEquals("Y", list.remove(1));
        assertEquals(2, list.size());

        // 包含检查
        assertTrue(list.contains("A"));
        assertTrue(list.contains("C"));
        assertFalse(list.contains("B"));

        // 索引查找
        assertEquals(0, list.indexOf("A"));
        assertEquals(1, list.indexOf("C"));

        // 清空
        list.clear();
        assertTrue(list.isEmpty());
        assertEquals(0, list.size());
    }

    @Test
    @DisplayName("性能测试 - 大数据量操作")
    void performanceTest_largeDataSet() {
        int count = 10000;

        // 添加大量元素
        for (int i = 0; i < count; i++) {
            intList.add(i);
        }
        assertEquals(count, intList.size());

        // 随机访问
        for (int i = 0; i < 100; i++) {
            int randomIndex = new Random().nextInt(count);
            assertEquals(randomIndex, intList.get(randomIndex).intValue());
        }

        // 迭代所有元素
        AtomicInteger sum = new AtomicInteger();
        intList.iterator().forEachRemaining(sum::addAndGet);
        assertEquals(count * (count - 1) / 2, sum.get());
    }

    @Test
    @DisplayName("边界测试 - 单个元素链表")
    void boundaryTest_singleElementList() {
        list.add("A");

        assertEquals(1, list.size());
        assertFalse(list.isEmpty());
        assertEquals("A", list.get(0));
        assertEquals("A", list.set(0, "B"));
        assertEquals("B", list.get(0));
        assertTrue(list.contains("B"));
        assertEquals(0, list.indexOf("B"));
        assertEquals(0, list.lastIndexOf("B"));
        assertTrue(list.remove("B"));
        assertTrue(list.isEmpty());
    }

    // 辅助方法：将DoublyLinkedList转换为List以便比较
    private <T> List<T> toList(DoublyLinkedList<T> dll) {
        List<T> result = new ArrayList<>();
        for (T element : dll) {
            result.add(element);
        }
        return result;
    }

    // ========== 其他重要方法的测试 ==========
    @Test
    @DisplayName("add(int, T) - 在指定位置插入")
    void add_atIndex_insertsAtCorrectPosition() {
        list.add("A");
        list.add("C");
        list.add(1, "B");

        assertEquals(3, list.size());
        assertEquals("A", list.get(0));
        assertEquals("B", list.get(1));
        assertEquals("C", list.get(2));
    }

    @Test
    @DisplayName("clear() - 清空链表")
    void clear_removesAllElements() {
        list.add("A");
        list.add("B");
        list.add("C");

        list.clear();

        assertEquals(0, list.size());
        assertTrue(list.isEmpty());
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(0));
    }

    @Test
    @DisplayName("indexOf() - 查找元素索引")
    void indexOf_findsCorrectIndex() {
        list.add("A");
        list.add("B");
        list.add("A"); // 重复元素

        assertEquals(0, list.indexOf("A"));
        assertEquals(1, list.indexOf("B"));
        assertEquals(-1, list.indexOf("C"));
    }

    @Test
    @DisplayName("lastIndexOf() - 查找最后出现的位置")
    void lastIndexOf_findsLastOccurrence() {
        list.add("A");
        list.add("B");
        list.add("A"); // 重复元素

        assertEquals(2, list.lastIndexOf("A"));
        assertEquals(1, list.lastIndexOf("B"));
        assertEquals(-1, list.lastIndexOf("C"));
    }

    @Test
    @DisplayName("toArray() - 转换为数组")
    void toArray_returnsCorrectArray() {
        list.add("A");
        list.add("B");
        list.add("C");

        Object[] array = list.toArray();
        assertArrayEquals(new Object[]{"A", "B", "C"}, array);
    }

    @Test
    @DisplayName("toArray(T[]) - 转换为指定类型数组")
    void toArray_withType_returnsCorrectArray() {
        list.add("A");
        list.add("B");
        list.add("C");

        String[] array = list.toArray(new String[0]);
        assertArrayEquals(new String[]{"A", "B", "C"}, array);
    }
}