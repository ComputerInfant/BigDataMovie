package com.duan.bigdatamoviedemo.service;
import org.apache.hadoop.hbase.spark.datasources.HBaseTableCatalog;
import com.duan.bigdatamoviedemo.model.Movie;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;

import org.apache.hadoop.hbase.util.Bytes;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;



@Service
public class HBaseService {

    @Value("${hbase.zookeeper.quorum}")
    private String quorum;

    @Value("${hbase.zookeeper.property.clientPort}")
    private String clientPort;
    private final List<Movie> top10MoviesCache = new CopyOnWriteArrayList<>();
    private Connection connection;
    private  SparkSession spark;
    @PostConstruct
    public void init() throws IOException {


        Configuration config = HBaseConfiguration.create();
        config.set("hbase.zookeeper.quorum", quorum);
        config.set("hbase.zookeeper.property.clientPort", clientPort);
        this.connection = ConnectionFactory.createConnection(config);
        startScheduledTableClear();

//        // 确保连接已成功建立
//        if (connection == null || connection.isClosed()) {
//            throw new IOException("Failed to establish connection to HBase");
//        }
//        spark = SparkSession.builder()
//                .appName("SparkHBaseDemo")
//                .master("local[*]")
//                .getOrCreate();
//        System.out.println("Spark session created");
        // 创建电影索引表
        createMovieTitleIndexTable();

    }
//    public Dataset<Row> getTop10Movies() {
//        String catalog = "{"
//                + "\"table\":{\"namespace\":\"default\", \"name\":\"movies\"},"
//                + "\"rowkey\":\"movieId\","
//                + "\"columns\":{"
//                + "\"movieId\":{\"cf\":\"info\", \"col\":\"movieId\", \"type\":\"string\"},"
//                + "\"title\":{\"cf\":\"info\", \"col\":\"title\", \"type\":\"string\"},"
//                + "\"avg_rating\":{\"cf\":\"info\", \"col\":\"avg_rating\", \"type\":\"double\"}"
//                + "}"
//                + "}";
//
//        // 添加调试日志
//        System.out.println("HBase Catalog: " + catalog);
//
//        try {
//            Dataset<Row> hbaseData = spark.read()
//                    .option(HBaseTableCatalog.tableCatalog(), catalog)
//                    .format("org.apache.hadoop.hbase.spark")
//                    .load();
//
//            // 添加调试日志
//            System.out.println("HBase Data Schema:");
//            hbaseData.printSchema();
//
//            System.out.println("HBase Data Sample:");
//            hbaseData.show(5);
//
//            Dataset<Row> sortedData = hbaseData.orderBy(hbaseData.col("avg_rating").desc()).limit(10);
//
//            // 添加调试日志
//            System.out.println("Top 10 Movies:");
//            sortedData.show();
//
//            return sortedData;
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw e;
//        }
//    }
public void rateMovie(String movieId) throws IOException {
    Table ratingsTable = connection.getTable(TableName.valueOf("movie_ratings"));

    // 获取并更新 movie_ratings 表中的评分点击量信息
    Get get = new Get(Bytes.toBytes(movieId));
    Result result = ratingsTable.get(get);
    long ratingCount = 0;
    if (!result.isEmpty()) {
        ratingCount = Bytes.toLong(result.getValue(Bytes.toBytes("info"), Bytes.toBytes("rating_count")));
    }

    long newRatingCount = ratingCount + 1;

    Put ratingPut = new Put(Bytes.toBytes(movieId));
    ratingPut.addColumn(Bytes.toBytes("info"), Bytes.toBytes("rating_count"), Bytes.toBytes(newRatingCount));
    ratingsTable.put(ratingPut);

    ratingsTable.close();

    // 更新 movies 表中的点击量信息
    Table moviesTable = connection.getTable(TableName.valueOf("movies"));

    Put moviePut = new Put(Bytes.toBytes(movieId));
    moviePut.addColumn(Bytes.toBytes("info"), Bytes.toBytes("rating_count"), Bytes.toBytes(newRatingCount));
    moviesTable.put(moviePut);

    moviesTable.close();
}
    public List<Movie> getTopMovies() throws IOException {
        List<Movie> topMovies = new ArrayList<>();


        Table table = connection.getTable(TableName.valueOf("top_movies"));

        // 使用 Scan 来扫描整个表，并获取前十行数据
        Scan scan = new Scan();

        ResultScanner scanner = table.getScanner(scan);
        int count = 0;
        for (Result result : scanner) {
            if (count >= 10) {
                break; // 已获取到前十部电影信息，结束循环
            }
            String movieId = Bytes.toString(result.getRow());
            // 根据 movieId 获取电影信息
            Movie movie = getMovieById(movieId);
            if (movie != null) {
                topMovies.add(movie);
                count++;
            }
        }

        table.close();
        return topMovies;
    }





    public Movie getMovieById(String movieId) throws IOException {
        Table table = connection.getTable(TableName.valueOf("movies"));
        Get get = new Get(Bytes.toBytes(movieId));
        Result result = table.get(get);

        Movie movie = new Movie();
        movie.setMovieId(movieId);
        movie.setTitle(Bytes.toString(result.getValue(Bytes.toBytes("info"), Bytes.toBytes("title"))));
        movie.setGenres(Bytes.toString(result.getValue(Bytes.toBytes("info"), Bytes.toBytes("genres"))));
        movie.setRating(Bytes.toString(result.getValue(Bytes.toBytes("info"), Bytes.toBytes("avg_rating"))));

        table.close();
        return movie;
    }

