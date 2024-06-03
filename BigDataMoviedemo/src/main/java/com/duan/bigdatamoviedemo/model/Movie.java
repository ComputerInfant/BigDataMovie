package com.duan.bigdatamoviedemo.model;

public class Movie {
    private String movieId;
    private String title;
    private String genres;
    private String rating;
    private long RatingCount;


    // Getters and setters

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getMovieId() {
        return movieId;
    }

    public void setMovieId(String movieId) {
        this.movieId = movieId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getGenres() {
        return genres;
    }

    public void setGenres(String genres) {
        this.genres = genres;
    }

    public long getRatingCount() {
        return RatingCount;
    }

    public void setRatingCount(long ratingCount) {
        RatingCount = ratingCount;
    }
}
