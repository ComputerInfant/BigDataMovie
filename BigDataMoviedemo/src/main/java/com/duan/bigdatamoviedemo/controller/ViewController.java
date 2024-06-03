package com.duan.bigdatamoviedemo.controller;

import com.duan.bigdatamoviedemo.model.Movie;
import com.duan.bigdatamoviedemo.service.HBaseService;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.List;
import java.util.UUID;


@Controller
public class ViewController {

    @Autowired
    private HBaseService hBaseService;


    @GetMapping("/")
    public String index() {
        return "index";
    }


    @GetMapping("/search")
    public String search(@RequestParam(required = false) String movieId,
                         @RequestParam(required = false) String title, Model model) throws IOException {
        Movie movie = null;
        if (movieId != null && !movieId.isEmpty()) {
            movie = hBaseService.getMovieById(movieId);
        } else if (title != null && !title.isEmpty()) {
            movie = hBaseService.getMovieByTitle(title);
        }
        model.addAttribute("movie", movie);
        return "index";
    }
    @GetMapping("/top10")
    public String getTop10Movies(Model model) {
        try {
            // 调用 MovieService 中的方法获取前十部电影信息
            List<Movie> movies = hBaseService.getTopMovies();
            model.addAttribute("movies", movies);
        } catch (IOException e) {
            e.printStackTrace();
            return "index";
        }
        return "top10"; //
    }
    @PostMapping("/rate")
    public String rateMovie(@RequestParam String movieId, Model model) {
        try {
            hBaseService.rateMovie(movieId);
            model.addAttribute("message", "评分点击成功");
        } catch (IOException e) {
            e.printStackTrace();
            model.addAttribute("message", "评分点击失败");
        }
        return "index"; // 返回评分结果页面
    }

    @GetMapping("/clickTop10")
    public String getTopClick10Movies(Model model) {
        try {
            List<Movie> top10Movies = hBaseService.getTop10Movies();
            model.addAttribute("top10Movies", top10Movies);
        } catch (IOException e) {
            e.printStackTrace();
            model.addAttribute("message", "获取前10电影失败");
        }
        return "333";
    }



}
