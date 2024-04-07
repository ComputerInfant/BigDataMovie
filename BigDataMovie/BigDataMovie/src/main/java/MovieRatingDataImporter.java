import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class MovieRatingDataImporter {

    public static void main(String[] args) {
        importMoviesData("D:\\pythonProject1\\movies.csv");
        importRatingsData("D:\\pythonProject1\\ratings.csv");
    }

    public static void importMoviesData(String csvFilePath) {
        try {
            // 读取movies.csv文件
            FileReader fileReader = new FileReader(new File(csvFilePath));
            CSVParser parser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(fileReader);

            // 连接到HBase数据库
            Configuration config = HBaseConfiguration.create();
            config.set("hbase.zookeeper.quorum", "bigdata"); // 设置Zookeeper连接信息
            Connection connection = ConnectionFactory.createConnection(config);
            Table table = connection.getTable(TableName.valueOf("movies"));

            // 逐行处理CSV文件
            for (CSVRecord record : parser) {
                String movieId = record.get("movieId");
                String title = record.get("title");
                String genres = record.get("genres");

                // 将数据写入HBase
                Put put = new Put(Bytes.toBytes(movieId));
                put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("title"), Bytes.toBytes(title));
                put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("genres"), Bytes.toBytes(genres));
                table.put(put);
            }

            // 关闭资源
            parser.close();
            fileReader.close();
            table.close();
            connection.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void importRatingsData(String csvFilePath) {
        try {
            // 读取ratings.csv文件
            FileReader fileReader = new FileReader(new File(csvFilePath));
            CSVParser parser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(fileReader);

            // 连接到HBase数据库
            Configuration config = HBaseConfiguration.create();
            config.set("hbase.zookeeper.quorum", "bih"); // 设置Zookeeper连接信息
            Connection connection = ConnectionFactory.createConnection(config);
            Table table = connection.getTable(TableName.valueOf("ratings"));

            // 逐行处理CSV文件
            for (CSVRecord record : parser) {
                String userId = record.get("userId");
                String movieId = record.get("movieId");
                String rating = record.get("rating");
                String timestamp = record.get("timestamp");

                // 将数据写入HBase
                Put put = new Put(Bytes.toBytes(userId + "-" + movieId + "-" + timestamp));
                put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("userId"), Bytes.toBytes(userId));
                put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("movieId"), Bytes.toBytes(movieId));
                put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("rating"), Bytes.toBytes(rating));
                put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("timestamp"), Bytes.toBytes(timestamp));
                table.put(put);
            }

            // 关闭资源
            parser.close();
            fileReader.close();
            table.close();
            connection.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
