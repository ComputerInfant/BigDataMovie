package com.duan.bigdatamoviedemo.controller;

import com.duan.bigdatamoviedemo.model.Movie;
import com.duan.bigdatamoviedemo.service.HBaseService;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@RestController
@RequestMapping("/movies")
public class MovieController {

    @Autowired
    private HBaseService hBaseService;


    @GetMapping("/{id}")
    public Movie getMovieById(@PathVariable String id) throws IOException {
        return hBaseService.getMovieById(id);
    }

    @GetMapping("/search")
    public Movie getMovieByTitle(@RequestParam String title) throws IOException {
        return hBaseService.getMovieByTitle(title);
    }


    @PostMapping("/delete/{id}")
    public String deleteMovie(@PathVariable String id, Model model) throws IOException {
        hBaseService.deleteMovieById(id);
        model.addAttribute("message", "电影删除成功");
        return "删除成功";
    }
    @PostMapping("/add")
    public String addMovie(@RequestParam String title, @RequestParam String genres, Model model) throws IOException {
        Movie movie = new Movie();
        movie.setTitle(title);
        movie.setGenres(genres);


        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
        movie.setMovieId(timeStamp);
        hBaseService.putMovie(movie);
        model.addAttribute("message", "电影添加成功");
        return "添加成功";
    }






}
