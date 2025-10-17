package com.Tsimur.Dubcast;


import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NowPlayingService {
    // TODO: замени streamUrl на реальные прямые ссылки (SoundCloud/статические mp3)
    private final List<Track> playlist = List.of(
            new Track("t1","Track One","https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3", 6 * 60_000L),
            new Track("t2","Track Two","https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3", 5 * 60_000L),
            new Track("t3","Track Three","https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3", 4 * 60_000L)
    );

    private final long startEpochMs = System.currentTimeMillis();

    public NowPlayingDto now() {
        long now = System.currentTimeMillis();
        long total = playlist.stream().mapToLong(Track::durationMs).sum();

        long t = (now - startEpochMs) % total;
        long acc = 0;
        Track current = playlist.get(0);
        long curStartOffset = 0;

        for (Track tr : playlist) {
            if (t < acc + tr.durationMs()) {
                current = tr;
                curStartOffset = acc;
                break;
            }
            acc += tr.durationMs();
        }

        long startedAt = now - (t - curStartOffset);
        long endsAt = startedAt + current.durationMs();
        long posMs = now - startedAt;

        return new NowPlayingDto(current.id(), current.title(), current.streamUrl(), startedAt, endsAt, posMs);
    }
}