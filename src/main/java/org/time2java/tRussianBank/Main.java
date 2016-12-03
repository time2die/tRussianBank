package org.time2java.tRussianBank;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;
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
        new TelegramBotsApi().registerBot(new RussianBot());
    }
}