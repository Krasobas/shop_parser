package com.krasobas.shop_parser.parse;

import com.krasobas.shop_parser.model.Product;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SeleniumParser {
    private ChromeDriver driver;

    public SeleniumParser() {
        init();
    }

    public void init() {
        System.setProperty("webdriver.chrome.driver", "./drivers/chromedriver");
        driver = new ChromeDriver();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
    }

    public void siteConnection(){

        System.out.println(driver.findElement(By.cssSelector("html")).getAttribute("innerHTML"));
        driver.findElement(By.cssSelector("html")).findElement(By.cssSelector("body > div:nth-child(3) > div:nth-child(1) > div:nth-child(1) > table > tr > td:nth-child(6) > table > tr:nth-child(1) > td > div:nth-child(1) > div:nth-child(1)")).getText();
    }

    public Product parseDynamicPage(Properties config, String url) {
        Product product = new Product(url);
        try {
            driver.get(url);
            product.setTitle(driver.findElement(By.cssSelector(config.getProperty("product.title"))).getText());
            product.setPrice(driver.findElement(By.cssSelector(config.getProperty("product.price"))).getText());
            product.setDescription(driver.findElement(By.cssSelector(config.getProperty("product.description")))
                    .getText().replaceAll("\n", " "));
            String image = driver.findElement(By.cssSelector(config.getProperty("product.images")))
                    .getAttribute(config.getProperty("product.images.attr"));
            product.setImages(new ArrayList<>(List.of(image)));
//            siteConnection();
        } catch (Exception e) {
            System.out.println("Something wrong");
            e.printStackTrace();
        }
        return product;
    }

    public String getField(Properties config, String url, String property, int times) {
        StringBuilder rsl = new StringBuilder();
        if (times == 0) {
            return rsl.append("Impossible to get optional prices." ).append(System.lineSeparator()).toString();
        }
        rsl.append(System.lineSeparator());
        String buttonCSS = config.getProperty(property.concat(".dynamic.button"));
        String optionCSS = config.getProperty(property.concat(".dynamic.option"));
        String optionAttr = config.getProperty(property.concat(".dynamic.option.attr")) == null ? "" : config.getProperty(property.concat(".dynamic.option.attr"));
        String optionValue = config.getProperty(property.concat(".dynamic.option.value")) == null ? "" : config.getProperty(property.concat(".dynamic.option.value"));
        /**
         * TODO: .dynamic.price - you should replace it by .dynamic.value for make it universal
         */
        String priceCSS = config.getProperty(property.concat(".dynamic.price"));
        String ignore = config.getProperty(property.concat(".dynamic.ignore"));
        try {
            driver.get(url);
            WebDriverWait timer = new WebDriverWait(driver, Duration.ofSeconds(10));
            JavascriptExecutor jse = (JavascriptExecutor)driver;

            WebElement button = driver.findElement(By.cssSelector(buttonCSS));
            jse.executeScript("arguments[0].click();", button);
            boolean isButton = button.getTagName().equals("button");

            List<WebElement> options = driver.findElements(By.cssSelector(optionCSS));
            WebElement option = options.stream().filter(el -> el.isSelected() || optionValue.equals(el.getAttribute(optionAttr))).findFirst().get();
            String initialOption = option.getText();
            boolean isJS = !option.getTagName().equals("option");

            int size = options.size();

            for (int i = 0; i < size; i++) {
                String currentURL = driver.getCurrentUrl();

                options = driver.findElements(By.cssSelector(optionCSS));
                if (options.size() != size) {
                    throw new Exception("NPE");
                }
                option = options.get(i);
                String currentOption = option.getText();
                if (currentOption.equals(ignore)) {
                    continue;
                }
                rsl.append(String.format("%s ", currentOption));

                WebElement priceBeforeClick = driver.findElement(By.cssSelector(priceCSS));

                if (isJS) {
                    jse.executeScript("arguments[0].click();", option);
                } else {
                    option.click();
                }

                if (!initialOption.equals(currentOption)) {
                    timer.until(d -> !d.getCurrentUrl().equals(currentURL) && !priceBeforeClick.equals(driver.findElement(By.cssSelector(priceCSS))));
                    initialOption = currentOption;
                }

                rsl.append(driver.findElement(By.cssSelector(priceCSS)).getText().replaceAll(System.lineSeparator(), ""))
                        .append(System.lineSeparator());

                if (isButton && !currentURL.equals(url)) {
                    timer.until(ExpectedConditions.stalenessOf(button));
                }

                button = driver.findElement(By.cssSelector(buttonCSS));
                jse.executeScript("arguments[0].click();", button);
            }
        } catch (Exception e) {
            System.out.println("Impossible to get a field dynamically because of exception:");
            e.printStackTrace();
            System.out.println(String.format("Retring for: %s%n", url));
            rsl.setLength(0);
            rsl.append(getField(config, url, property, --times));
        }
        return rsl.toString();
    }

    public ChromeDriver getDriver() {
        return driver;
    }

    public void setDriver(ChromeDriver driver) {
        this.driver = driver;
    }

    public static void main(String[] args) {
        SeleniumParser sp = new SeleniumParser();
        System.out.println(sp.parseDynamicPage(sp.initConfig("shops/shop.ledelas.properties"),
                "https://shop.ledelas.fr/index.html#Product=Product_1883"));
        sp.getDriver().close();

    }

    public Properties initConfig(String properties) {
        Properties config = new Properties();
        try (InputStream in = SeleniumParser.class.getClassLoader()
                .getResourceAsStream(properties)) {
            if (in != null) {
                config.load(in);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return config;
    }

    private String setImgSize(String img, Properties config) {
        Pattern pattern = Pattern.compile(config.getProperty("product.images.pattern"));
        Matcher matcher = pattern.matcher(img);
        String[] groups = config.getProperty("product.images.groups").split(":");
        StringBuilder rsl = new StringBuilder();
        if (matcher.find()) {
            rsl.append(matcher.group(Integer.parseInt(groups[0])))
                    .append(config.getProperty("product.images.size"))
                    .append(matcher.group(Integer.parseInt(groups[1])));

        }
        return rsl.toString();
    }
}
