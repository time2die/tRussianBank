import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;

/**
 * Created by time2die on 06.11.16.
 */

public class Main {

    public static void main(String[] args) throws TelegramApiRequestException {
        ApiContextInitializer.init();

        new TelegramBotsApi().registerBot(new tRussianBankBot());
    }

    public static class tRussianBankBot extends TelegramLongPollingBot {
        @Override
        public void onUpdateReceived(Update update) {
            if (updateHasCommand(update, "/status")) {
                if (userHasRights(update)) {
                    sendMessage(update, GoogleApiClient.getStatus());
                }
            }else if(updateHasCommand(update, "/search урбанист") && userHasRights(update)){
                sendMessage(update,"Довольно странно спрашивать информация про урбаниста кода Вы в компании приличных русских людей");
            } else if (updateStartWithCommand(update, "/search") && userHasRights(update)) {
                processSearchOperation(update);
            } else {
                processElseVariant(update);
            }
        }

        void processSearchOperation(Update update){
            String text = null;
            try {
                text = update.getMessage().getText().split(" ")[1];
            } catch (java.lang.ArrayIndexOutOfBoundsException ex) {
                sendMessage(update, "Следует указать кого вы ищите.\nПример работы: /search урбанист");
            }

            sendMessage(update, text);
        }

        void processElseVariant(Update update) {
            if (userHasRights(update))
                sendMessage(update, "cant analize:>" + update.getMessage().getText() + "<");
            else
                sendMessage(update, "you hasn't right for this command");
        }

        public void sendMessage(Update update, String text) {
            SendMessage message = new SendMessage()
                    .setChatId(update.getMessage().getChatId())
                    .setText(text);
            try {
                sendMessage(message);
            } catch (TelegramApiException e) {e.printStackTrace();}
        }

        public boolean updateHasCommand(Update update, String message) {
            return update.hasMessage() && update.getMessage().hasText() && message.equals(update.getMessage().getText().trim().toLowerCase());
        }

        public boolean updateStartWithCommand(Update update, String message) {
            return update.hasMessage() && update.getMessage().hasText() && update.getMessage().getText().toLowerCase().startsWith(message);
        }


        public boolean userHasRights(Update update) {
            return update.getMessage().getChatId() == 69711013 || update.getMessage().getChatId() == -29036710;
        }

        @Override
        public String getBotUsername() {
            return "tRussianBank";
        }

        @Override
        public String getBotToken() {
            return "231872682:AAH_QI3VbUAqaDfAs4jO4fYIjbdJCsAdsuY";
        }
    }
}