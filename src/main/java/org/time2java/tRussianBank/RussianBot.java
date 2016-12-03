package org.time2java.tRussianBank;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.time2java.tRussianBank.domain.gaAnswer;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by time2die on 03.12.16.
 */
@Slf4j
public class RussianBot extends TelegramLongPollingBot {
    Config conf = ConfigFactory.load();

    @Override
    public void onUpdateReceived(Update update) {
        if (updateHasCommand(update, "/status")) {
            if (userHasRights(update)) {
                sendMessage(update, GoogleApiClient.getStatus());
            }
        } else if (updateHasCommand(update, "/search урбанист") && userHasRights(update)) {
            sendMessage(update, "Довольно странно спрашивать информация про урбаниста кода Вы в компании приличных русских людей");
        } else if (updateStartWithCommand(update, "/search") && userHasRights(update)) {
            processSearchOperation(update);
        } else if (updateHasCommand(update, "/debts") && userHasRights(update)) {
            processDebtsCommand(update);
        } else {
//                processElseVariant(update);
        }
    }

    private void processDebtsCommand(Update update) {
        gaAnswer ga = GoogleApiClient.getAllUser();
        Set<List<String>> searchResult = searchDebts(ga);
        StringBuffer sb = new StringBuffer("Должники: ");
        for (List<String> resultUser : searchResult) {
            String v0 = resultUser.get(0);
            String v6 = resultUser.get(6);
            String v7 = resultUser.get(7);

            sb.append("\n");
            sb.append("\n");
            sb.append(v0);
            sb.append("\nДолжен " + v6);
            sb.append(" р до " + v7);

        }

        sendMessage(update, sb.toString());

    }

    private Set<List<String>> searchDebts(gaAnswer ga) {
        Set<List<String>> result = new HashSet<>();

        for (List<String> iter : ga.getValues()) {
            if (!"".equals(iter.get(7))) {
                result.add(iter);
                continue;
            }
        }


        return result;
    }

    void processSearchOperation(Update update) {
        String text = null;
        try {
            text = update.getMessage().getText().split(" ")[1].toLowerCase();
            text = StringUtils.replace(text, "ё", "е");
        } catch (java.lang.ArrayIndexOutOfBoundsException ex) {
            sendMessage(update, "Следует указать кого вы ищите.\nПример работы: /search урбанист");
        }

        gaAnswer ga = GoogleApiClient.getAllUser();
        Set<List<String>> searchResult = search(ga, text);

        if (searchResult.size() == 0) {
            sendMessage(update, "Совпадений нет");
            return;
        } else if (searchResult.size() > 1) {
            sendMessage(update, "Количество совпадений: " + searchResult.size() + "\nИспользуйте другой запрос");
            return;
        }

        List<String> resultUser = searchResult.iterator().next();
        log.error("resultUser: " + resultUser.toString());

        String v0 = resultUser.get(0);
        String v1 = resultUser.get(1);
        String v2 = resultUser.get(2);
        String v3 = resultUser.get(3);
        String v4 = resultUser.get(4);
        String v5 = resultUser.get(5);
        String v6 = resultUser.get(6);
        String v7 = resultUser.get(7);
        String v8 = resultUser.get(8);
        String v9 = resultUser.get(9);
        String v10 = resultUser.get(10);
        String v11 = resultUser.get(11);

        StringBuffer sb = new StringBuffer();
        try {
            sb.append(v0);
            sb.append("\nВсего взносов: " + v3);
            sb.append("\nНа сумму: " + v4);
            sb.append("\nВсего займов: " + v5);
            if (!"".equals(v7)) {
                sb.append("\nСейчас должен: " + v6);
                sb.append("\nДата возврата: " + v7);
            }
            sb.append("\nДосрочных погашений: " + ("".equals(v8) ? "нет" : v8));
            sb.append("\nПросрочек: " + ("".equals(v9) ? "нет" : v9));
            sb.append("\nВзносы за прошедшие 3 месяца: " + getBooleanValueFromGAPI(v10));
            sb.append("\nВзносы за текущий месяц: " + getBooleanValueFromGAPI(v11));
        } catch (Exception e) {
            e.printStackTrace();
        }

        sendMessage(update, sb.toString());
    }

    private String getBooleanValueFromGAPI(String value) {
        if ("0".equals(value) || "".equals(value))
            return "уплачено";
        return "не уплачено";
    }

    private Set<List<String>> search(gaAnswer ga, String text) {
        Set<List<String>> result = new HashSet<>();

        for (List<String> userIterator : ga.getValues()) {
            for (String userVariable : userIterator) {
                if ((userVariable + "").toLowerCase().indexOf(text) != -1) {
                    result.add(userIterator);
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
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
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