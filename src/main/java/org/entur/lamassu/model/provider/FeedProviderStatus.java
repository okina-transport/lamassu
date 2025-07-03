package org.entur.lamassu.model.provider;

public class FeedProviderStatus {

  private String id;
  private DailyStatus dailyStatus;
  private String dataset;
  private String url;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public DailyStatus getDailyStatus() {
    return dailyStatus;
  }

  public void setDailyStatus(DailyStatus dailyStatus) {
    this.dailyStatus = dailyStatus;
  }

  public String getDataset() {
    return dataset;
  }

  public void setDataset(String dataset) {
    this.dataset = dataset;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public enum DailyStatus {
    UNKNOWN,
    GREEN,
    ORANGE,
    RED,
  }
}
