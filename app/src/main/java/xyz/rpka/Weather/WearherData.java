package xyz.rpka.Weather;

class WearherData {
    private float latitude;
    private float longitude;
    private String timezone;
    Currently currently;
    Minutely minutely;
    Hourly hourly;
    Daily daily;
    Flags flags;
    private float offset;

    public float getLatitude() {
        return latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public String getTimezone() {
        return timezone;
    }

    public Currently getCurrently() {
        return currently;
    }

    public Minutely getMinutely() {
        return minutely;
    }

    public Hourly getHourly() {
        return hourly;
    }

    public Daily getDaily() {
        return daily;
    }

    public Flags getFlags() {
        return flags;
    }

    public float getOffset() {
        return offset;
    }
}
