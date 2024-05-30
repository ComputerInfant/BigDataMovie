import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.functions;

public class RatingsCalculation {
    public static void main(String[] args) {
        // 设置 Hadoop Home 目录为绝对路径
        String hadoopHomeDir = "H:\\flyy\\BigDataMovie-main\\BigDataMovie\\BigDataMovie_data\\src\\main\\resources\\hadoop-3.0.0";
        System.setProperty("hadoop.home.dir", hadoopHomeDir);

        SparkSession spark = SparkSession.builder()
                .appName("RatingsCalculation")
                .config("spark.master", "local")
                .getOrCreate();

        // 读取本地文件系统中的数据集
        Dataset<Row> ratingsDF = spark.read()
                .option("header", "true")
                .option("delimiter",",")
                .csv("H:\\data\\ml-latest\\ratings.csv"); // 本地文件路径

        // 输出数据集的模式
        ratingsDF.printSchema();

        // 计算每部电影的平均评分
        Dataset<Row> avgRatingsDF = ratingsDF.groupBy("movieId")
                .agg(functions.avg("rating").alias("avg_rating"));

        // 打印结果
        avgRatingsDF.show();

        // 关闭SparkSession
        spark.stop();
    }
}
