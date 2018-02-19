package ru.spb.lanton.soft.exchange.exchangesecurityanalysis.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import ru.spb.lanton.soft.exchange.exchangesecurityanalysis.view.WindowMainController;

public class Parser extends AsyncTask {

    private Output choiceOutput;

    private final List<Terror> terrorListAll;

    private final Set<Terror> terrorListUnicumIp;

    private final Set<Terror> terrorListUnicumName;

    private final WindowMainController controller;

    private LocalDate date;

    private boolean fullLog;

    private final List<String> fullLogList;

    private final ResourceBundle rb;

    /**
     * Дефолтный конструктор.
     *
     * @param controller ссылка на контроллер.
     */
    public Parser(WindowMainController controller) {
        this.controller = controller;
        terrorListAll = new ArrayList<>();
        terrorListUnicumIp = new HashSet<>();
        terrorListUnicumName = new HashSet<>();
        fullLog = false;
        fullLogList = new ArrayList<>();
        rb = ResourceBundle.getBundle("settings");
    }

    /**
     * Установка даты отчета.
     *
     * @param date дата.
     */
    public void setDate(LocalDate date) {
        this.date = date;
    }

    /**
     * Устанавливает значение ведения полного лога.
     *
     * @param value TRUE - вести полный лог. FALSE - не вести полный лог.
     */
    public void setFullLog(boolean value) {
        fullLog = value;
    }

    /**
     * Получение списка файлов, парсинг логов
     */
    public void prepareData() {
        choiceOutput = Output.CONSOLE;
        terrorListAll.clear();
        terrorListUnicumIp.clear();
        terrorListUnicumName.clear();
        fullLogList.clear();
        publishProgress("Статус: подготовка данных...", Output.STATUS);
        String pathToSmtpLogs = rb.getString("pathToSmtpLogs");
        String pathToImapLogs = rb.getString("pathToImapLogs");
        String pathToHttpLogs = rb.getString("pathToHttpLogs");

        List<String> listPathToFiles = new ArrayList<>();
        listPathToFiles.add(pathToSmtpLogs);
        listPathToFiles.add(pathToImapLogs);
        listPathToFiles.add(pathToHttpLogs);

        int countFiles = 0;
        for (String pathToFiles : listPathToFiles) {
            File dir = new File(pathToFiles);
            File[] arrFiles = dir.listFiles();
            if (arrFiles != null) {
                List<File> files = Arrays.asList(arrFiles);
                String year = Integer.toString(date.getYear());
                year = year.substring(2);
                String month = date.getMonthValue() < 10 ? ("0" + date.getMonthValue()) : Integer.toString(date.getMonthValue());
                String day = date.getDayOfMonth() < 10 ? ("0" + date.getDayOfMonth()) : Integer.toString(date.getDayOfMonth());
                String patternDate = year + month + day;
                for (File file : files) {
                    if (file.isFile()) {
                        if (file.getName().contains(patternDate)) {
                            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
                                output("Чтение файла: " + file.getName());
                                int count = 0;
                                String s;
                                String pattern = "Denied";
                                String patternHttp = "302 0 0";
                                while ((s = reader.readLine()) != null) {
                                    if (s.contains(pattern) || s.contains(patternHttp)) {
                                        if (file.getName().contains("RECV")) {
                                            publishProgress("Чтение данных из лог файла SMTP...", Output.STATUS);
                                            if (findUserNameAndIpSmtp(reader.readLine())) {
                                                count++;
                                            }
                                        }
                                        if (file.getName().contains("IMAP")) {
                                            publishProgress("Чтение данных из лог файла IMAP...", Output.STATUS);
                                            if (findUserNameAndIpImap(s)) {
                                                count++;
                                            }
                                        }
                                        if (file.getName().contains("u_ex")) {
                                            publishProgress("Чтение данных из лог файла HTTP...", Output.STATUS);
                                            if (findUserNameAndIpHttp(s)) {
                                                count++;
                                            }
                                        }
                                    }
                                }
                                if (0 == count) {
                                    output("Совпадений не найдено.");
                                } else {
                                    output("Найдено " + count + " совпадений!");
                                }
                            } catch (IOException ex) {
                                System.err.println("IO Exception!");
                            }
                            countFiles++;
                        }
                    }
                }
            }
        }
        if (0 == countFiles) {
            output("Файлов за указанную дату не найдено!");
        } else {
            // подготовить данные об уникальных атаках
            fillIpCounty();
            publishProgress("Подготавливаются списки данных о пользователях и IP...", Output.STATUS);
            prepareUnicumTerrorIpList();
            prepareUnicumTerrorUsernameList();
            controller.enabledButtonPrintToScree();
            controller.enabledButtonPrintToFile();
        }

