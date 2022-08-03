# shop_parser

This project represent a configurable product parser.

The application can parse static and dynamic web pages as well as pdf catalogs.

To run the application use the command below:

`java -jar target/shop_parser.jar path` where `path` is directory with source files or a source file.

As a source file application can read `.txt` file with URLs or `.pdf` file.

As result application return a CSV file for each source located in `shop_parser/results`.

To prepare the application for work user have to create a `.properties` config file for each source (website or pdf).

The config file has a number of keys:

<details>
  <summary>WEB</summary>

- `app.url:` - the page with a list of products to parse
- `app.store.csv:` - the path to a result CSV file

####Dynamic page:
If you need to parse a dynamic page you have to add the key below:
- `shop.type=dynamic`

####The keys below are CSS queries:

- `product.element:` - ccs for product element on the page with product gallery
- `product.title:`
- `product.link:`
- `product.price:`
- `product.description:` - main description
- `product.info:` - additional information

You can add several css query using `;` delimiter if you need to concat some elements.
For example: `product.price:.o-detail__purchase--prices .product-price .from;.amount;.price-middle`

#####Dynamic field:
If you need to parse only some fields dynamically you should add these keys for each field:

- `product.price.dynamic:true`
- `product.price.dynamic.check:` element to check if it is present on current page
- `product.price.dynamic.button:` it will be clicked
- `product.price.dynamic.option:` it will be also clicked
- `product.price.dynamic.option.attr:` attribute to check if option is selected
- `product.price.dynamic.option.value:` expected value
- `product.price.dynamic.price:` product filed to save
- `product.price.dynamic.ignore:` text to ignore

#####Images:

- `product.images:` - css query for image
- `product.images.attr:` - attribute with image link
- `product.images.carousel:`  if there is an image gallery `true`, otherwise `false`

The keys below are required if `product.images.carousel` is `true` and you need to change the image size in the image link.

- `product.images.size:` - new size
- `product.images.pattern:` - pattern to split the image link on groups 
- `product.images.groups:` - groups to save

</details>
<details>
  <summary>PDF</summary> 
The config file for pdf catalog has to have the keys below:

- `pdf.header:` table header to delete where column titles are concat by `|`
- `pdf.categories:` the same for categories if you need
- `pdf.errors:` words to delete

At first parser will get all pdf lines and delete header, categories and errors.
Then it will split each line using these keys:

- `product.title.start:`
- `product.title.end:`
- `product.origin.start:`
- `product.origin.end:`
- `product.description.start:`
- `product.description.end:`
- `product.info.start:`
- `product.info.end:`
- `product.price.start:`
- `product.price.end:`

Finally, you need to indicate the path to a result file:
- `app.store.csv:`
</details>

###Used technologies:

- JSOUP – for static pages parsing
-  SELENIUM – for dynamic pages parsing
- PDFBOX – for PDF parsing
- OPENCSV – for serialization Product objects to a CSV file.



