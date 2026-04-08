package master.flame.danmaku.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.IDanmakus;
import master.flame.danmaku.danmaku.model.android.DanmakuContext;
import master.flame.danmaku.danmaku.model.android.Danmakus;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;

public class MappedDanmakuParser<T> extends BaseDanmakuParser {

    private final List<T> items;
    private final DanmakuItemMapper<T> itemMapper;

    public MappedDanmakuParser(List<T> items, DanmakuItemMapper<T> itemMapper) {
        if (itemMapper == null) {
            throw new IllegalArgumentException("itemMapper == null");
        }
        this.items = items == null ? Collections.<T>emptyList() : new ArrayList<T>(items);
        this.itemMapper = itemMapper;
    }

    @Override
    protected IDanmakus parse() {
        DanmakuContext danmakuContext = mContext;
        Danmakus danmakus = new Danmakus(
                Danmakus.ST_BY_TIME,
                danmakuContext.isDuplicateMergingEnabled(),
                danmakuContext.getBaseComparator()
        );
        for (T item : items) {
            BaseDanmaku danmaku = itemMapper.map(item, danmakuContext);
            if (danmaku == null) {
                continue;
            }
            danmaku.flags = danmakuContext.mGlobalFlagValues;
            danmaku.setTimer(mTimer);
            danmakus.addItem(danmaku);
            if (mListener != null) {
                mListener.onDanmakuAdd(danmaku);
            }
        }
        return danmakus;
    }
}
