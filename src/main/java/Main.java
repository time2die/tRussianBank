import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.generics.BotOptions;

import java.util.logging.Logger;

/**
 * Created by time2die on 06.11.16.
 */
@SpringBootApplication

public class Main {
    private static final Logger logger = Logger.getLogger("Telegram Bots Api");

    public static void main(String[] args) throws TelegramApiRequestException {
        TelegramBotsApi tg = new TelegramBotsApi();
        ApiContextInitializer.init();

        tg.registerBot(new MyAmazingBot());
    }

    public static class MyAmazingBot extends TelegramLongPollingBot {
        @Override
        public void onUpdateReceived(Update update) {

            if (update.hasMessage() && update.getMessage().hasText() && "/status".equals(update.getMessage().getText())) {
                SendMessage message = new SendMessage()
                        .setChatId(update.getMessage().getChatId())
                        .setText("very much $$$$$");
                try {
                    sendMessage(message);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public String getBotUsername() {
            return "tRussianBank";
        }

        @Override
        public String getBotToken() {
            return "";
        }

    }
}

