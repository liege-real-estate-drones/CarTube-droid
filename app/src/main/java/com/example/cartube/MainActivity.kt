package com.example.cartube

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cartube.data.PlaylistRepository

class MainActivity : AppCompatActivity() {

    private lateinit var playlistRepository: PlaylistRepository
    private lateinit var playlistAdapter: PlaylistAdapter
    private lateinit var playlistUrlEditText: EditText
    private lateinit var addPlaylistButton: Button
    private lateinit var playlistsRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        playlistRepository = PlaylistRepository(this)

        playlistUrlEditText = findViewById(R.id.playlistUrlEditText)
        addPlaylistButton = findViewById(R.id.addPlaylistButton)
        playlistsRecyclerView = findViewById(R.id.playlistsRecyclerView)

        setupRecyclerView()
        loadPlaylists()

        addPlaylistButton.setOnClickListener {
            val url = playlistUrlEditText.text.toString()
            if (url.isNotBlank() && url.contains("youtube.com/playlist")) {
                playlistRepository.addPlaylist(url)
                playlistUrlEditText.text.clear()
                loadPlaylists()
            } else {
                Toast.makeText(this, "Please enter a valid YouTube playlist URL", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupRecyclerView() {
        playlistAdapter = PlaylistAdapter(mutableListOf()) { url ->
            playlistRepository.removePlaylist(url)
            loadPlaylists()
        }
        playlistsRecyclerView.layoutManager = LinearLayoutManager(this)
        playlistsRecyclerView.adapter = playlistAdapter
    }

    private fun loadPlaylists() {
        val playlists = playlistRepository.getPlaylists().toList()
        playlistAdapter.updateData(playlists)
    }
}
