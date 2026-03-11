package com.theodo.springblueprint.common.utils.collections;

import java.util.function.Function;
import org.eclipse.collections.api.factory.Bags;
import org.eclipse.collections.api.factory.BiMaps;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.factory.SortedBags;
import org.eclipse.collections.api.factory.SortedMaps;
import org.eclipse.collections.api.factory.SortedSets;
import org.eclipse.collections.api.factory.bag.ImmutableBagFactory;
import org.eclipse.collections.api.factory.bag.sorted.ImmutableSortedBagFactory;
import org.eclipse.collections.api.factory.bimap.ImmutableBiMapFactory;
import org.eclipse.collections.api.factory.list.ImmutableListFactory;
import org.eclipse.collections.api.factory.map.ImmutableMapFactory;
import org.eclipse.collections.api.factory.map.sorted.ImmutableSortedMapFactory;
import org.eclipse.collections.api.factory.set.ImmutableSetFactory;
import org.eclipse.collections.api.factory.set.sorted.ImmutableSortedSetFactory;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.set.ImmutableSet;

@SuppressWarnings({ "PMD.UseCollectionUtils", "unused" })
public interface Immutable {
    ImmutableBagFactory bag = Bags.immutable;
    ImmutableBiMapFactory biMap = BiMaps.immutable;
    ImmutableListFactory list = Lists.immutable;
    ImmutableMapFactory map = Maps.immutable;
    ImmutableSetFactory set = Sets.immutable;
    ImmutableSortedBagFactory sortedBag = SortedBags.immutable;
    ImmutableSortedMapFactory sortedMap = SortedMaps.immutable;
    ImmutableSortedSetFactory sortedSet = SortedSets.immutable;

    static <TDest, TSource> ImmutableList<TDest> collectList(
        Iterable<TSource> iterable,
        Function<TSource, TDest> mapper) {
        return Mutable.collectList(iterable, mapper).toImmutable();
    }

    static <TDest, TSource> ImmutableList<TDest> collectList(TSource[] array, Function<TSource, TDest> mapper) {
        return Mutable.collectList(array, mapper).toImmutable();
    }

    static <TDest, TSource> ImmutableSet<TDest> collectSet(
        Iterable<TSource> iterable,
        Function<TSource, TDest> mapper) {
        return Mutable.collectSet(iterable, mapper).toImmutable();
    }

    static <TDest, TSource> ImmutableSet<TDest> collectSet(TSource[] array, Function<TSource, TDest> mapper) {
        return Mutable.collectSet(array, mapper).toImmutable();
    }
}
