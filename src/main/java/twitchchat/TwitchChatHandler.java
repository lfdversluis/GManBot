package twitchchat;

import com.google.common.io.Files;
import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.cap.EnableCapHandler;
import org.pircbotx.exception.IrcException;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.ConnectEvent;
import org.pircbotx.hooks.events.JoinEvent;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.PrivateMessageEvent;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Created by Dons on 23-05-2016.
 *
 */
public class TwitchChatHandler {
    private static boolean connected = false;
    private static final PircBotX bot;

    // Configure bot
    static {
        Configuration config = new Configuration.Builder()
                .setName("BotManG").setMessageDelay(1900)
                .setAutoReconnect(true)
                .addCapHandler(new EnableCapHandler("twitch.tv/membership"))
                .addCapHandler(new EnableCapHandler("twitch.tv/commands"))
                .addCapHandler(new EnableCapHandler("twitch.tv/tags"))
                .addListener(new ChatUtility())
                .addAutoJoinChannel("#guardsmanbob")
                .buildForServer("irc.chat.twitch.tv", 6667, getPassword());
        bot = new PircBotX(config);
    }

    /**
     * Attempts to connect to the chat server, this may take several seconds so this method should be called before
     * using the bot to send or relieve messages.
     */
    public static void connect() {
        new Thread(() -> {
            try {
                bot.startBot();
            } catch (IrcException | IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static PircBotX getBot() { return bot; }


    /**
     * Loads the twitchChat Oauth token from Data/twitchchatpassword.txt
     * @return the twitchChat OAuth token
     */
    private static String getPassword() {
        File passwordfile = new File("Data/twitchchatpassword.txt");
        if (passwordfile.exists()) {
            try {
                return Files.readFirstLine(passwordfile, Charset.defaultCharset());
            } catch (IOException e) {
                System.out.println("Error when attempting to read the twitchchatpassword text file!");
                e.printStackTrace();
            }
        } else {
            System.out.println("Twitchchatpassword must be located in Data/twitchchatpassword.txt");
        }
        return "";
    }

    private static class ChatUtility extends ListenerAdapter {

        @Override
        public void onConnect(ConnectEvent event) throws Exception {
            connected = true;
            System.out.println("Connected to Twitch Chat Server");
        }

        @Override
        public void onJoin(JoinEvent event) throws Exception {
            if (event.getChannel().getName().equalsIgnoreCase("#guardsmanbob") && event.getUser().getNick().equalsIgnoreCase(bot.getNick())) {
                System.out.println("Joined Channel " + event.getChannel().getName());
            }
        }

        @Override
        public void onMessage(MessageEvent event) throws Exception {
            String displayName = event.getTags().get("display-name");
            String color = event.getTags().get("color");

            System.out.println(event.getChannel().getName() + " " + color + " <" + displayName + "> " + event.getMessage());
        }

        @Override
        public void onPrivateMessage(PrivateMessageEvent event) throws Exception {
            System.out.println("PM from:" + event.getUser().getNick() + " - " + event.getMessage());
        }
    }
}
