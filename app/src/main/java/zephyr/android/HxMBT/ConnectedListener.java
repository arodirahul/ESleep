package zephyr.android.HxMBT;

public interface ConnectedListener<T> {
    void Connected(ConnectedEvent<T> connectedEvent);
}
