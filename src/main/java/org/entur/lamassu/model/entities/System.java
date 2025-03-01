package org.entur.lamassu.model.entities;

public class System implements Entity {
    String id;
    String language;
    TranslatedString name;
    TranslatedString shortName;
    Operator operator;
    String url;
    String purchaseUrl;
    String startDate;
    String phoneNumber;
    String email;
    String feedContactEmail;
    String timezone;
    String licenseUrl;
    BrandAssets brandAssets;
    String termsUrl;
    String termsLastUpdated;
    String privacyUrl;
    String privacyLastUpdated;
    RentalApps rentalApps;

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public TranslatedString getName() {
        return name;
    }

    public void setName(TranslatedString name) {
        this.name = name;
    }

    public TranslatedString getShortName() {
        return shortName;
    }

    public void setShortName(TranslatedString shortName) {
        this.shortName = shortName;
    }

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPurchaseUrl() {
        return purchaseUrl;
    }

    public void setPurchaseUrl(String purchaseUrl) {
        this.purchaseUrl = purchaseUrl;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFeedContactEmail() {
        return feedContactEmail;
    }

    public void setFeedContactEmail(String feedContactEmail) {
        this.feedContactEmail = feedContactEmail;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getLicenseUrl() {
        return licenseUrl;
    }

    public void setLicenseUrl(String licenseUrl) {
        this.licenseUrl = licenseUrl;
    }

    public BrandAssets getBrandAssets() {
        return brandAssets;
    }

    public void setBrandAssets(BrandAssets brandAssets) {
        this.brandAssets = brandAssets;
    }

    public String getTermsUrl() {
        return termsUrl;
    }

    public void setTermsUrl(String termsUrl) {
        this.termsUrl = termsUrl;
    }

    public String getTermsLastUpdated() {
        return termsLastUpdated;
    }

    public void setTermsLastUpdated(String termsLastUpdated) {
        this.termsLastUpdated = termsLastUpdated;
    }

    public String getPrivacyUrl() {
        return privacyUrl;
    }

    public void setPrivacyUrl(String privacyUrl) {
        this.privacyUrl = privacyUrl;
    }

    public String getPrivacyLastUpdated() {
        return privacyLastUpdated;
    }

    public void setPrivacyLastUpdated(String privacyLastUpdated) {
        this.privacyLastUpdated = privacyLastUpdated;
    }

    public RentalApps getRentalApps() {
        return rentalApps;
    }

    public void setRentalApps(RentalApps rentalApps) {
        this.rentalApps = rentalApps;
    }

    @Override
    public String toString() {
        return "System{" +
                "id='" + id + '\'' +
                ", language='" + language + '\'' +
                ", name=" + name +
                ", shortName=" + shortName +
                ", operator=" + operator +
                ", url='" + url + '\'' +
                ", purchaseUrl='" + purchaseUrl + '\'' +
                ", startDate='" + startDate + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", email='" + email + '\'' +
                ", feedContactEmail='" + feedContactEmail + '\'' +
                ", timezone='" + timezone + '\'' +
                ", licenseUrl='" + licenseUrl + '\'' +
                ", brandAssets=" + brandAssets +
                ", termsUrl='" + termsUrl + '\'' +
                ", termsLastUpdated='" + termsLastUpdated + '\'' +
                ", privacyUrl='" + privacyUrl + '\'' +
                ", privacyLastUpdated='" + privacyLastUpdated + '\'' +
                ", rentalApps=" + rentalApps +
                '}';
    }
}
