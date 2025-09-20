package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.service.ReminderService;
import pro.sky.telegrambot.service.TelegramBotService;

import javax.annotation.PostConstruct;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    @Autowired
    private TelegramBot telegramBot;

    @Autowired
    private ReminderService reminderService;

    @Autowired
    private TelegramBotService telegramBotService;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
        logger.info("Telegram bot updates listener initialized");
    }

    @Override
    public int process(List<Update> updates) {
        logger.info("Processing {} updates", updates.size());

        updates.forEach(update -> {
            logger.info("Processing update: {}", update);

            if (update.message() != null && update.message().text() != null) {
                Long chatId = update.message().chat().id();
                String text = update.message().text();

                try {
                    switch (text) {
                        case "/start":
                            handleStartCommand(chatId);
                            break;
                        case "/my_reminders":
                            handleMyRemindersCommand(chatId);
                            break;
                        case "/help":
                            handleHelpCommand(chatId);
                            break;
                        default:
                            handleReminder(chatId, text);
                    }
                } catch (Exception e) {
                    logger.error("Error processing update: {}", update, e);
                    telegramBotService.sendMessage(chatId, "❌ Произошла ошибка при обработке вашего запроса.");
                }
            }
        });

        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    private void handleStartCommand(Long chatId) {
        logger.info("Handling /start command for chatId: {}", chatId);

        String welcomeMessage = "👋 Привет! Я бот для напоминаний.\n\n" +
                                "📋 Доступные команды:\n" +
                                "/start - начать работу\n" +
                                "/my_reminders - мои напоминания\n" +
                                "/help - помощь\n\n" +
                                "📝 Отправь мне напоминание в формате:\n" +
                                "01.01.2022 20:00 Сделать домашнюю работу\n\n" +
                                "⏰ Я пришлю тебе сообщение в указанное время!";

        telegramBotService.sendMessage(chatId, welcomeMessage);
    }

    private void handleHelpCommand(Long chatId) {
        logger.info("Handling /help command for chatId: {}", chatId);

        String helpMessage = "❓ Помощь по использованию бота:\n\n" +
                             "📝 Формат напоминания:\n" +
                             "ДД.ММ.ГГГГ ЧЧ:MM Текст напоминания\n\n" +
                             "📋 Примеры:\n" +
                             "• 25.12.2023 20:00 Поздравить с Рождеством\n" +
                             "• 01.01.2024 00:00 С Новым годом!\n" +
                             "• 15.01.2024 09:30 Совещание с командой\n\n" +
                             "⚡ Команды:\n" +
                             "/start - начать работу\n" +
                             "/my_reminders - посмотреть активные напоминания\n" +
                             "/help - показать эту справку";

        telegramBotService.sendMessage(chatId, helpMessage);
    }

    private void handleMyRemindersCommand(Long chatId) {
        logger.info("Handling /my_reminders command for chatId: {}", chatId);

        List<pro.sky.telegrambot.model.Reminder> reminders = reminderService.getUserReminders(chatId);

        if (reminders.isEmpty()) {
            telegramBotService.sendMessage(chatId, "📭 У вас нет активных напоминаний.");
            return;
        }

        StringBuilder message = new StringBuilder("📋 Ваши активные напоминания:\n\n");
        for (int i = 0; i < reminders.size(); i++) {
            pro.sky.telegrambot.model.Reminder reminder = reminders.get(i);
            message.append(i + 1)
                    .append(". ")
                    .append(reminder.getReminderDate().format(FORMATTER))
                    .append(" - ")
                    .append(reminder.getReminderText())
                    .append("\n");
        }

        telegramBotService.sendMessage(chatId, message.toString());
    }

    private void handleReminder(Long chatId, String text) {
        logger.info("Handling reminder for chatId: {}, text: {}", chatId, text);

        boolean success = reminderService.parseAndSaveReminder(chatId, text);

        if (success) {
            telegramBotService.sendMessage(chatId, "✅ Напоминание успешно добавлено!");
        } else {
            String errorMessage = "❌ Неверный формат. Используйте:\n" +
                                  "ДД.ММ.ГГГГ ЧЧ:MM Текст напоминания\n\n" +
                                  "📋 Пример:\n" +
                                  "01.01.2022 20:00 Сделать домашнюю работу\n\n" +
                                  "⚠ Убедитесь, что:\n" +
                                  "• Дата и время указаны корректно\n" +
                                  "• Дата не в прошлом\n" +
                                  "• Время указано в 24-часовом формате";
            telegramBotService.sendMessage(chatId, errorMessage);
        }
    }
}