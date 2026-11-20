package com.pricehawk.backend.payload.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CompareItem {
    private String platform;
    private String platformId;
    private String name;
    private String title;
    private Double price;
    private String image;
    private String url;
    private String productUrl;
    private Double confidence;
    private Double rating;
    private Integer ratingCount;
    private Integer reviewCount;
    private String availability;
    private Integer position;
    private String source;

    @JsonProperty("is_lowest")
    private Boolean lowest;

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getPlatformId() {
        return platformId;
    }

    public void setPlatformId(String platformId) {
        this.platformId = platformId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Object price) {
        this.price = parseDouble(price);
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getProductUrl() {
        return productUrl;
    }

    public void setProductUrl(String productUrl) {
        this.productUrl = productUrl;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Object confidence) {
        this.confidence = parseDouble(confidence);
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Object rating) {
        this.rating = parseDouble(rating);
    }

    public Integer getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(Object ratingCount) {
        this.ratingCount = parseInteger(ratingCount);
    }

    public Integer getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(Object reviewCount) {
        this.reviewCount = parseInteger(reviewCount);
    }

    public String getAvailability() {
        return availability;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Object position) {
        this.position = parseInteger(position);
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    @JsonProperty("is_lowest")
    public Boolean getLowest() {
        return lowest;
    }

    @JsonProperty("is_lowest")
    public void setLowest(Boolean lowest) {
        this.lowest = lowest;
    }

    public CompareItem copy() {
        CompareItem copy = new CompareItem();
        copy.platform = platform;
        copy.platformId = platformId;
        copy.name = name;
        copy.title = title;
        copy.price = price;
        copy.image = image;
        copy.url = url;
        copy.productUrl = productUrl;
        copy.confidence = confidence;
        copy.rating = rating;
        copy.ratingCount = ratingCount;
        copy.reviewCount = reviewCount;
        copy.availability = availability;
        copy.position = position;
        copy.source = source;
        copy.lowest = lowest;
        return copy;
    }

    private Double parseDouble(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value instanceof String text) {
            try {
                String normalized = text.replaceAll("[^0-9.\\-]", "");
                return normalized.isBlank() ? null : Double.parseDouble(normalized);
            } catch (Exception ignored) {
                return null;
            }
        }
        return null;
    }

    private Integer parseInteger(Object value) {
        Double parsed = parseDouble(value);
        return parsed == null ? null : parsed.intValue();
    }
}
