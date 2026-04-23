package Delta.Snake;

import Delta.Map.SegmentStorage;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static Delta.Map.SegmentStorage.pack;
import static Delta.Map.SegmentStorage.unpack;

public final class Snake {
    private final ArrayDeque<Long> body = new ArrayDeque<>();
    private final Set<Long> occupied = new HashSet<>();

    public Snake(int headX, int headY, int initialLength, Direction initialDirection) {
        if (initialLength < 1) {
            throw new IllegalArgumentException("stupid u forgot the snake | initialLength must be at least 1");
        }


        for (int i = 0; i < initialLength; i++) {
            int x = headX;
            int y = headY;

            switch (initialDirection) {
                case RIGHT -> x = headX - i;
                case LEFT -> x = headX + i;
                case UP -> y = headY + i;
                case DOWN -> y = headY - i;
            }

            long key = pack(x, y);
            body.addLast(key);
            occupied.add(key);
        }
    }

    public SegmentStorage.Coord head() {
        return unpack(body.peekFirst());
    }

    public SegmentStorage.Coord tail() {
        return unpack(body.peekLast());
    }

    public boolean occupies(int x, int y) {
        return occupied.contains(pack(x, y));
    }

    public int length() {
        return body.size();
    }

    public List<SegmentStorage.Coord> segments() {
        List<SegmentStorage.Coord> result = new ArrayList<>(body.size());
        for (long key : body) {
            result.add(unpack(key));
        }
        return result;
    }

    public SegmentStorage.Coord nextHead(Direction dir) {
        SegmentStorage.Coord h = head();
        int nx = h.x();
        int ny = h.y();

        switch (dir) {
            case UP -> ny--;
            case DOWN -> ny++;
            case LEFT -> nx--;
            case RIGHT -> nx++;
        }

        return new SegmentStorage.Coord(nx, ny);
    }

    public boolean wouldHitItself(int x, int y, boolean grow) {
        long newHead = pack(x, y);
        long tail = body.peekLast();

        return occupied.contains(newHead) && (grow || newHead != tail);
    }

    public MoveResult move(Direction dir, boolean grow) {
        SegmentStorage.Coord next = nextHead(dir);
        long newHead = pack(next.x(), next.y());

        long oldHead = body.peekFirst();

        body.addFirst(newHead);
        occupied.add(newHead);

        Long removedTail = null;
        if (!grow) {
            removedTail = body.removeLast();
            occupied.remove(removedTail);
        }

        return new MoveResult(
                unpack(oldHead),
                unpack(newHead),
                removedTail == null ? null : unpack(removedTail)
        );
    }

    public enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    public record MoveResult(
            SegmentStorage.Coord oldHead,
            SegmentStorage.Coord newHead,
            SegmentStorage.Coord removedTail
    ) {}
}