    public void createMovieTitleIndexTable() throws IOException {
        Admin admin = connection.getAdmin();
        TableName tableName = TableName.valueOf("movie_title_index");
        if (!admin.tableExists(tableName)) {
            TableDescriptor tableDescriptor = TableDescriptorBuilder.newBuilder(tableName)
                    .setColumnFamily(ColumnFamilyDescriptorBuilder.newBuilder(Bytes.toBytes("info")).build())
                    .build();
            admin.createTable(tableDescriptor);

            // 同步movies表中现有的电影信息到索引表中
            Table movieTable = connection.getTable(TableName.valueOf("movies"));
            Scan scan = new Scan();
            ResultScanner scanner = movieTable.getScanner(scan);
            Table indexTable = connection.getTable(TableName.valueOf("movie_title_index"));
            for (Result result : scanner) {
                String movieId = Bytes.toString(result.getRow());
                String title = Bytes.toString(result.getValue(Bytes.toBytes("info"), Bytes.toBytes("title")));
                Put indexPut = new Put(Bytes.toBytes(title));
                indexPut.addColumn(Bytes.toBytes("info"), Bytes.toBytes("movieId"), Bytes.toBytes(movieId));
                indexTable.put(indexPut);
            }
            movieTable.close();
            indexTable.close();
        }
        admin.close();
    }
    public void putMovie(Movie movie) throws IOException {
        Table movieTable = connection.getTable(TableName.valueOf("movies"));
        Put put = new Put(Bytes.toBytes(movie.getMovieId()));
        put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("title"), Bytes.toBytes(movie.getTitle()));
        put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("genres"), Bytes.toBytes(movie.getGenres()));
        movieTable.put(put);

        // 更新索引表
        Table indexTable = connection.getTable(TableName.valueOf("movie_title_index"));
        Put indexPut = new Put(Bytes.toBytes(movie.getTitle()));
        indexPut.addColumn(Bytes.toBytes("info"), Bytes.toBytes("movieId"), Bytes.toBytes(movie.getMovieId()));
        indexTable.put(indexPut);

        movieTable.close();
        indexTable.close();
    }
    public Movie getMovieByTitle(String title) throws IOException {
        Table indexTable = connection.getTable(TableName.valueOf("movie_title_index"));
        Get get = new Get(Bytes.toBytes(title));
        Result indexResult = indexTable.get(get);
        String movieId = Bytes.toString(indexResult.getValue(Bytes.toBytes("info"), Bytes.toBytes("movieId")));
        indexTable.close();

        if (movieId == null) {
            return null; // 未找到对应的电影
        }

        return getMovieById(movieId);
    }
    public void deleteMovieById(String movieId) throws IOException {
        Table movieTable = connection.getTable(TableName.valueOf("movies"));
        Delete delete = new Delete(Bytes.toBytes(movieId));
        movieTable.delete(delete);
        movieTable.close();

        // 删除索引表中的相关条目
        Table indexTable = connection.getTable(TableName.valueOf("movie_title_index"));
        Scan scan = new Scan();
        scan.addColumn(Bytes.toBytes("info"), Bytes.toBytes("movieId"));
        ResultScanner scanner = indexTable.getScanner(scan);
        for (Result result : scanner) {
            if (movieId.equals(Bytes.toString(result.getValue(Bytes.toBytes("info"), Bytes.toBytes("movieId"))))) {
                Delete indexDelete = new Delete(result.getRow());
                indexTable.delete(indexDelete);
                break;
            }
        }
        indexTable.close();
    }

    //点击量
    public List<Movie> getTop10Movies() throws IOException {

        Table ratingsTable = connection.getTable(TableName.valueOf("movie_ratings"));
        Scan scan = new Scan();
        scan.addColumn(Bytes.toBytes("info"), Bytes.toBytes("rating_count"));
        ResultScanner scanner = ratingsTable.getScanner(scan);


        List<Movie> movies = new ArrayList<>();
        for (Result result : scanner) {
            Movie movie = new Movie();
            movie.setMovieId(Bytes.toString(result.getRow()));
            movie.setRatingCount(Bytes.toLong(result.getValue(Bytes.toBytes("info"), Bytes.toBytes("rating_count"))));
            movies.add(movie);
            System.out.println(movie);
        }

        // 排序并获取前10个
        movies.sort((m1, m2) -> Long.compare(m2.getRatingCount(), m1.getRatingCount()));
        List<Movie> top10Movies = movies.size() > 10 ? movies.subList(0, 10) : movies;
        System.out.println(top10Movies);
        ratingsTable.close();
        return top10Movies;
    }
    private void clearTable() throws IOException {
        Table table = connection.getTable(TableName.valueOf("movie_ratings"));
        Scan scan = new Scan();
        ResultScanner scanner = table.getScanner(scan);
        for (Result result : scanner) {
            Delete delete = new Delete(result.getRow());
            table.delete(delete);
        }
        scanner.close();
        table.close();
        System.out.println("Table cleared at " + System.currentTimeMillis());
    }

    private void startScheduledTableClear() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        Runnable task = () -> {
            try {
                clearTable();
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        scheduler.scheduleAtFixedRate(task, 0, 1, TimeUnit.MINUTES);
    }


}






