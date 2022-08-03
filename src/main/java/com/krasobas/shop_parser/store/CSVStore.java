package com.krasobas.shop_parser.store;

import com.krasobas.shop_parser.model.Product;
import com.opencsv.CSVWriter;
import com.opencsv.bean.*;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import org.apache.commons.collections4.comparators.FixedOrderComparator;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class CSVStore implements Store {
    @Override
    public void store(List<Product> products, Properties config) {
        /**
         * TODO: if user give some txt with the same shops
         * result will be written below existing text but with reaping of header
         * better is to create each time a new file for example with actual date and time
         */
        String path = config.getProperty("app.store.csv");
        try (Writer writer = Files.newBufferedWriter(Paths.get(path), StandardCharsets.UTF_8, StandardOpenOption.APPEND)) {
            StatefulBeanToCsv<Product> sbc = new StatefulBeanToCsvBuilder<Product>(writer)
                    .withSeparator(CSVWriter.DEFAULT_SEPARATOR)
                    .withMappingStrategy(getStrategy())
                    .build();
//            .withMappingStrategy(getStrategy())

            sbc.write(products);
        } catch (IOException | CsvRequiredFieldEmptyException | CsvDataTypeMismatchException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Product> getList(Properties config) {
        List<Product> list = Collections.emptyList();
        try (Reader reader = Files.newBufferedReader(Paths.get(config.getProperty("app.store.csv")))) {
            CsvToBean<Product> csv = new CsvToBeanBuilder<Product>(reader)
                    .withType(Product.class)
                    .withMappingStrategy(getStrategy())
                    .build();
//            .withMappingStrategy(getStrategy())
            list = csv.parse();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    private HeaderColumnNameMappingStrategy<Product> getStrategy() {
        String[] columns = new String[] {"ID", "TITLE", "PRICE", "LINK", "IMAGES", "ORIGIN", "DESCRIPTION", "INFO"};
//        ColumnPositionMappingStrategy<Product> strategy = new ColumnPositionMappingStrategy<>();
        HeaderColumnNameMappingStrategy<Product> strategy = new HeaderColumnNameMappingStrategy<>();
        strategy.setType(Product.class);
        strategy.setColumnOrderOnWrite(new FixedOrderComparator<>(columns));

//        strategy.setColumnMapping(columns);

        return strategy;
    }
}
