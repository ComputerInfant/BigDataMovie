import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Repository;

import java.io.IOException;

@Repository
public class MovieDao {

    @Autowired
    private Connection hbaseConnection;

    public Movie getMovieById(String movieId) throws IOException {
        Table table = hbaseConnection.getTable(TableName.valueOf("movies"));
        Get get = new Get(Bytes.toBytes(movieId));
        Result result = table.get(get);

        Movie movie = new Movie();
        movie.setMovieId(movieId);
        movie.setTitle(Bytes.toString(result.getValue(Bytes.toBytes("info"), Bytes.toBytes("title"))));
        movie.setGenres(Bytes.toString(result.getValue(Bytes.toBytes("info"), Bytes.toBytes("genres"))));

        table.close();
        return movie;
    }
}
