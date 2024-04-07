import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MovieService {

    @Autowired
    private MovieDao movieDao;

    public Movie getMovieById(String movieId) {
        return movieDao.getMovieById(movieId);
    }
}
