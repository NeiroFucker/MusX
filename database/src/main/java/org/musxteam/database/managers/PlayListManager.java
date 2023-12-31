package org.musxteam.database.managers;

import org.musxteam.database.DatabaseConnection;
import org.musxteam.database.managers.types.PlaylistBase;
import org.musxteam.database.models.MusicEntryModel;
import org.musxteam.database.models.PlaylistModel;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;

public class PlayListManager {
    private static final String ADD_COMMAND =
            "INSERT INTO PlayList (title, user) VALUES (\"{0}\", \"{1}\")";
    private static final String ADD_MUSIC_COMMAND =
            "INSERT INTO playlist_music_entries (playlist_id, music_entry_id) VALUES (\"{0}\", \"{1}\")";
    private static final String SELECT_USER_MUSIC_COMMAND =
            "SELECT * FROM playlist_music_entries WHERE playlist_id = \"{0}\"";
    private static final String SELECT_MUSIC_COMMAND =
            "SELECT * FROM playlist_music_entries WHERE playlist_id = \"{0}\" and music_entry_id = \"{1}\"";
    private static final String DELETE_MUSIC_COMMAND =
            "DELETE FROM playlist_music_entries WHERE playlist_id = \"{0}\" and music_entry_id = \"{1}\"";
    private static final String GET_COMMAND = "SELECT * FROM PlayList WHERE user = \"{0}\"";
    private static final String DELETE_COMMAND = "DELETE FROM PlayList WHERE id = \"{0}\"";
    private static final String SELECT_COMMAND = "SELECT * FROM PlayList WHERE id = \"{0}\"";

    public static void addPlaylist(String title, int user_id) {
        DatabaseConnection connection = DatabaseConnection.getInstance();
        connection.executeUpdate(MessageFormat.format(ADD_COMMAND, title, user_id));
    }
    public static void deletePlaylist(int id) throws IllegalArgumentException {
        DatabaseConnection connection = DatabaseConnection.getInstance();

        if (selectPlaylist(id).isEmpty())
            throw new IllegalArgumentException("There is no playlist!");
        connection.executeUpdate(MessageFormat.format(DELETE_COMMAND, id));
    }
    public static void addMusicEntry(int playlistId, String videoId,
                                     String musicService, String title, String channelTitle)
            throws IllegalArgumentException {
        DatabaseConnection connection = DatabaseConnection.getInstance();
        MusicEntryModel entry = MusicEntryManager.addMusicEntry(videoId, musicService, title, channelTitle);

        if (isTrackExists(playlistId, entry.id()))
            throw new IllegalArgumentException("Already exists!");
        connection.executeUpdate(MessageFormat.format(ADD_MUSIC_COMMAND, playlistId, entry.id()));
    }
    public static void deleteMusicEntry(int playlistId, int entryId) throws IllegalArgumentException {
        DatabaseConnection connection = DatabaseConnection.getInstance();

        if (!isTrackExists(playlistId, entryId))
            throw new IllegalArgumentException("There is no track!");
        connection.executeUpdate(MessageFormat.format(DELETE_MUSIC_COMMAND, playlistId, entryId));
    }
    public static ArrayList<PlaylistModel> selectPlaylist(int id) {
        return getPlayLists(MessageFormat.format(SELECT_COMMAND, id));
    }
    public static ArrayList<PlaylistModel> getAllUserPlaylists(int userId) {
        return getPlayLists(MessageFormat.format(GET_COMMAND, userId));
    }

    private static boolean isTrackExists(int playlistId, int entryId) {
        DatabaseConnection connection = DatabaseConnection.getInstance();
        try {
            ResultSet playlistMusic = connection.executeQuery(
                    MessageFormat.format(SELECT_MUSIC_COMMAND, playlistId, entryId)
            );
            boolean alreadyAdded = playlistMusic.next();
            playlistMusic.close(); return alreadyAdded;
        }
        catch (SQLException ex) { ex.printStackTrace(System.out); return true; }
    }
    private static ArrayList<PlaylistModel> getPlayLists(String query) {
        DatabaseConnection connection = DatabaseConnection.getInstance();
        try {
            ResultSet listSet = connection.executeQuery(query);
            ArrayList<PlaylistBase> playlistBases = new ArrayList<>();

            while(listSet.next()) {
                playlistBases.add(new PlaylistBase(
                        listSet.getInt("id"),
                        listSet.getString("title")
                ));
            }

            ArrayList<PlaylistModel> result = new ArrayList<>();
            for (PlaylistBase base : playlistBases) {
                ArrayList<MusicEntryModel> musicEntries = selectMusicEntries(base.id());
                result.add(new PlaylistModel(base.id(), base.title(), musicEntries));
            }

            listSet.close(); return result;
        }
        catch (SQLException ex) { return new ArrayList<>(); }
    }
    private static ArrayList<MusicEntryModel> selectMusicEntries(int playlistId) throws SQLException {
        DatabaseConnection connection = DatabaseConnection.getInstance();

        ArrayList<MusicEntryModel> musicEntries = new ArrayList<>();
        ResultSet musicSet = connection.executeQuery(MessageFormat.format(SELECT_USER_MUSIC_COMMAND, playlistId));

        ArrayList<Integer> entriesId = new ArrayList<>();
        while (musicSet.next()) entriesId.add(musicSet.getInt("music_entry_id"));

        for (int entry_id : entriesId) {
            ArrayList<MusicEntryModel> entry = MusicEntryManager.selectMusicEntry(entry_id);
            if (!entry.isEmpty()) musicEntries.add(entry.getFirst());
        }

        musicSet.close(); return musicEntries;
    }
}
