package zephyr.android.HxMBT;

import java.util.EventObject;

public class ConnectedEvent<T> extends EventObject {
    public ConnectedEvent(T source)
    {
        super(source);
    }

    public T getSource()
    {
        return (T) this.source;
    }
}
