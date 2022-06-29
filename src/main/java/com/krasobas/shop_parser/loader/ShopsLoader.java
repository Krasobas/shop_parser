package com.krasobas.shop_parser.loader;

import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ShopsLoader {
    private Set<String> urls;
    private Set<String> resources;
    private Map<String, Set<String>> shops;

    public ShopsLoader(String path) {
        initUrls(path);
        initResources();
        initShops();
    }

    private void initUrls(String path) {
        Set<String> urls = Collections.emptySet();
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(path))) {
            urls = reader.lines().filter(url -> !url.isEmpty()).collect(Collectors.toCollection(TreeSet::new));
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.urls =  urls;
    }

    private void initResources() {
        Reflections reflections = new Reflections("", new ResourcesScanner());
        this.resources = reflections.getResources(Pattern.compile(".*\\.properties"));
    }

    private void initShops() {
        this.shops = new HashMap<>();
        this.urls.forEach(url -> {
            String properties = getPropertiesName(url);
            if (this.resources.contains(properties)) {
                if (!this.shops.containsKey(properties)) {
                    this.shops.put(properties, new TreeSet<>(List.of(url)));
                } else {
                    this.shops.get(properties).add(url);
                }
            }
        });
    }

    private static String getPropertiesName(String url) {
        StringBuilder properties = new StringBuilder();
        Pattern pattern = Pattern.compile("((http[s]?|ftp)://)([www\\.]*)(([^:^/]*)(\\.[a-z]+))");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            properties.append(matcher.group(5));
        }
        return properties.append(".properties").toString();
    }

    public Map<String, Set<String>> getShops() {
        return shops;
    }

    public void setShops(Map<String, Set<String>> shops) {
        this.shops = shops;
    }
}
