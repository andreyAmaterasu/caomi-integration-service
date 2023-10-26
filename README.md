# caomi-integration-service

Сервис для взаимодействия МИС с ЦАМИ через шину caomi-fhir

### Используемое ПО
- Java 17
- Spring boot 2.6.6
- MSSQL-jdbc 9.4.1.jre11

## Сборка
```sh
mvn clean install 
```

### Описание сервиса
Сервис **caomi-integration-service** это сервис-scheduler (выполнение работ по расписанию). \
Сервис имеет следующие работы:
- _addDeviceJob_ \
Поиск данных оборудования в БД для отправки в ЦАМИ на метод POST AddDevice \

### Описание атрбиутов конфигурации (application.yml)
Настройки job сервиса: \
<span style="color:green;">jobs.addDeviceJob.cron</span> - cron addDeviceJob работы сервиса (например: '*/10 * * * * *' - раз в 10 сек.)

Общие настройки для работы сервиса: \
<span style="color:green;">app.caomi.url</span> - url до внешней системы (url до сервиса ЦАМИ). \
<span style="color:green;">app.sql.deviceSql</span> - sql за получением данных 'Device' из БД \
<span style="color:green;">app.sql.offset</span> -  offset для deviceSql
<span style="color:green;">app.sql.limit</span> - limit для deviceSql

Настройки подключения к БД: \
<span style="color:green;">database.servername</span> - адрес сервера БД \
<span style="color:green;">database.database</span> - база данных сервера \
<span style="color:green;">database.username</span> - имя пользователя БД \
<span style="color:green;">database.password</span> - пароль пользователя БД

----

### Алгоритм работы addDeviceJob
Алгоритм работы: \
1 - Проверка присутствия 'device sql запроса' в конфигурации application.yml
Если - app.sql.deviceSql заполнены, то продолжить работу. \
Иначе - сообщить в логе об отсутствии sql-запроса, закончить Job. \
2 - Поиск данных Device в БД по sql-запросу из конфига (app.sql.deviceSql)
с учетом offset (app.sql.offset) и limit (app.sql.limit). \
Если данные найдены, продолжить. \
Иначе - закончить работу Job
Важно: (_Если limit = 0, сервис автоматически поставит значение 2147483647 т.к. он не может
быть 0_). \
3 - Сбор Json для отправки в ЦАМИ

|              Поле запроса в РИМС               |         Значение заполнения          
|:----------------------------------------------:|:------------------------------------:|
|                  deviceMisId                   |          Device.deviceMisId          |
|                    isActive                    |       Device.is_active_device        |
|                   deviceName                   |          Device.deviceName           |
|                     owner                      |             Device.owner             |
| service (заполняется только 1 элемент массива) |                                      |
|                  serviceCode                   |          Device.serviceCode          |
|                  serviceName                   |          Device.serviceName          |
|                    isActive                    |        Device.is_active_service      |

Пример Json:
```json
{
  "deviceMisId": "111",
  "isActive": false,
  "deviceName": "Оборудование для УЗИ",
  "owner": "owner",
  "service": [
    {
      "serviceCode": "serviceCode",
      "serviceName" : "serviceName",
      "isActive": true
    }
  ]
}
```

4 - Отправка и получение ответа от ЦАМИ \
Пример ответа: \

```json
{
  "errorCode": "0",
  "errorText": "errorText",
  "id": 1,
  "dateTime": "2021-06-24T16:00:00Z",
  "idReferral": "guid"
}
```

5 - Обновление записи в таблице hst_caomiDevice. \
Поиск записи осуществляется по Device.caomiDeviceID = hst_caomiDevice.caomiDeviceID

| hst_caomiDevice | Значение заполнения из ответа ЦАМИ 
|:---------------:|:----------------------------------:|
|     caomiID     |                 id                 |
|    errorCode    |             errorCode              |
|    errorText    |             errorText              |
|   dateStatus    |          Тек. дата-время           |
|   httpStatus    |    httpStatus пришедшего ответа    |
|    statusID     |                 1                  |


