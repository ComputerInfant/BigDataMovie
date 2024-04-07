import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class MovieController {

    @Autowired
    private MovieService movieService;

    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public String searchMovie(Model model, @RequestParam(value = "movieId", required = false) String movieId) {
        if (movieId != null) {
            Movie movie = movieService.getMovieById(movieId);
            if (movie != null) {
                model.addAttribute("movie", movie);
            } else {
                model.addAttribute("message", "Movie not found.");
            }
        }
        return "search";
    }
}
