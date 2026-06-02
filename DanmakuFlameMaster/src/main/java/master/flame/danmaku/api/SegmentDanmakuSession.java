package master.flame.danmaku.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import master.flame.danmaku.controller.DrawHandler;
import master.flame.danmaku.controller.IDanmakuView;
import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.DanmakuTimer;
import master.flame.danmaku.danmaku.model.android.DanmakuContext;

public class SegmentDanmakuSession<T> {

    private final IDanmakuView danmakuView;
    private final DanmakuContext danmakuContext;
    private final DanmakuItemMapper<T> itemMapper;
    private final TreeMap<Long, DanmakuSegmentData<T>> segmentStore = new TreeMap<Long, DanmakuSegmentData<T>>();
    private final DrawHandler.Callback internalCallback = new DrawHandler.Callback() {
        @Override
        public void prepared() {
            boolean shouldReload;
            synchronized (SegmentDanmakuSession.this) {
                preparing = false;
                prepared = true;
                shouldReload = reloadOnPrepared;
                reloadOnPrepared = false;
            }
            danmakuView.start(resolveStartPositionMs());
            if (shouldReload) {
                reloadActiveDanmakus();
            }
            if (externalCallback != null) {
                externalCallback.prepared();
            }
        }

        @Override
        public void updateTimer(DanmakuTimer timer) {
            if (externalCallback != null) {
                externalCallback.updateTimer(timer);
            }
        }

        @Override
        public void danmakuShown(BaseDanmaku danmaku) {
            if (externalCallback != null) {
                externalCallback.danmakuShown(danmaku);
            }
        }

        @Override
        public void drawingFinished() {
            if (externalCallback != null) {
                externalCallback.drawingFinished();
            }
        }
    };

    private DrawHandler.Callback externalCallback;
    private PlayerTimeProvider playerTimeProvider;
    private boolean prepared;
    private boolean preparing;
    private boolean reloadOnPrepared;
    private boolean hasPendingSeekPosition;
    private long pendingSeekPositionMs;

    public SegmentDanmakuSession(
            IDanmakuView danmakuView,
            DanmakuContext danmakuContext,
            DanmakuItemMapper<T> itemMapper
    ) {
        this(danmakuView, danmakuContext, itemMapper, null);
    }

    public SegmentDanmakuSession(
            IDanmakuView danmakuView,
            DanmakuContext danmakuContext,
            DanmakuItemMapper<T> itemMapper,
            PlayerTimeProvider playerTimeProvider
    ) {
        if (danmakuView == null) {
            throw new IllegalArgumentException("danmakuView == null");
        }
        if (danmakuContext == null) {
            throw new IllegalArgumentException("danmakuContext == null");
        }
        if (itemMapper == null) {
            throw new IllegalArgumentException("itemMapper == null");
        }
        this.danmakuView = danmakuView;
        this.danmakuContext = danmakuContext;
        this.itemMapper = itemMapper;
        setPlayerTimeProvider(playerTimeProvider);
    }

    public synchronized void setCallback(DrawHandler.Callback callback) {
        this.externalCallback = callback;
    }

    public synchronized void setPlayerTimeProvider(PlayerTimeProvider playerTimeProvider) {
        this.playerTimeProvider = playerTimeProvider;
        danmakuContext.setDanmakuSync(
                playerTimeProvider == null ? null : new PlayerDanmakuSync(playerTimeProvider)
        );
    }

    public synchronized void prepare() {
        if (prepared || preparing) {
            return;
        }
        preparing = true;
        reloadOnPrepared = false;
        danmakuView.setCallback(internalCallback);
        danmakuView.prepare(new MappedDanmakuParser<T>(flattenItems(), itemMapper), danmakuContext);
    }

    public synchronized void appendSegment(DanmakuSegmentData<T> segmentData) {
        if (segmentData == null) {
            return;
        }
        boolean existed = segmentStore.containsKey(segmentData.getSegmentIndex());
        segmentStore.put(segmentData.getSegmentIndex(), copySegment(segmentData));
        if (!prepared) {
            if (preparing) {
                reloadOnPrepared = true;
            }
            return;
        }
        if (existed) {
            reloadActiveDanmakus();
            return;
        }
        addItems(segmentData.getItems());
        danmakuView.forceRender();
    }

    public synchronized void replaceSegments(Collection<DanmakuSegmentData<T>> segments) {
        segmentStore.clear();
        if (segments != null) {
            for (DanmakuSegmentData<T> segment : segments) {
                if (segment == null) {
                    continue;
                }
                segmentStore.put(segment.getSegmentIndex(), copySegment(segment));
            }
        }
        if (!prepared) {
            if (preparing) {
                reloadOnPrepared = true;
            }
            return;
        }
        reloadActiveDanmakus();
    }

    public synchronized void clearSegments() {
        segmentStore.clear();
        if (!prepared) {
            if (preparing) {
                reloadOnPrepared = true;
            }
            return;
        }
        danmakuView.removeAllDanmakus(true);
        danmakuView.clearDanmakusOnScreen();
        danmakuView.forceRender();
    }

    public synchronized void seekTo(long positionMs) {
        pendingSeekPositionMs = Math.max(0L, positionMs);
        hasPendingSeekPosition = true;
        if (!prepared) {
            return;
        }
        danmakuView.seekTo(pendingSeekPositionMs);
        hasPendingSeekPosition = false;
    }

    public synchronized void pause() {
        danmakuView.pause();
    }

    public synchronized void resume() {
        if (!prepared) {
            prepare();
            return;
        }
        danmakuView.resume();
    }

    public synchronized void show() {
        danmakuView.show();
    }

    public synchronized void hide() {
        danmakuView.hide();
    }

    public synchronized void release() {
        prepared = false;
        preparing = false;
        reloadOnPrepared = false;
        danmakuView.release();
    }

    public synchronized boolean isPrepared() {
        return prepared;
    }

    public synchronized Set<Long> getSegmentIndices() {
        return Collections.unmodifiableSet(new LinkedHashSet<Long>(segmentStore.keySet()));
    }

    private synchronized void reloadActiveDanmakus() {
        danmakuView.removeAllDanmakus(true);
        danmakuView.clearDanmakusOnScreen();
        addItems(flattenItems());
        danmakuView.forceRender();
    }

    private void addItems(List<T> items) {
        for (T item : items) {
            BaseDanmaku danmaku = itemMapper.map(item, danmakuContext);
            if (danmaku != null) {
                danmakuView.addDanmaku(danmaku);
            }
        }
    }

    private List<T> flattenItems() {
        List<T> items = new ArrayList<T>();
        for (DanmakuSegmentData<T> segmentData : segmentStore.values()) {
            items.addAll(segmentData.getItems());
        }
        return items;
    }

    private DanmakuSegmentData<T> copySegment(DanmakuSegmentData<T> segmentData) {
        return new DanmakuSegmentData<T>(segmentData.getSegmentIndex(), segmentData.getItems());
    }

    private long resolveStartPositionMs() {
        long startPositionMs;
        synchronized (this) {
            if (hasPendingSeekPosition) {
                startPositionMs = pendingSeekPositionMs;
                hasPendingSeekPosition = false;
                return startPositionMs;
            }
            startPositionMs = playerTimeProvider == null ? 0L : playerTimeProvider.getCurrentTimeMs();
        }
        return Math.max(0L, startPositionMs);
    }
}
