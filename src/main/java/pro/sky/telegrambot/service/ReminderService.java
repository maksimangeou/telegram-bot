package pro.sky.telegrambot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.Reminder;
import pro.sky.telegrambot.repository.ReminderRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ReminderService {

    @Autowired
    private ReminderRepository reminderRepository;

    @Autowired
    private TelegramBotService telegramBotService;

    private static final Pattern REMINDER_PATTERN =
            Pattern.compile("(\\d{2}\\.\\d{2}\\.\\d{4} \\d{2}:\\d{2}) (.+)");

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public boolean parseAndSaveReminder(Long chatId, String text) {
        Matcher matcher = REMINDER_PATTERN.matcher(text);
        if (matcher.matches()) {
            String dateTimeStr = matcher.group(1);
            String reminderText = matcher.group(2);

            try {
                LocalDateTime reminderDate = LocalDateTime.parse(dateTimeStr, DATE_FORMATTER);

                // Проверка что дата в будущем
                if (reminderDate.isBefore(LocalDateTime.now())) {
                    return false;
                }

                Reminder reminder = new Reminder(chatId, reminderText, reminderDate);
                reminderRepository.save(reminder);
                return true;
            } catch (DateTimeParseException e) {
                return false;
            }
        }
        return false;
    }

    @Scheduled(fixedRate = 30000) // Проверка каждые 30 секунд
    public void checkAndSendReminders() {
        LocalDateTime now = LocalDateTime.now();
        List<Reminder> dueReminders = reminderRepository.findDueReminders(now);

        for (Reminder reminder : dueReminders) {
            String message = "⏰ Напоминание: " + reminder.getReminderText();
            telegramBotService.sendMessage(reminder.getChatId(), message);
            reminder.setIsSent(true);
            reminderRepository.save(reminder);
        }
    }

    public List<Reminder> getUserReminders(Long chatId) {
        return reminderRepository.findActiveRemindersByChatId(chatId);
    }
}