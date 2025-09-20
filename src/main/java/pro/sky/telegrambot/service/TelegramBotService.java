package pro.sky.telegrambot.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.listener.TelegramBotUpdatesListener;

import javax.annotation.PostConstruct;

@Service
public class TelegramBotService {

    @Value("${bot.token}")
    private String botToken;

    private TelegramBot bot;

    @Autowired
    private TelegramBotUpdatesListener updatesListener;

    @PostConstruct
    public void init() {
        bot = new TelegramBot(botToken);

        bot.setUpdatesListener(updates -> {
            int processedUpdates = updatesListener.process(updates);
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    public void sendMessage(Long chatId, String text) {
        SendMessage request = new SendMessage(chatId, text);
        SendResponse response = bot.execute(request);

        if (!response.isOk()) {
            System.err.println("Ошибка отправки сообщения: " + response.description());
        }
    }
}
