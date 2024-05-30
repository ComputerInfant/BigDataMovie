//import org.apache.spark.sql.SparkSession;
//import org.apache.spark.sql.Dataset;
//import org.apache.spark.sql.Row;
//import org.apache.spark.sql.functions;
//
//import org.apache.hadoop.hbase.HBaseConfiguration;
//import org.apache.hadoop.hbase.client.Connection;
//import org.apache.hadoop.hbase.client.ConnectionFactory;
//import org.apache.hadoop.hbase.client.Put;
//import org.apache.hadoop.hbase.client.Table;
//import org.apache.hadoop.hbase.TableName;
//import org.apache.hadoop.hbase.util.Bytes;
//
//public class MovieRatingsCalculation {
//    public static void main(String[] args) {
//        // 创建SparkSession
//        SparkSession spark = SparkSession.builder()
//                .appName("MovieRatingsCalculation")
//                .master("local[*]") // 本地运行模式
//                .getOrCreate();
//
//        // 读取本地文件系统中的数据集
//        Dataset<Row> ratingsDF = spark.read().format("csv")
//                .option("header", "true")
//                .load("file:///H:/data/ml-latest/ratings.csv"); // 本地文件路径
//
//        // 计算每部电影的平均评分
//        Dataset<Row> avgRatingsDF = ratingsDF.groupBy("movieId")
//                .agg(functions.avg("rating").alias("avg_rating"));
//
//        // 保存结果到HBase
//        avgRatingsDF.foreachPartition(partition -> {
//            Connection conn = ConnectionFactory.createConnection(HBaseConfiguration.create());
//            Table table = conn.getTable(TableName.valueOf("movie_ratings"));
//
//            partition.forEachRemaining(row -> {
//                try {
//                    String movieId = row.getString(0);  // 获取电影ID
//                    Double avgRating = row.getDouble(1);  // 获取平均评分
//
//                    Put put = new Put(Bytes.toBytes(movieId));
//                    put.addColumn(Bytes.toBytes("cf"), Bytes.toBytes("avg_rating"), Bytes.toBytes(avgRating.toString()));
//                    table.put(put);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            });
//
//            table.close();
//            conn.close();
//        });
//
//        // 关闭SparkSession
//        spark.stop();
//    }
//}
