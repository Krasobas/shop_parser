package com.krasobas.shop_parser.parse;

import com.krasobas.shop_parser.model.Product;
import org.jsoup.nodes.Element;

import java.util.List;
import java.util.function.Predicate;

public interface Parser {
    List<Product> parse();
}
