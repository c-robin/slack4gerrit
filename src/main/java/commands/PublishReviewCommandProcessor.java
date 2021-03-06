package commands;

import java.util.concurrent.ExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.inject.Singleton;
import com.ullink.slack.review.gerrit.ChangeInfoFormatter;
import com.ullink.slack.review.gerrit.GerritChangeInfoService;
import com.ullink.slack.review.gerrit.reviewrequests.ReviewRequestService;
import com.ullink.slack.review.subscription.ProjectSubscriptionService;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;

@Singleton
public class PublishReviewCommandProcessor implements SlackBotCommandProcessor
{
    @Inject
    private ExecutorService            executor;
    @Inject
    private ReviewRequestService       reviewRequestService;
    @Inject
    private ProjectSubscriptionService projectSubscriptionService;
    @Inject
    private GerritChangeInfoService    gerritChangeInfoService;
    @Inject
    private ChangeInfoFormatter        changeInfoDecorator;

    private Pattern                    PUBLISH_REVIEW_PATTERN = Pattern.compile("!publishreview\\s+([\\w-]+)\\s+(\\d+)+\\s*(.*)");

    public PublishReviewCommandProcessor()
    {
    }

    @Override
    public boolean process(String command, SlackMessagePosted event, SlackSession session)
    {
        Matcher matcher = PUBLISH_REVIEW_PATTERN.matcher(command);
        if (matcher.matches())
        {
            String channelNameToPublish = matcher.group(1);
            String changeId = matcher.group(2);
            String comment = matcher.group(3);
            SlackChannel channel = session.findChannelByName(channelNameToPublish);
            if (channel != null)
            {
                if (event.getSender() != null)
                {
                    executor.execute(new MessageHandler(event.getSender(), channelNameToPublish, event.getChannel(), changeId.trim(), comment, session, reviewRequestService, projectSubscriptionService, gerritChangeInfoService,
                        changeInfoDecorator));
                }
                else if (event.getBot() != null)
                {
                    executor.execute(new MessageHandler(event.getBot(), channelNameToPublish, event.getChannel(), changeId.trim(), comment, session, reviewRequestService, projectSubscriptionService, gerritChangeInfoService,
                        changeInfoDecorator));
                }
            }
            return true;
        }
        return false;
    }

}
