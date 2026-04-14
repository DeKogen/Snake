package Delta.Map;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public final class SegmentStorage {
    public enum SegmentType {
        EMPTY, WALL, SNAKE_BODY, SNAKE_HEAD, APPLE
    }

    private final int width;
    private final int height;

    private final Map<Long, SegmentType> typeByCoord = new HashMap<>();
    private final EnumMap<SegmentType, ArrayList<Long>> coordsByType =
            new EnumMap<>(SegmentType.class);
    private final Map<Long, Integer> indexInBucket = new HashMap<>();

    public SegmentStorage(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("width and height must be > 0");
        }

        this.width = width;
        this.height = height;

        for (SegmentType type : SegmentType.values()) {
            coordsByType.put(type, new ArrayList<>());
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                long key = pack(x, y);
                typeByCoord.put(key, SegmentType.EMPTY);
                addToBucket(key, SegmentType.EMPTY);
            }
        }
    }

    public SegmentType get(int x, int y) {
        checkBounds(x, y);
        return typeByCoord.get(pack(x, y));
    }

    public void put(int x, int y, SegmentType newType) {
        checkBounds(x, y);

        long key = pack(x, y);
        SegmentType oldType = typeByCoord.get(key);

        if (oldType == newType) {
            return;
        }

        removeFromBucket(key, oldType);
        typeByCoord.put(key, newType);
        addToBucket(key, newType);
    }

    public Coord getRandomByType(SegmentType type) {
        ArrayList<Long> bucket = coordsByType.get(type);
        if (bucket.isEmpty()) {
            return null;
        }

        int idx = ThreadLocalRandom.current().nextInt(bucket.size());
        return unpack(bucket.get(idx));
    }

    public int countByType(SegmentType type) {
        return coordsByType.get(type).size();
    }

    private void addToBucket(long key, SegmentType type) {
        ArrayList<Long> bucket = coordsByType.get(type);
        indexInBucket.put(key, bucket.size());
        bucket.add(key);
    }

    private void removeFromBucket(long key, SegmentType type) {
        ArrayList<Long> bucket = coordsByType.get(type);
        int removeIndex = indexInBucket.remove(key);
        int lastIndex = bucket.size() - 1;

        if (removeIndex != lastIndex) {
            long lastKey = bucket.get(lastIndex);
            bucket.set(removeIndex, lastKey);
            indexInBucket.put(lastKey, removeIndex);
        }

        bucket.remove(lastIndex);
    }

    private void checkBounds(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            throw new IndexOutOfBoundsException("Out of bounds: (" + x + ", " + y + ")");
        }
    }

    public static long pack(int x, int y) {
        return (((long) x) << 32) | (y & 0xffffffffL);
    }

    public static Coord unpack(long key) {
        int x = (int) (key >> 32);
        int y = (int) key;
        return new Coord(x, y);
    }

    public record Coord(int x, int y) {}
}