package ir.eqtech.fasstrack;

public class ReceivingData {
    private String slotId;

    public ReceivingData(String truck_slot_number) {
        this.slotId = truck_slot_number;
    }

    public String getTruck_slot_number() {
        return slotId;
    }

    public void setTruck_slot_number(String truck_slot_number) {
        this.slotId = truck_slot_number;
    }
}
