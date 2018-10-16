package ru.spb.lanton.soft.exchange.exchangesecurityanalysis.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Класс для работы с файловой БД стран и ip адресов.
 *
 * @author antoxa
 */
public class FileDB implements DataAccessLayer, Runnable {

    private enum PARAM {
        IP, COUNTRY
    }

    private File file = new File("ip.db");

    @Override
    public void addIpAndCountry(String ip, String country) {
        String str;
        try (FileWriter writer = new FileWriter(file, true)) {
            str = ip + "\t" + country + "\n";
            writer.write(str);
        } catch (IOException ex) {
            System.err.println("ошибка записи в файл базы данный ip и стран!");
        }
    }

    @Override
    public String getIp(String country) {
        return parser(PARAM.IP, country);
    }

    @Override
    public String getCountry(String ip) {
        return parser(PARAM.COUNTRY, ip);
    }

    /**
     * Парсим файл и возвращаем нужный параметр.
     *
     * @return страна или ip адрес.
     */
    private String parser(PARAM param1, String param2) {
        String find = null;
        String str = null;
        String[] arr = null;
        String ip = null;
        String country = null;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            while ((str = reader.readLine()) != null) {
                arr = str.split("\t");
                ip = arr[0];
                country = arr[1];
                switch (param1) {
                    case IP:
                        if (country.equals(param2)) {
                            // надо подумать что делать с возвращением ip по стране, т.к. на одну страну много ip. Какой именно ip вернет: первый найденный.
                            find = ip;
                        }
                        break;
                    case COUNTRY:
                        if (ip.equals(param2)) {
                            find = country;
                        }
                        break;
                }
            }
        } catch (IOException ex) {
            System.err.println("Ошибка при чтении файла БД.");
            return null;
        }
        return find;
    }

    @Override
    public void run() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
