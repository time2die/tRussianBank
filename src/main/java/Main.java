import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;
import org.time2java.tRussianBank.domain.User;
import org.time2java.tRussianBank.domain.gaAnswer;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by time2die on 06.11.16.
 */

public class Main {

    public static void main(String[] args) throws TelegramApiRequestException {
        ApiContextInitializer.init();

        new TelegramBotsApi().registerBot(new tRussianBankBot());
    }

    public static class tRussianBankBot extends TelegramLongPollingBot {
        Config conf = ConfigFactory.load();

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
//                processElseVariant(update);
            }
        }

        void processSearchOperation(Update update){
            String text = null;
            try {
                text = update.getMessage().getText().split(" ")[1];
            } catch (java.lang.ArrayIndexOutOfBoundsException ex) {
                sendMessage(update, "Следует указать кого вы ищите.\nПример работы: /search урбанист");
            }

            gaAnswer ga = GoogleApiClient.getAllUser() ;
            Set<List<String>> searchResult = search(ga,text.toLowerCase()) ;

            if(searchResult.size() == 0){
                sendMessage(update,"Совпадений нет");
                return;
            }else if(searchResult.size() > 1 ){
                sendMessage(update,"Количество совпадений: "+searchResult.size()+"\nИспользуйте другой запрос");
                return;
            }

            List<String> resultUser = searchResult.iterator().next() ;

            String v0 = resultUser.get(0) ;
            String v1 = resultUser.get(1) ;
            String v2 = resultUser.get(2) ;
            String v3 = resultUser.get(3) ;
            String v4 = resultUser.get(4) ;
            String v5 = resultUser.get(5) ;
            String v6 = resultUser.get(6) ;
            String v7 = resultUser.get(7) ;
            String v8 = resultUser.get(8) ;
            String v9 = resultUser.get(9) ;


//            User user = User
//                    .builder()
//                    .name(v0)
//                    .vkID(v1)
//                    .city(v2)
//                    .paymentNum(v3 == null ? 0 : Integer.valueOf(v3))
//                    .paymentSum(v4 == null ? 0 : Double.valueOf(v4))
//                    .debtCount(v5 == null ? 0 : Integer.valueOf(v5))
//                    .currentDeb(v6 == null ? 0 : Double.valueOf(v6))
//                    .earlyReturn(v7 == null ? 0 : Integer.valueOf(v7))
//                    .hasLastMounthsPays(v8 == null ? false : Boolean.FALSE.valueOf(v8))
//                    .build();

            StringBuffer sb = new StringBuffer() ;
            try {
                sb.append(v0) ;
                sb.append("\nВсего взносов: "+v3);
                sb.append("\nНа сумму: "+v4);
                sb.append("\nВсего займов: "+v5);
                sb.append("\nСейчас должен: "+v6);
                sb.append("\nДосрочных погашений: "+ ("".equals(v7)? "нет" :v7));
                sb.append("\nПросрочек: "+("".equals(v8)? "нет" :v8));
//                sb.append("\nВзносов за 3 месяца : "+v9);
            }catch (Exception e){
                e.printStackTrace();
            }


            sendMessage(update,sb.toString());
        }

        private Set<List<String>> search(gaAnswer ga, String text) {
            Set<List<String>> result = new HashSet<>() ;

            for (List<String> userIterator : ga.getValues()) {
                for (String userVariable : userIterator) {
                    if((userVariable+"").toLowerCase().indexOf(text) != -1){
                        result.add(userIterator) ;
                        continue;
                    }
                }
            }

            return result;
        }

        void processElseVariant(Update update) {
            if (userHasRights(update))
                sendMessage(update, "Я пока так не умею" + update.getMessage().getText() + "<");
            else
                sendMessage(update, "У вас не достаточно прав для выполнения запроса");
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
            return conf.getString("tgBotKey");
        }
    }
}