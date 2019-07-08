package xyz.rpka.Weather;

class Currently {
    private float time;
    private String summary;
    private String icon;
    private float nearestStormDistance;
    private float nearestStormBearing;
    private float precipIntensity;
    private float precipProbability;
    private float temperature;
    private float apparentTemperature;
    private float dewPoint;
    private float humidity;
    private float pressure;
    private float windSpeed;
    private float windGust;
    private float windBearing;
    private float cloudCover;
    private float uvIndex;
    private float visibility;
    private float ozone;

    public float getTime() {
        return time;
    }

    public String getSummary() {
        return summary;
    }

    public String getIcon() {
        return icon;
    }

    public float getNearestStormDistance() {
        return nearestStormDistance;
    }

    public float getNearestStormBearing() {
        return nearestStormBearing;
    }

    public float getPrecipIntensity() {
        return precipIntensity;
    }

    public float getPrecipProbability() {
        return precipProbability;
    }

    public float getTemperature() {
        return temperature;
    }

    public float getApparentTemperature() {
        return apparentTemperature;
    }

    public float getDewPoint() {
        return dewPoint;
    }

    public float getHumidity() {
        return humidity;
    }

    public float getPressure() {
        return pressure;
    }

    public float getWindSpeed() {
        return windSpeed;
    }

    public float getWindGust() {
        return windGust;
    }

    public float getWindBearing() {
        return windBearing;
    }

    public float getCloudCover() {
        return cloudCover;
    }

    public float getUvIndex() {
        return uvIndex;
    }

    public float getVisibility() {
        return visibility;
    }

    public float getOzone() {
        return ozone;
    }
}
