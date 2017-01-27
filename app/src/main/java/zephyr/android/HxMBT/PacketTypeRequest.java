package zephyr.android.HxMBT;

public class PacketTypeRequest {
    public boolean ACCELEROMETER_ENABLE;
    public boolean BREATHING_ENABLE;
    public boolean ECG_ENABLE;
    public boolean EVENT_ENABLE;
    public boolean GP_ENABLE;
    public boolean RtoR_ENABLE;
    public boolean SUMMARY_ENABLE;

    public void EnableGP(boolean input) {
        this.GP_ENABLE = input;
    }

    public void EnableECG(boolean input) {
        this.ECG_ENABLE = input;
    }

    public void EnableBreathing(boolean input) {
        this.BREATHING_ENABLE = input;
    }

    public void EnableTtoR(boolean input) {
        this.RtoR_ENABLE = input;
    }

    public void EnableAccelerometry(boolean input) {
        this.ACCELEROMETER_ENABLE = input;
    }

    public void EnableSummary(boolean input) {
        this.SUMMARY_ENABLE = input;
    }

    public void EnableEvent(boolean input) {
        this.EVENT_ENABLE = input;
    }

    public PacketTypeRequest() {
        this.GP_ENABLE = false;
        this.ECG_ENABLE = false;
        this.BREATHING_ENABLE = false;
        this.RtoR_ENABLE = true;
        this.ACCELEROMETER_ENABLE = false;
        this.SUMMARY_ENABLE = false;
        this.EVENT_ENABLE = false;
    }
}
