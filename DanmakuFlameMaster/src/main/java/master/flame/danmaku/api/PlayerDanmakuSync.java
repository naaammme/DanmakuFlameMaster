package master.flame.danmaku.api;

import master.flame.danmaku.danmaku.model.AbsDanmakuSync;

public class PlayerDanmakuSync extends AbsDanmakuSync {

    private final PlayerTimeProvider playerTimeProvider;

    public PlayerDanmakuSync(PlayerTimeProvider playerTimeProvider) {
        if (playerTimeProvider == null) {
            throw new IllegalArgumentException("playerTimeProvider == null");
        }
        this.playerTimeProvider = playerTimeProvider;
    }

    @Override
    public long getUptimeMillis() {
        return Math.max(0L, playerTimeProvider.getCurrentTimeMs());
    }

    @Override
    public int getSyncState() {
        return playerTimeProvider.isPlaying() ? SYNC_STATE_PLAYING : SYNC_STATE_HALT;
    }

    @Override
    public long getThresholdTimeMills() {
        return Math.max(1000L, playerTimeProvider.getSyncThresholdTimeMs());
    }

    @Override
    public boolean isSyncPlayingState() {
        return true;
    }
}
