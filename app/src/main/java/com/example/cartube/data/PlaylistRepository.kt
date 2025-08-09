package com.example.cartube.data

import android.content.Context
import android.content.SharedPreferences

class PlaylistRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("playlists", Context.MODE_PRIVATE)
    private val key = "playlist_urls"

    fun getPlaylists(): MutableSet<String> {
        return prefs.getStringSet(key, mutableSetOf()) ?: mutableSetOf()
    }

    fun addPlaylist(url: String) {
        val playlists = getPlaylists()
        playlists.add(url)
        prefs.edit().putStringSet(key, playlists).apply()
    }

    fun removePlaylist(url: String) {
        val playlists = getPlaylists()
        playlists.remove(url)
        prefs.edit().putStringSet(key, playlists).apply()
    }
}
