import org.junit.jupiter.api.Test;

import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ConsListList and its backed subList view.
 *
 * 设计要点（对应使用场景）：
 * 1) subList.clear() 直接删除父表区间
 * 2) 通过 subList(from, from).add(...) 在父表指定位置插入
 * 3) subList.set(i, x) 会修改父表 from+i 位置
 * 4) subList.remove(i) 同步更新父表与视图
 * 5) 迭代与遍历行为正确
 * 6) 越界参数抛出 IndexOutOfBoundsException
 * 7) 父表被并发修改时，子表访问 fail-fast（ConcurrentModificationException）
 * 8) 迭代器的 remove() 不支持
 */
public class ConsListListSubListTest {

    /* ---------- helpers ---------- */

    /** 构造一个含给定元素的 ConsListList */
    private static ConsListList<Integer> listOf(int... xs) {
        ConsListList<Integer> l = new ConsListList<>();
        for (int x : xs) l.add(x);
        return l;
    }

    /** 快照：把任意 List 的当前内容拷到 ArrayList 便于断言（只用迭代，不调用 toArray） */
    private static <E> List<E> snapshot(List<E> src) {
        ArrayList<E> r = new ArrayList<>();
        for (E e : src) r.add(e);
        return r;
    }

    /* ---------- tests ---------- */

    @Test
    void subList_clear_removesRangeFromParent() {
        // Given
        ConsListList<Integer> list = listOf(0, 1, 2, 3, 4, 5, 6);
        List<Integer> win = list.subList(2, 5); // [2,3,4]

        // When: 清空子表窗口
        win.clear();

        // Then: 父表区间被删除
        assertEquals(List.of(0, 1, 5, 6), snapshot(list));
        assertEquals(List.of(), snapshot(win)); // 子表视图变空
    }

    @Test
    void insert_at_position_via_emptySubList() {
        // Given
        ConsListList<Integer> list = listOf(10, 20, 30, 40);
        List<Integer> at1 = list.subList(1, 1); // 锚定位置1的空窗口

        // When: 在位置1 依次插入
        at1.add(99);
        at1.add(98);

        // Then: 父表在 index=1 处依次插入，顺序保持
        assertEquals(List.of(10, 99, 98, 20, 30, 40), snapshot(list));
        assertEquals(List.of(99, 98), snapshot(at1)); // 子表窗口随之扩展
    }

    @Test
    void subList_set_reflects_in_parent() {
        // Given
        ConsListList<Integer> list = listOf(5, 6, 7, 8);
        List<Integer> mid = list.subList(1, 3); // [6,7]

        // When
        Integer old = mid.set(1, 99); // 改变子表 index=1（父表 index=2）

        // Then
        assertEquals(7, old);
        assertEquals(List.of(5, 6, 99, 8), snapshot(list));
        assertEquals(List.of(6, 99), snapshot(mid));
    }

    @Test
    void subList_remove_updates_parent_and_view() {
        // Given
        ConsListList<Integer> list = listOf(1, 2, 3, 4, 5);
        List<Integer> win = list.subList(1, 4); // [2,3,4]

        // When
        Integer removed = win.remove(1); // 移除子表 index=1（父表 index=2）

        // Then
        assertEquals(3, removed);
        assertEquals(List.of(1, 2, 4, 5), snapshot(list));
        assertEquals(List.of(2, 4), snapshot(win));
    }

    @Test
    void iterate_over_subList() {
        // Given
        ConsListList<Integer> list = listOf(7, 8, 9, 10, 11);
        List<Integer> win = list.subList(2, 5); // [9,10,11]

        // When
        ArrayList<Integer> seen = new ArrayList<>();
        for (Integer x : win) seen.add(x);

        // Then
        assertEquals(List.of(9, 10, 11), seen);
    }

    @Test
    void subList_bounds_checks() {
        ConsListList<Integer> list = listOf(1, 2, 3);

        assertThrows(IndexOutOfBoundsException.class, () -> list.subList(-1, 2));
        assertThrows(IndexOutOfBoundsException.class, () -> list.subList(0, 4));
        assertThrows(IndexOutOfBoundsException.class, () -> list.subList(3, 2)); // from > to
        assertDoesNotThrow(() -> list.subList(0, 3)); // 合法边界
        assertDoesNotThrow(() -> list.subList(3, 3)); // 空窗口
    }

    @Test
    void subList_is_failFast_when_parent_modified() {
        // Given
        ConsListList<Integer> list = listOf(1, 2, 3, 4, 5);
        List<Integer> sub = list.subList(1, 4); // [2,3,4]

        // When: 父表发生结构性修改
        list.add(99);

        // Then: 子表在后续访问时应 fail-fast
        assertThrows(ConcurrentModificationException.class, sub::size);
        assertThrows(ConcurrentModificationException.class, () -> sub.get(0));
        assertThrows(ConcurrentModificationException.class, () -> {
            for (Integer ignored : sub) { /* 触发迭代访问 */ break; }
        });
    }

    @Test
    void iterator_remove_is_unsupported_on_list_and_subList() {
        // list 迭代器的 remove 不支持
        ConsListList<Integer> list = listOf(1, 2, 3);
        Iterator<Integer> it = list.iterator();
        assertTrue(it.hasNext());
        it.next();
        assertThrows(UnsupportedOperationException.class, it::remove);

        // subList 迭代器的 remove 也不支持
        List<Integer> sub = list.subList(1, 3);
        Iterator<Integer> it2 = sub.iterator();
        assertTrue(it2.hasNext());
        it2.next();
        assertThrows(UnsupportedOperationException.class, it2::remove);
    }
}
