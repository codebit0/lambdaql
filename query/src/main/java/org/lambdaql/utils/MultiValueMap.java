package org.lambdaql.utils;

import java.util.*;
import java.util.function.BiConsumer;

import java.util.*;
import java.util.function.BiConsumer;

public class MultiValueMap<K, V> implements Map<K, List<V>> {

    private final Map<K, List<V>> delegate = new HashMap<>();

    // ✅ 단일 값 추가 전용
    public void add(K key, V value) {
        delegate.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
    }

    public void addAll(K key, Collection<? extends V> values) {
        delegate.computeIfAbsent(key, k -> new ArrayList<>()).addAll(values);
    }

    public V getFirst(K key) {
        List<V> values = delegate.get(key);
        return (values != null && !values.isEmpty()) ? values.get(0) : null;
    }

    public void forEachFlat(BiConsumer<? super K, ? super V> action) {
        for (Entry<K, List<V>> entry : delegate.entrySet()) {
            for (V value : entry.getValue()) {
                action.accept(entry.getKey(), value);
            }
        }
    }

    // ✅ Map 인터페이스 구현

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return delegate.containsKey(key);
    }

    // ✅ 개별 value 기준 검색
    @Override
    public boolean containsValue(Object value) {
        for (List<V> list : delegate.values()) {
            if (list.contains(value)) return true;
        }
        return false;
    }

    @Override
    public List<V> get(Object key) {
        return delegate.get(key);
    }

    @Override
    public List<V> put(K key, List<V> value) {
        return delegate.put(key, value);
    }

    @Override
    public List<V> remove(Object key) {
        return delegate.remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends List<V>> m) {
        delegate.putAll(m);
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public Set<K> keySet() {
        return delegate.keySet();
    }

    @Override
    public Collection<List<V>> values() {
        return delegate.values();
    }

    @Override
    public Set<Entry<K, List<V>>> entrySet() {
        return delegate.entrySet();
    }

    @Override
    public String toString() {
        return delegate.toString();
    }
}
