//import org.apache.hadoop.conf.Configuration;
//import org.apache.spark.sql.Dataset;
//import org.apache.spark.sql.Row;
//import org.apache.spark.sql.SparkSession;
//import org.apache.spark.sql.functions;
//import org.apache.hadoop.hbase.HBaseConfiguration;
//import org.apache.hadoop.hbase.client.Connection;
//import org.apache.hadoop.hbase.client.ConnectionFactory;
//import org.apache.hadoop.hbase.client.Put;
//import org.apache.hadoop.hbase.client.Table;
//import org.apache.hadoop.hbase.TableName;
//import org.apache.hadoop.hbase.util.Bytes;
//
//public class RatingsToHBase {
//    public static void main(String[] args) {
//        // 设置 Hadoop Home 目录为绝对路径
//        String hadoopHomeDir = "D:\\BigDataMovie-main\\BigDataMovie_data\\src\\main\\resources\\hadoop-3.0.0";
//        System.setProperty("hadoop.home.dir", hadoopHomeDir);
//
//        SparkSession spark = SparkSession.builder()
//                .appName("RatingsCalculation")
//                .config("spark.master", "local")
//                .getOrCreate();
//
//        // 读取本地文件系统中的数据集
//        Dataset<Row> ratingsDF = spark.read()
//                .option("header", "true")
//                .option("delimiter",",")
//                .csv("C:\\Users\\15214\\Desktop\\BigDataMovie\\ml-latest\\ratings.csv"); // 本地文件路径
//
//        // 输出数据集的模式
//        ratingsDF.printSchema();
//
//        // 计算每部电影的平均评分
//        Dataset<Row> avgRatingsDF = ratingsDF.groupBy("movieId")
//                .agg(functions.avg("rating").alias("avg_rating"));
//
//        // 保存结果到 HBase
//        avgRatingsDF.foreachPartition(partition -> {
//            // 创建 HBase 连接
//            Configuration config = HBaseConfiguration.create();
//            config.set("hbase.zookeeper.quorum", "bigdata"); // 设置Zookeeper连接信息
//            Connection connection = ConnectionFactory.createConnection(config);
//            Table table = connection.getTable(TableName.valueOf("movies"));
//
//            partition.forEachRemaining(row -> {
//                try {
//                    String movieId = row.getString(0);  // 获取电影ID
//                    Double avgRating = row.getDouble(1);  // 获取平均评分
//
//                    Put put = new Put(Bytes.toBytes(movieId));
//                    put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("avg_rating"), Bytes.toBytes(avgRating.toString())); // 添加平均评分列到 "info" 列族
//                    table.put(put);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            });
//
//            // 关闭连接
//            table.close();
//            connection.close();
//        });
//
//
//
//        // 关闭SparkSession
//        spark.stop();
//    }
//}
import org.apache.hadoop.conf.Configuration;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.functions;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.util.Bytes;

public class RatingsToHBase {
    public static void main(String[] args) {
        // 设置 Hadoop Home 目录为绝对路径
        String hadoopHomeDir = "D:\\BigDataMovie-main\\BigDataMovie_data\\src\\main\\resources\\hadoop-3.0.0";
        System.setProperty("hadoop.home.dir", hadoopHomeDir);

        SparkSession spark = SparkSession.builder()
                .appName("RatingsCalculation")
                .config("spark.master", "local")
                .getOrCreate();

        // 读取本地文件系统中的数据集
        Dataset<Row> ratingsDF = spark.read()
                .option("header", "true")
                .option("delimiter", ",")
                .csv("C:\\Users\\15214\\Desktop\\BigDataMovie\\ml-latest\\ratings.csv"); // 本地文件路径

        // 输出数据集的模式
        ratingsDF.printSchema();

        // 计算每部电影的平均评分
        Dataset<Row> avgRatingsDF = ratingsDF.groupBy("movieId")
                .agg(functions.avg("rating").alias("avg_rating"))
                .orderBy(functions.desc("avg_rating")) // 按平均评分降序排列
                .limit(10); // 获取排名前十的电影

        // 保存排名前十的电影到新的 HBase 表
        avgRatingsDF.foreachPartition(partition -> {
            // 创建 HBase 连接
            Configuration config = HBaseConfiguration.create();
            config.set("hbase.zookeeper.quorum", "bigdata"); // 设置 Zookeeper 连接信息
            Connection connection = ConnectionFactory.createConnection(config);
            Table table = connection.getTable(TableName.valueOf("top_movies")); // 新表名为 top_movies

            partition.forEachRemaining(row -> {
                try {
                    String movieId = row.getString(0);  // 获取电影ID
                    Double avgRating = row.getDouble(1);  // 获取平均评分

                    Put put = new Put(Bytes.toBytes(movieId));
                    put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("avg_rating"), Bytes.toBytes(avgRating.toString())); // 添加平均评分列到 "info" 列族
                    table.put(put);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            // 关闭连接
            table.close();
            connection.close();
        });

        // 关闭 SparkSession
        spark.stop();
    }
}
