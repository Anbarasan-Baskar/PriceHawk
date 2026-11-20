package com.pricehawk.backend.service;

import com.pricehawk.backend.entity.PriceHistory;
import com.pricehawk.backend.entity.Product;
import com.pricehawk.backend.payload.request.ApifyPriceHistoryRequest;
import com.pricehawk.backend.repository.PriceHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ApifyPriceHistoryService {

    private static final Pattern ASIN_PATTERN = Pattern.compile("/(?:dp|gp/product)/([A-Z0-9]{10})(?:[/?]|$)", Pattern.CASE_INSENSITIVE);

    private final RestTemplate restTemplate;
    private final ProductService productService;
    private final PriceHistoryRepository priceHistoryRepository;
    private final String apifyActorUrl;
    private final String apifyApiToken;

    @Autowired
    public ApifyPriceHistoryService(RestTemplate restTemplate,
                                    ProductService productService,
                                    PriceHistoryRepository priceHistoryRepository,
                                    @Value("${apify.actor.url}") String apifyActorUrl,
                                    @Value("${apify.api.token:}") String apifyApiToken) {
        this.restTemplate = restTemplate;
        this.productService = productService;
        this.priceHistoryRepository = priceHistoryRepository;
        this.apifyActorUrl = apifyActorUrl;
        this.apifyApiToken = apifyApiToken;
    }

    public Map<String, Object> fetchAndStorePriceHistory(ApifyPriceHistoryRequest request) {
        String url = buildActorUrl();

        Map<String, Object> payload = new HashMap<>();
        payload.put("identifiers", request.getIdentifiers());
        payload.put("country", request.getCountry());
        payload.put("include_variants", request.getIncludeVariants() != null ? request.getIncludeVariants() : false);
        payload.put("stream_output", request.getStreamOutput() != null ? request.getStreamOutput() : true);

        String resolvedUrl = Objects.requireNonNull(url, "Apify actor URL must be configured");
        Object actorResponse = restTemplate.postForObject(resolvedUrl, payload, Object.class);
        List<Map<String, Object>> results = normalizeActorResponse(actorResponse);

        List<Map<String, Object>> savedProducts = new ArrayList<>();
        for (Map<String, Object> item : results) {
            Product product = buildProductFromApifyResult(item);
            if (product == null) {
                continue;
            }

            Product saved = productService.saveOrUpdateProduct(product);
            int savedHistoryCount = saveHistoryEntries(saved, item);

            savedProducts.add(Map.of(
                    "productId", saved.getId(),
                    "platformId", saved.getPlatformId(),
                    "name", saved.getName(),
                    "priceHistorySaved", savedHistoryCount
            ));
        }

        return Map.of(
                "sourceCount", results.size(),
                "savedProducts", savedProducts
        );
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> normalizeActorResponse(Object actorResponse) {
        if (actorResponse instanceof List<?> list) {
            return (List<Map<String, Object>>) list;
        }
        if (actorResponse instanceof Map<?, ?> map) {
            Map<String, Object> responseMap = (Map<String, Object>) map;
            Object data = responseMap.get("data");
            if (data instanceof List<?>) {
                return (List<Map<String, Object>>) data;
            }
            Object result = responseMap.get("result");
            if (result instanceof List<?>) {
                return (List<Map<String, Object>>) result;
            }
            Object items = responseMap.get("items");
            if (items instanceof List<?>) {
                return (List<Map<String, Object>>) items;
            }
        }
        return Collections.emptyList();
    }

    private String buildActorUrl() {
        if (apifyApiToken != null && !apifyApiToken.isBlank()) {
            if (apifyActorUrl.contains("?")) {
                return apifyActorUrl + "&token=" + apifyApiToken;
            }
            return apifyActorUrl + "?token=" + apifyApiToken;
        }
        return apifyActorUrl;
    }

    private Product buildProductFromApifyResult(Map<String, Object> item) {
        if (item == null) {
            return null;
        }

        String asin = asString(item.get("asin"));
        String url = asString(item.get("product_url"));
        if (asin == null || asin.isBlank()) {
            asin = extractAsinFromUrl(url);
        }
        if (asin == null || asin.isBlank()) {
            return null;
        }

        Integer price = asInteger(item.get("price"));
        String name = asString(item.get("name"));
        String imageUrl = asString(item.get("image_url"));
        Integer reviewCount = asInteger(item.get("n_reviews"));
        Double rating = asDouble(item.get("rating"));

        Product product = new Product();
        product.setPlatform("AMAZON");
        product.setPlatformId(asin);
        product.setName(name != null ? name : asin);
        product.setProductUrl(url != null ? url : asin);
        product.setImageUrl(imageUrl);
        product.setCurrentPrice(price);
        product.setReviewCount(reviewCount);
        product.setRating(rating);
        product.setIsTracked(false);
        return product;
    }

    private int saveHistoryEntries(Product saved, Map<String, Object> item) {
        int count = 0;
        count += saveHistoryArray(saved, item.get("price_new_history"));
        count += saveHistoryArray(saved, item.get("price_amazon_history"));
        count += saveHistoryArray(saved, item.get("price_buybox_history"));
        count += saveHistoryArray(saved, item.get("price_prime_exclusive_history"));
        count += saveHistoryArray(saved, item.get("price_new_fba_history"));
        if (count == 0 && saved.getCurrentPrice() != null) {
            priceHistoryRepository.save(Objects.requireNonNull(createHistory(saved, saved.getCurrentPrice(), null)));
            count++;
        }
        return count;
    }

    private int saveHistoryArray(Product saved, Object historyObject) {
        if (!(historyObject instanceof List<?> historyList)) {
            return 0;
        }

        int count = 0;
        for (Object rawItem : historyList) {
            if (!(rawItem instanceof Map<?, ?> itemMap)) {
                continue;
            }
            Map<?, ?> record = itemMap;
            Integer price = asInteger(record.get("price"));
            LocalDateTime recordedAt = parseDate(asString(record.get("date")));
            if (price != null) {
                priceHistoryRepository.save(Objects.requireNonNull(createHistory(saved, price, recordedAt)));
                count++;
            }
        }
        return count;
    }

    private PriceHistory createHistory(Product saved, Integer price, LocalDateTime recordedAt) {
        PriceHistory history = new PriceHistory();
        history.setProduct(saved);
        history.setPrice(price);
        if (recordedAt != null) {
            history.setRecordedAt(recordedAt);
        }
        return history;
    }

    private String extractAsinFromUrl(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }
        Matcher matcher = ASIN_PATTERN.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private Integer asInteger(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String text) {
            try {
                String normalized = text.replaceAll("[^0-9.\\-]", "");
                if (normalized.isBlank()) {
                    return null;
                }
                return (int) Math.round(Double.parseDouble(normalized));
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    private Double asDouble(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value instanceof String text) {
            try {
                return Double.parseDouble(text);
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private LocalDateTime parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return LocalDateTime.parse(value);
        } catch (DateTimeParseException ignored) {
        }

        try {
            return OffsetDateTime.parse(value).toLocalDateTime();
        } catch (DateTimeParseException ignored) {
        }

        return null;
    }
}
