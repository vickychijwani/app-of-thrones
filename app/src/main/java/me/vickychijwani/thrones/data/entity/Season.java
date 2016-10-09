package me.vickychijwani.thrones.data.entity;

import java.util.List;

public class Season {

    public final int number;
    public final List<Episode> episodes;

    public Season(int number, List<Episode> episodes) {
        this.number = number;
        this.episodes = episodes;
    }

}
