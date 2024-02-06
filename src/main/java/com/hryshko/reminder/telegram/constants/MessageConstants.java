package com.hryshko.reminder.telegram.constants;

public class MessageConstants {
    public final static String START_MESSAGE = "Привіт!\nДля того щоб скористатися послугами цього боту,\nнеобхідно пройти процес реєстрації";
    public final static String START_MESSAGE_REG = "Привіт!\nТи вже авторизувався як користвувач цього боту. \n\nТож для того щоб скористатися послугами цього боту,обери потрібну команду в меню";

    //
    public final static String REG_ENTER_NAME = "Введи своє ім'я";
    public final static String REG_ENTER_PHONE_NUMBER = "Надішли нам свій номер телефону";
    public final static String REG_FINISHED = "Вітаю! Реєстрацію успішно завершенно!!!";
    public final static String REG_PHONE_ERROR = "Вибач! Ти надіслав щось не те!";

    //

    public final static String REMINDER_REGISTRATION = "Процес створення нагадування розпочато!";
    public final static String REMINDER_ENTER_TEXT = "Введи освновний текст нагадування";
    public final static String REMINDER_ENTER_DATA = "Надішли дату коли необдіно сповістити тебе у форматі \"ДД-ММ-РРРР\"";
    public final static String REMINDER_ENTER_TIME = "Надішли годину о котрій необдіно сповістити тебе у форматі \"ГГ:ХХ\"";
    public final static String REMINDER_PRE_INFO = "Інформація про нагадування:";
    public final static String REMINDER_INFO = "<b>Текст нагадування:</b>\n %s \n\n<b>Час нагадування:</b> %s o %s";
    public final static String GET_ALL_REMINDERS = "Ось ваш список усіх актуальних нагадувань!";
    public final static String EMPTY_LIST_OF_REMINDERS = "Спершу додай хоча б одине нагадування \uD83D\uDE42";

}
