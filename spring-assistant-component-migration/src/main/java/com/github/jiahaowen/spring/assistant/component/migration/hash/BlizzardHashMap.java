package com.github.jiahaowen.spring.assistant.component.migration.hash;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

/**
 * 暴雪哈希实现
 *
 * @author jiahaowen.jhw
 * @version $Id: BlizzardHashMap.java, v 0.1 2017-03-08 下午2:19 jiahaowen.jhw Exp $
 */
public class BlizzardHashMap<K, V> {

    static final int MAXIMUM_CAPACITY = 1 << 30;

    // the default initial capacity
    private final int DEFAULT_INITIAL_CAPACITY = 16;

    // the default load factor
    private final float DEFAULT_LOAD_FACTOR = 0.75f;

    // load factor for the hash table.
    private final float loadFactor;

    // hash table data
    private Entry<K, V>[] table;

    // total entries in the hash table
    private int size;

    // a randomized value applied to MPQ hash
    // value falls between 0 to 3
    private float hashSeed;

    // reference table for hashing
    private int cryptTable[];

    // The next size value at which to resize (capacity * load factor).
    private int threshold;

    /** constructs an empty HashMap with default initial capacity and load factor */
    public BlizzardHashMap() {
        this.loadFactor = DEFAULT_LOAD_FACTOR;
        this.threshold = DEFAULT_INITIAL_CAPACITY;
        initTable();
    }

    /**
     * return number of keys in hash table
     *
     * @return number of keys
     */
    public int size() {
        return size;
    }

    /**
     * Tests if this hashtable maps no keys to values.
     *
     * @return hash table is empty or not
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Returns the value to which the specified key is mapped, or null if this map contains no
     * mapping for the key.
     *
     * @param key key string of the key-value pair
     */
    public V get(String key) {
        if (key == null) {
            throw new NullPointerException();
        }
        Entry<K, V> entry = getEntry(key);
        return null == entry ? null : entry.getValue();
    }

    /**
     * Associates the specified value with the specified key in this map. If the map previously
     * contained a mapping for the key, the old value is replaced.
     *
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with key, or null if there was no mapping for key.
     */
    public V put(String key, V value) {
        if (key == null || value == null) {
            throw new NullPointerException();
        }
        int hash = hash(key);
        int i = indexFor(hash, table.length);
        for (Entry<K, V> e = table[i]; e != null; e = e.next) {
            String k = e.key;
            if (e.hash == hash && (k == key || key.equals(k))) {
                V oldValue = e.value;
                e.value = value;
                return oldValue;
            }
        }
        addEntry(hash, key, value, i);
        return null;
    }

    /**
     * Removes the mapping for the specified key from this map if present.
     *
     * @param key key whose mapping is to be removed from the map
     * @return the previous value associated with key or null if there was no mapping for key.
     */
    public V remove(String key) {
        if (key == null) {
            throw new NullPointerException();
        }
        Entry<K, V> e = removeEntryForKey(key);
        return (e == null ? null : e.value);
    }

    /** Removes all of the mappings from this map. The map will be empty after this call returns. */
    public void clear() {
        Arrays.fill(table, null);
        size = 0;
    }

    /**
     * Removes and returns the entry associated with the specified key in the HashMap. Returns null
     * if the HashMap contains no mapping for this key.
     */
    private Entry<K, V> removeEntryForKey(String key) {
        if (size == 0) {
            return null;
        }
        int hash = hash(key);
        int i = indexFor(hash, table.length);
        Entry<K, V> prev = table[i];
        Entry<K, V> e = prev;

        while (e != null) {
            Entry<K, V> next = e.next;
            String k = e.key;
            if (e.hash == hash && (k == key || key.equals(k))) {
                size--;
                if (prev == e) {
                    table[i] = next;
                } else {
                    prev.next = next;
                }
                return e;
            }
            prev = e;
            e = next;
        }

        return e;
    }

    /**
     * Returns the entry associated with the specified key in the HashMap. Returns null if the
     * HashMap contains no mapping for the key.
     */
    private Entry<K, V> getEntry(String key) {
        if (size == 0) {
            return null;
        }

        int hash = hash(key);
        for (Entry<K, V> e = table[indexFor(hash, table.length)]; e != null; e = e.next) {
            String k = e.key;
            if (e.hash == hash && (k == key || key.equals(k))) return e;
        }
        return null;
    }

