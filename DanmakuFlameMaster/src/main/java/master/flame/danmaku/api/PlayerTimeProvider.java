package master.flame.danmaku.api;

public interface PlayerTimeProvider {

    long getCurrentTimeMs();

    boolean isPlaying();

    default long getSyncThresholdTimeMs() {
        return 1500L;
    }
}
