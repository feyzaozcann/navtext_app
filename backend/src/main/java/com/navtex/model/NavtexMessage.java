package com.navtex.model;

public class NavtexMessage {
    private String id;
    private String station;
    private String country;
    private String flag;
    private String type;
    private String typeLabel;
    private String raw;
    private String title;
    private String publishedAt;
    private String sourceUrl;

    public NavtexMessage() {}

    public NavtexMessage(String id, String station, String country, String flag,
                         String type, String typeLabel, String raw,
                         String title, String publishedAt, String sourceUrl) {
        this.id = id;
        this.station = station;
        this.country = country;
        this.flag = flag;
        this.type = type;
        this.typeLabel = typeLabel;
        this.raw = raw;
        this.title = title;
        this.publishedAt = publishedAt;
        this.sourceUrl = sourceUrl;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getStation() { return station; }
    public void setStation(String station) { this.station = station; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getFlag() { return flag; }
    public void setFlag(String flag) { this.flag = flag; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTypeLabel() { return typeLabel; }
    public void setTypeLabel(String typeLabel) { this.typeLabel = typeLabel; }

    public String getRaw() { return raw; }
    public void setRaw(String raw) { this.raw = raw; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getPublishedAt() { return publishedAt; }
    public void setPublishedAt(String publishedAt) { this.publishedAt = publishedAt; }

    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }
}
