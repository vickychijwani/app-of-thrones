package me.vickychijwani.thrones.network.entity;

import java.util.List;

public class HboSeason {

    private int id;
    private int seasonNumber;
    private List<HboEpisode> episodes;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSeasonNumber() {
        return seasonNumber;
    }

    public void setSeasonNumber(int seasonNumber) {
        this.seasonNumber = seasonNumber;
    }

    public List<HboEpisode> getEpisodes() {
        return episodes;
    }

    public void setEpisodes(List<HboEpisode> episodes) {
        this.episodes = episodes;
    }

}