        Date dateNow = new Date();
        SimpleDateFormat formateToTime = new SimpleDateFormat("hh:mm:ss");
        publishProgress("Статус: данные подготовлены на " + formateToTime.format(dateNow), Output.STATUS);
    }

    /**
     * Получаем страны для IP адресов.
     */
    private void fillIpCounty() {
        int i = 1;
        int total = terrorListAll.size();
        for (Terror terror : terrorListAll) {
            publishProgress("Получаем данные о местоположении IP адресов: " + i + " из " + total, Output.STATUS);
            terror.setIpCountry(getCountry(terror.getIp()));
            i++;
        }
    }

    /**
     * Получить страну для адреса.
     */
    private String getCountry(String address) {
        String jsonAnswer = null;
        String country = null;
        String site = "http://freegeoip.net/json/" + address;
        URL url = null;
        try {
            url = new URL(site);
        } catch (MalformedURLException ex) {
            Logger.getLogger(Parser.class.getName()).log(Level.SEVERE, null, ex);
        }
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
        } catch (IOException ex) {
            Logger.getLogger(Parser.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            connection.setRequestMethod("GET");
        } catch (ProtocolException ex) {
            Logger.getLogger(Parser.class.getName()).log(Level.SEVERE, null, ex);
        }
        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            jsonAnswer = response.toString();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        JSONParser parser = new JSONParser();
        try {
            JSONObject object = (JSONObject) parser.parse(jsonAnswer);
            country = (String) object.get("country_name");
        } catch (ParseException ex) {
            Logger.getLogger(Parser.class.getName()).log(Level.SEVERE, null, ex);
        }
        return country;
    }

    /**
     * Подготовка уникального список IP адресов, с которых были попытки
     * неудачной авторизации.
     */
    private void prepareUnicumTerrorIpList() {
        boolean needAdd;
        for (Terror terror : terrorListAll) {
            needAdd = true;
            for (Terror terrorUnicum : terrorListUnicumIp) {
                if (terrorUnicum.getIp().equals(terror.getIp())) {
                    terrorUnicum.addCountAttackIp();
                    needAdd = false;
                    break;
                }
            }
            if (needAdd) {
                terrorListUnicumIp.add(terror);
            }
        }
    }

    /**
     * Подготовка уникального списка пользователей, к которым были попытки
     * неудачной авторизации.
     */
    private void prepareUnicumTerrorUsernameList() {
        boolean needAdd;
        for (Terror terror : terrorListAll) {
            needAdd = true;
            for (Terror terrorUnicum : terrorListUnicumName) {
                if (terrorUnicum.getUserName().equals(terror.getUserName())) {
                    terrorUnicum.addCountAttackUsername();
                    needAdd = false;
                    break;
                }
            }
            if (needAdd) {
                terrorListUnicumName.add(terror);
            }
        }
    }

    /**
     * Парсинг строки лога SMTP
     *
     * @param s строка для парсинга
     */
    private boolean findUserNameAndIpSmtp(String s) {
        boolean result;
        if (fullLog) {
            fullLogList.add(s);
        }
        String pattern = "^(\\d{4}-\\d{2}-\\d{2}\\w\\d{2}:\\d{2}:\\d{2}).+\\d{3}\\D([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}).*\\s([a-zA-Z0-9\\.\\@]{2,})";;
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(s);
        String date = null;
        String ip = null;
        String userName = null;
        String propocol = "SMTP";
        if (m.find()) {
            date = m.group(1);
            ip = m.group(2);
            userName = m.group(3);
            terrorListAll.add(new Terror(userName, ip, date, propocol));
            result = true;
        } else {
            result = false;
        }
        return result;
    }

    /**
     * Парсинг строки лога IMAP
     *
     * @param s строка для парсинга
     */
    private boolean findUserNameAndIpImap(String s) {
        boolean result;
        if (fullLog) {
            fullLogList.add(s);
        }
        String pattern = "^(\\d{4}-\\d{2}-\\d{2}\\w\\d{2}:\\d{2}:\\d{2}).+(?:[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}).\\d{3}.([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}):\\d{3,5}\\,([a-zA-Z0-9\\.\\@\\_\\-]{2,}).\\d";
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(s);
        String date = null;
        String userName = null;
        String ip = null;
        String propocol = "IMAP";
        if (m.find()) {
            date = m.group(1);
            ip = m.group(2);
            userName = m.group(3);
            terrorListAll.add(new Terror(userName, ip, date, propocol));
            result = true;
        } else {
            result = false;
        }
        return result;
    }

    /**
     * Парсинг строки лога HTTP
     *
     * @param s строка для парсинга
     */
    private boolean findUserNameAndIpHttp(String s) {
        boolean result;
        if (fullLog) {
            fullLogList.add(s);
        }
        String pattern = "^(\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2}).+\\s((?:[a-zA-Z0-9._]{1,20}\\@\\w{2,20}\\.\\w{2,5})|(?:\\w{2,20}\\\\[a-zA-Z0-9._]{1,20}))\\s([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}).+(302\\ 0\\ 0)";
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(s);
        String date = null;
        String userName = null;
        String ip = null;
        String propocol = "HTTP";
        if (m.find()) {
            date = m.group(1);
            userName = m.group(2);
            ip = m.group(3);
            String mask = rb.getString("mask");
            if (!ip.contains(mask)) {
                terrorListAll.add(new Terror(userName, ip, date, propocol));
                result = true;
            } else {
                result = false;
            }
        } else {
            result = false;
        }
        return result;
    }

    /**
     * Вывести полный отчет о неудачных попытках авторизации.
     */
    private void allData() {
        for (Terror terror : terrorListAll) {
            output(terror.getDate() + "  IP: " + terror.getIp() + "   User name: " + terror.getUserName() + "   Протокол: " + terror.getProtocol());
        }
    }

    /**
     * Вывести сокращенный отчет о неудачных попытках авторизации. Отчет
     * содержит адреса ящиков и IP ареса, с которых пытались авторизоваться, с
     * указанием количества попыток подключения.
     */
    private void shortReport() {
        List<Terror> sortedList = new ArrayList<>(terrorListUnicumIp);
        Collections.sort(sortedList, new Comparator<Terror>() {
            @Override
            public int compare(Terror o1, Terror o2) {
                return o1.getIpCountry().compareTo(o2.getIpCountry());
            }
        });
        for (Terror terror : sortedList) {
            output("IP:" + terror.getIp() + " - " + terror.getIpCountry() + " - " + terror.getCountAttackIp() + " раз.");
        }

        sortedList.clear();
        sortedList = new ArrayList<>(terrorListUnicumName);
        Collections.sort(sortedList, new Comparator<Terror>() {
            @Override
            public int compare(Terror o1, Terror o2) {
                return o1.getUserName().compareTo(o2.getUserName());
            }
        });
        for (Terror terror : sortedList) {
            output("User name: " + terror.getUserName() + " - " + terror.getCountAttackUsername() + " раз.");
        }
    }

    /**
     * Выводит адрес ящика и список IP адресов, с которых пытались на этот ящик
     * авторизоваться.
     */
    private void emailReport() {
        Map<Terror, List> listReport = new HashMap<>();
        Set<Terror> listIp = new HashSet<>();
        for (Terror email : terrorListUnicumName) {
            for (Terror terror : terrorListAll) {
                if (email.getUserName().equals(terror.getUserName())) {
                    listIp.add(terror);
                }
            }
            listReport.put(email, new ArrayList<Terror>(listIp));
            listIp.clear();
        }
        for (Map.Entry<Terror, List> me : listReport.entrySet()) {
            List<Terror> ipList = me.getValue();
            output("На ящик " + me.getKey().getUserName() + " пытались подключиться " + ipList.size() + " раз, со следующих адресов:");
            for (Terror ip : ipList) {
                output(ip.getDate() + "  IP: " + ip.getIp() + " - " + ip.getIpCountry() + "  Протокол: " + ip.getProtocol());
            }
        }
    }

    /**
     * Полный лог. Список строк содержащих сведения о неудачных попытках
     * авторизации.
     */
    private void fullLog() {
        for (String string : fullLogList) {
            output(string);
        }
    }

    /**
     * В зависимости от параметра VOTE осуществляется вывод данных в файл или в
     * терминал. Данные записываются в конец файла. Если файла не существует, то
     * он создается.
     *
     * @param msg строка вывода
     */
    private void output(String msg) {
        switch (choiceOutput) {
            case CONSOLE:
                //System.out.println(msg);
                publishProgress(msg + "\n", Output.CONSOLE);
                //publishProgress
                break;
            case FILE:
                File file = new File("report.log");
                try {
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    FileWriter fw = new FileWriter(file, true);
                    msg += "\n";
                    fw.write(msg);
                    fw.close();
                } catch (IOException ex) {
                    publishProgress("Ошибка записи в файл!" + "\n", Output.CONSOLE);
                }
                break;
            default:
                System.out.println(msg);
        }
    }

    /**
     *
     */
    public void printReport(Object... params) {
        choiceOutput = (Output) params[1];
        switch ((int) params[0]) {
            case 0:
                emailReport();
                break;
            case 1:
                shortReport();
                break;
            case 2:
                allData();
                break;
            case 3:
                fullLog();
                break;
        }
    }

    @Override
    public void onPreExecute() {

    }

    @Override
    public Object doInBackground(Object... params) {
        prepareData();
        return params;
    }

    @Override
    public void onPostExecute(Object params) {
        controller.enabledButtonPrintToFile();
        controller.enabledButtonPrintToScree();
        controller.enabledChoicer();
    }

    @Override
    public void progressCallback(Object... params) {
        switch ((Output) params[1]) {
            case CONSOLE:
                controller.appendText((String) params[0]);
                break;
            case STATUS:
                controller.setStatus((String) params[0]);
        }
    }

}
