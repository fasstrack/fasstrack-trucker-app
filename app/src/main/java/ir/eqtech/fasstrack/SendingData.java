package ir.eqtech.fasstrack;

public class SendingData {
    private String truck_id;
    private String truck_ETA;
    private String truck_Distance;

    public SendingData(String truck_id, String truck_ETA, String truck_Distance) {
        this.truck_id = truck_id;
        this.truck_ETA = truck_ETA;
        this.truck_Distance = truck_Distance;
    }

    public String getTruck_id() {
        return truck_id;
    }

    public void setTruck_id(String truck_id) {
        this.truck_id = truck_id;
    }

    public String getTruck_ETA() {
        return truck_ETA;
    }

    public void setTruck_ETA(String truck_ETA) {
        this.truck_ETA = truck_ETA;
    }

    public String getTruck_Distance() {
        return truck_Distance;
    }

    public void setTruck_Distance(String truck_Distance) {
        this.truck_Distance = truck_Distance;
    }
}
