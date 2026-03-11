package com.theodo.springblueprint.common.utils.collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.withinPercentage;

import com.theodo.springblueprint.testhelpers.annotations.UnitTest;
import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.set.ImmutableSet;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

@UnitTest
class ImmutableUnitTests {

    @Test
    void collectList_on_Iterable_returns_ImmutableList_of_mapped_elements() {
        List<String> input = List.of("a", "b", "c");
        Function<String, Integer> mapper = String::length;

        // Act
        ImmutableList<Integer> result = Immutable.collectList(input, mapper);

        assertThat(result).containsExactly(1, 1, 1);
    }

    @Test
    void collectList_on_Array_returns_ImmutableList_of_mapped_elements() {
        String[] input = { "x", "yy", "z", "x" };
        Function<String, Integer> mapper = String::length;

        // Act
        ImmutableList<Integer> result = Immutable.collectList(input, mapper);

        assertThat(result).containsExactly(1, 2, 1, 1);
    }

    @Test
    void collectSet_on_Iterable_returns_ImmutableSet_of_mapped_unique_elements() {
        List<String> input = List.of("a", "bb", "c", "a");
        Function<String, Integer> mapper = String::length;

        // Act
        ImmutableSet<Integer> result = Immutable.collectSet(input, mapper);

        assertThat(result).containsExactlyInAnyOrder(1, 2);
    }

    @Test
    void collectSet_on_array_returns_ImmutableSet_of_mapped_unique_elements() {
        String[] input = { "x", "yy", "z", "x" };
        Function<String, Integer> mapper = String::length;

        // Act
        ImmutableSet<Integer> result = Immutable.collectSet(input, mapper);

        assertThat(result).containsExactlyInAnyOrder(1, 2);
    }

    @Test
    void collectList_on_empty_Iterable_returns_empty_ImmutableList() {
        List<String> input = List.of();
        Function<String, Integer> mapper = String::length;

        // Act
        ImmutableList<Integer> result = Immutable.collectList(input, mapper);

        assertThat(result).isEmpty();
    }

    @Test
    void collectSet_on_empty_Iterable_returns_empty_ImmutableSet() {
        List<String> input = List.of();
        Function<String, Integer> mapper = String::length;

        // Act
        ImmutableSet<Integer> result = Immutable.collectSet(input, mapper);

        assertThat(result).isEmpty();
    }

    @Test
    void collectSet_on_empty_Array_returns_empty_ImmutableSet() {
        String[] input = {};
        Function<String, Integer> mapper = String::length;

        // Act
        ImmutableSet<Integer> result = Immutable.collectSet(input, mapper);

        assertThat(result).isEmpty();
    }

    @Test
    void collectList_on_non_collection_Iterable_returns_ImmutableList_of_mapped_elements() {
        Iterable<String> input = () -> List.of("alpha", "beta", "gamma").iterator();
        Function<String, Integer> mapper = String::length;

        // Act
        ImmutableList<Integer> result = Immutable.collectList(input, mapper);

        assertThat(result).containsExactly(5, 4, 5);
    }

    @Test
    void collectList_on_Iterable_allocate_twice_the_memory_allocated_by_similar_array_instantiation() {
        final String value = "value";
        List<String> collection = Collections.nCopies(10_000, value);
        long baseline = getAllocatedBytesAfterWarmup(() -> getArray(collection.size()));

        // Act
        long allocatedMemory = getAllocatedBytesAfterWarmup(() -> Immutable.collectList(collection, s -> s));

        long expectedAllocatedMemory = baseline * 2;
        assertThat(allocatedMemory)
            .isCloseTo(expectedAllocatedMemory, withinPercentage(5)); // 5% error margin
    }

    @Test
    void collectList_on_Array_allocate_twice_the_memory_allocated_by_similar_array_instantiation() {
        Object[] collection = IntStream.rangeClosed(1, 10_000).boxed().toArray();
        long baseline = getAllocatedBytesAfterWarmup(() -> getArray(collection.length));

        // Act
        long allocatedMemory = getAllocatedBytesAfterWarmup(() -> Immutable.collectList(collection, s -> s));

        long expectedAllocatedMemory = baseline * 2;
        assertThat(allocatedMemory)
            .isCloseTo(expectedAllocatedMemory, withinPercentage(5)); // 5% error margin
    }

    @Test
    void collectSet_on_Iterable_allocate_less_than_four_times_the_memory_allocated_by_similar_array_instantiation() {
        double loadFactor = 0.75; // https://stackoverflow.com/a/10901821
        int powerOf2 = 128 * 128;
        int size = (int) (powerOf2 * loadFactor);

        List<Integer> collection = IntStream.rangeClosed(1, size).boxed().collect(Collectors.toList());
        long baseline = getAllocatedBytesAfterWarmup(() -> getArray(collection.size()));

        // Act
        double allocatedMemory = getAllocatedBytesAfterWarmup(() -> Immutable.collectSet(collection, s -> s));

        double expectedAllocatedMemory = (baseline / loadFactor) + baseline + (baseline / loadFactor);
        assertThat(allocatedMemory)
            .isCloseTo(expectedAllocatedMemory, withinPercentage(5));
    }

    @Test
    void collectSet_on_Array_allocate_less_than_four_times_the_memory_allocated_by_similar_array_instantiation() {
        double loadFactor = 0.75; // https://stackoverflow.com/a/10901821
        int powerOf2 = 128 * 128;
        int size = (int) (powerOf2 * loadFactor);

        Object[] collection = IntStream.rangeClosed(1, size).boxed().toArray();
        long baseline = getAllocatedBytesAfterWarmup(() -> getArray(collection.length));

        // Act
        double allocatedMemory = getAllocatedBytesAfterWarmup(() -> Immutable.collectSet(collection, s -> s));

        double expectedAllocatedMemory = (baseline / loadFactor) + baseline + (baseline / loadFactor);
        assertThat(allocatedMemory)
            .isCloseTo(expectedAllocatedMemory, withinPercentage(5));
    }

    @SuppressWarnings("nullness:new.array")
    private static Object @Nullable [] getArray(int size) {
        return new Object[size];
    }

    private static long getAllocatedBytesAfterWarmup(Supplier<?> method) {
        // Warming up
        method.get();

        com.sun.management.ThreadMXBean mxBean = (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();
        long currentThreadId = Thread.currentThread().threadId();

        long before = mxBean.getThreadAllocatedBytes(currentThreadId);
        method.get();
        return mxBean.getThreadAllocatedBytes(currentThreadId) - before;
    }
}
