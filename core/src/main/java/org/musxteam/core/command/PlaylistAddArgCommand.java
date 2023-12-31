package org.musxteam.core.command;

import org.musxteam.core.IRequest;
import org.musxteam.core.RequestReplies;
import org.musxteam.core.command.types.CommandBase;
import org.musxteam.core.command.types.HandlingState;
import org.musxteam.core.command.types.ICommandState;
import org.musxteam.core.views.PlaylistViewBase;
import org.musxteam.core.views.types.IViewFactory;
import org.musxteam.core.views.types.PlaylistView;
import org.musxteam.database.managers.PlayListManager;
import org.musxteam.database.models.PlaylistModel;
import org.musxteam.music.search.types.ISearchItem;
import org.musxteam.music.service.MusicServiceBase;

import java.io.IOException;
import java.util.ArrayList;

public class PlaylistAddArgCommand extends CommandBase {
    private String videoId;

    @Override
    protected ICommandState initStartState() { return new StartState(); }

    class StartState implements ICommandState {
        @Override
        public HandlingState handleRequest(IRequest request, IViewFactory viewFactory) {
            videoId = request.getText().split(" ")[1];
            ArrayList<PlaylistView> views = new ArrayList<>();

            for (PlaylistModel playlist : PlayListManager.getAllUserPlaylists(request.getUser().getId())) {
                views.add(new PlaylistView(playlist.title(), Integer.toString(playlist.id())));
            }
            PlaylistViewBase view = viewFactory.getPlaylistView(RequestReplies.PLAYLIST_CHOOSE.getReply(), views);

            changeState(new AddToPlaylistState()); return new HandlingState(view, false);
        }
    }

    class AddToPlaylistState implements ICommandState {
        @Override
        public HandlingState handleRequest(IRequest request, IViewFactory viewFactory) {
            try {
                int playlistId = Integer.parseInt(request.getText());
                ArrayList<PlaylistModel> entry = PlayListManager.selectPlaylist(playlistId);

                if (!entry.isEmpty()) {
                    MusicServiceBase service = request.getUser().musicService;
                    ISearchItem item = service.searchId(videoId);
                    PlayListManager.addMusicEntry(
                            playlistId, item.getItemVideoId(), "youtube",
                            item.getItemTitle(), item.getItemChannelTitle()
                    );
                    return new HandlingState(viewFactory.getTextMessageView(
                            RequestReplies.PLAYLIST_TRACK_ADDED.getReply()), true);
                }
                return new HandlingState(viewFactory.getTextMessageView(
                        RequestReplies.ILLEGAL_PLAYLIST_ID.getReply()), false);
            }
            catch (IOException | IllegalArgumentException ex) {
                return new HandlingState(viewFactory.getTextMessageView(ex.toString()), true);
            }
        }
    }
}
