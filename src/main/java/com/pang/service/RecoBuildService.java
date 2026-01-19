package com.pang.service;


public interface RecoBuildService {
    void rebuildSongSimFromPlaylists(int topK);
    void rebuildPlaylistSim(int topK);
}
