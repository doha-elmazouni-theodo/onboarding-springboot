package com.theodo.springblueprint.common.utils.collections;

import java.util.Collection;
import java.util.function.Function;
import org.eclipse.collections.api.factory.Bags;
import org.eclipse.collections.api.factory.BiMaps;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.factory.SortedBags;
import org.eclipse.collections.api.factory.SortedMaps;
import org.eclipse.collections.api.factory.SortedSets;
import org.eclipse.collections.api.factory.bag.MutableBagFactory;
import org.eclipse.collections.api.factory.bag.sorted.MutableSortedBagFactory;
import org.eclipse.collections.api.factory.bimap.MutableBiMapFactory;
import org.eclipse.collections.api.factory.list.MutableListFactory;
import org.eclipse.collections.api.factory.map.MutableMapFactory;
import org.eclipse.collections.api.factory.map.sorted.MutableSortedMapFactory;
import org.eclipse.collections.api.factory.set.MutableSetFactory;
import org.eclipse.collections.api.factory.set.sorted.MutableSortedSetFactory;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.MutableSet;

@SuppressWarnings({ "PMD.UseCollectionUtils", "unused" })
public interface Mutable {
    MutableBagFactory bag = Bags.mutable;
    MutableBiMapFactory biMap = BiMaps.mutable;
    MutableListFactory list = Lists.mutable;
    MutableMapFactory map = Maps.mutable;
    MutableSetFactory set = Sets.mutable;
    MutableSortedBagFactory sortedBag = SortedBags.mutable;
    MutableSortedMapFactory sortedMap = SortedMaps.mutable;
    MutableSortedSetFactory sortedSet = SortedSets.mutable;

    static <TDest, TSource> MutableList<TDest> collectList(
        Iterable<TSource> iterable,
        Function<TSource, TDest> mapper) {
        MutableList<TDest> objects = list.ofInitialCapacity(getSizeOrZero(iterable));
        for (TSource v : iterable) {
            objects.add(mapper.apply(v));
        }
        return objects;
    }

    static <TDest, TSource> MutableList<TDest> collectList(TSource[] array, Function<TSource, TDest> mapper) {
        MutableList<TDest> objects = list.ofInitialCapacity(array.length);
        for (TSource v : array) {
            objects.add(mapper.apply(v));
        }
        return objects;
    }

    static <TDest, TSource> MutableSet<TDest> collectSet(Iterable<TSource> iterable, Function<TSource, TDest> mapper) {
        MutableSet<TDest> objects = set.ofInitialCapacity(getSizeOrZero(iterable));
        for (TSource v : iterable) {
            objects.add(mapper.apply(v));
        }
        return objects;
    }

    static <TDest, TSource> MutableSet<TDest> collectSet(TSource[] array, Function<TSource, TDest> mapper) {
        MutableSet<TDest> objects = set.ofInitialCapacity(array.length);
        for (TSource v : array) {
            objects.add(mapper.apply(v));
        }
        return objects;
    }

    private static int getSizeOrZero(Iterable<?> iterable) {
        if (iterable instanceof Collection<?> collection) {
            return collection.size();
        }
        return 0;
    }
}
