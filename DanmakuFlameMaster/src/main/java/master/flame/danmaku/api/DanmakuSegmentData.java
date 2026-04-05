package master.flame.danmaku.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class DanmakuSegmentData<T> {

    private final long segmentIndex;
    private final List<T> items;

    public DanmakuSegmentData(long segmentIndex, List<T> items) {
        if (segmentIndex < 1L) {
            throw new IllegalArgumentException("segmentIndex must be >= 1");
        }
        this.segmentIndex = segmentIndex;
        this.items = Collections.unmodifiableList(
                items == null ? new ArrayList<T>() : new ArrayList<T>(items)
        );
    }

    public long getSegmentIndex() {
        return segmentIndex;
    }

    public List<T> getItems() {
        return items;
    }
}