    /**
     * Adds a new entry with the specified key, value and hash code to the specified bucket.
     *
     * @param hash hash code of the key
     * @param key key string of the key-value pair
     * @param value value object of the key-value pair
     * @param bucketIndex index of table array
     */
    private void addEntry(int hash, String key, V value, int bucketIndex) {
        if ((size >= threshold) && (null != table[bucketIndex])) {
            resize(2 * table.length);
            bucketIndex = indexFor(hash, table.length);
        }

        Entry<K, V> e = table[bucketIndex];
        table[bucketIndex] = new Entry<K, V>(hash, key, value, e);
        size++;
    }

    /**
     * Rehashes the contents of this map into a new array with a larger capacity. This method is
     * called automatically when the number of keys in this map reaches its threshold.
     *
     * <p>If current capacity is MAXIMUM_CAPACITY, this method does not resize the map, but sets
     * threshold to Integer.MAX_VALUE. This has the effect of preventing future calls.
     *
     * @param newCapacity the new table's capacity
     */
    private void resize(int newCapacity) {
        Entry[] oldTable = table;
        int oldCapacity = oldTable.length;
        if (oldCapacity == MAXIMUM_CAPACITY) {
            threshold = Integer.MAX_VALUE;
            return;
        }

        Entry[] newTable = new Entry[newCapacity];
        transfer(newTable);
        table = newTable;
        threshold = (int) Math.min(newCapacity * loadFactor, MAXIMUM_CAPACITY + 1);
    }

    /** Transfers all entries from current table to newTable. */
    private void transfer(Entry[] newTable) {
        int newCapacity = newTable.length;
        for (Entry<K, V> e : table) {
            while (null != e) {
                Entry<K, V> next = e.next;
                int i = indexFor(e.hash, newCapacity);
                e.next = newTable[i];
                newTable[i] = e;
                e = next;
            }
        }
    }

    /**
     * Returns index for hash code h.
     *
     * @param hash hash code of the key
     * @param length of table array
     */
    private int indexFor(int hash, int length) {
        return hash & (length - 1);
    }

    /** constructs an empty HashMap and initialize hash seed and cryptTable */
    private void initTable() {
        int capacity = DEFAULT_INITIAL_CAPACITY;
        this.table = new Entry[capacity];
        this.hashSeed = getRandom();
        initCryptTable();
    }

    /**
     * generates a random value falls between from 0f to 3f
     *
     * @return hash seed
     */
    private float getRandom() {
        Random ran = new Random();
        return ran.nextFloat() * 3.f;
    }

    /**
     * Uses MPQ hash algorithm to generate a long type variable as hashed value. This version only
     * accepts String.
     *
     * @param key receive a string as key in hash map.
     * @return returns a long type argument as hashed value
     */
    private int hash(String key) {
        int seed1 = 0x7FED7FED, seed2 = 0xEEEEEEEE;
        for (int i = 0; i < key.length(); i++) {
            int ch = key.charAt(i);
            seed1 = cryptTable[(int) (hashSeed * (1 << 8)) + ch] ^ (seed1 + seed2);
            seed2 = ch + seed1 + seed2 + (seed2 << 5) + 3;
        }
        return seed1;
    }

    /** constructs a encrypt table with 1280(0x500) elements. */
    private void initCryptTable() {
        this.cryptTable = new int[0x500];
        int seed = 0x00100001;
        int index1 = 0, index2 = 0, i;
        for (index1 = 0; index1 < 0x100; index1++) {
            for (index2 = index1, i = 0; i < 5; i++, index2 += 0x100) {
                int temp1, temp2;
                seed = (seed * 125 + 3) % 0x2AAAAB;
                temp1 = (seed & 0xFFFF) << 0x10;
                seed = (seed * 125 + 3) % 0x2AAAAB;
                temp2 = (seed & 0xFFFF);
                cryptTable[index2] = (temp1 | temp2);
            }
        }
    }

    static class Entry<K, V> {
        final String key;
        V value;
        Entry<K, V> next;
        int hash;

        /** Creates new entry. */
        Entry(int h, String k, V v, Entry<K, V> n) {
            value = v;
            next = n;
            key = k;
            hash = h;
        }

        public String getKey() {
            return key;
        }

        public final V getValue() {
            return value;
        }

        public final V setValue(V newValue) {
            V oldValue = value;
            value = newValue;
            return oldValue;
        }

        @Override
        public final boolean equals(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry e = (Map.Entry) o;
            Object k1 = getKey();
            Object k2 = e.getKey();
            if (k1 == k2 || (k1 != null && k1.equals(k2))) {
                Object v1 = getValue();
                Object v2 = e.getValue();
                if (v1 == v2 || (v1 != null && v1.equals(v2))) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public final int hashCode() {
            return Objects.hashCode(getKey()) ^ Objects.hashCode(getValue());
        }

        @Override
        public final String toString() {
            return getKey() + "=" + getValue();
        }
    }
}
