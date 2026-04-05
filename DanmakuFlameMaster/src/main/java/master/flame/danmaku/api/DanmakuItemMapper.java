package master.flame.danmaku.api;

import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.android.DanmakuContext;

public interface DanmakuItemMapper<T> {

    BaseDanmaku map(T item, DanmakuContext danmakuContext);
}
