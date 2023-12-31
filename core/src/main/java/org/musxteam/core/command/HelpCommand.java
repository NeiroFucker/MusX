package org.musxteam.core.command;

import org.musxteam.core.RequestReplies;
import org.musxteam.core.command.types.CommandBase;
import org.musxteam.core.IRequest;
import org.musxteam.core.command.types.HandlingState;
import org.musxteam.core.command.types.ICommandState;
import org.musxteam.core.views.types.IViewFactory;

public class HelpCommand extends CommandBase {
    @Override
    protected ICommandState initStartState() { return new StartState(); }

    class StartState implements ICommandState {
        @Override
        public HandlingState handleRequest(IRequest request, IViewFactory viewFactory) {
            return new HandlingState(viewFactory.getTextMessageView(RequestReplies.HELP.getReply()), true);
        }
    }
}
