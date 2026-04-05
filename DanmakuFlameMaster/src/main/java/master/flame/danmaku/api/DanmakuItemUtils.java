package master.flame.danmaku.api;

import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.android.DanmakuContext;
import master.flame.danmaku.danmaku.util.DanmakuUtils;

public final class DanmakuItemUtils {

    private DanmakuItemUtils() {
    }

    public static BaseDanmaku createDanmaku(
            DanmakuContext danmakuContext,
            int type,
            long timeMs
    ) {
        if (danmakuContext == null) {
            throw new IllegalArgumentException("danmakuContext == null");
        }
        BaseDanmaku danmaku = danmakuContext.mDanmakuFactory.createDanmaku(type, danmakuContext);
        if (danmaku != null) {
            danmaku.setTime(Math.max(0L, timeMs));
        }
        return danmaku;
    }

    public static BaseDanmaku createTextDanmaku(
            DanmakuContext danmakuContext,
            int type,
            long timeMs,
            CharSequence text
    ) {
        BaseDanmaku danmaku = createDanmaku(danmakuContext, type, timeMs);
        fillText(danmaku, text);
        return danmaku;
    }

    public static void fillText(BaseDanmaku danmaku, CharSequence text) {
        if (danmaku == null) {
            return;
        }
        DanmakuUtils.fillText(danmaku, text);
    }
}
