package ru.spb.lanton.soft.exchange.exchangesecurityanalysis.data;

/**
 * Класс для работы с БД адресов и стран.
 * @author antoxa
 */
public interface DataAccessLayer {
    
    /**
     * Добавляет данные о ip адресе и стране в базу данных.
     * @param address
     * @param country 
     */
    void addIpAndCountry(String address, String country);
    
    /**
     * Возвращает ip адрес страны.
     * @param country
     * @return 
     */
    String getIp(String country);
    
    /**
     * Возвращает страну по ip адресу.
     * @param ip
     * @return Название страны или NULL, если не найдена.
     */
    String getCountry(String ip);
    
}
