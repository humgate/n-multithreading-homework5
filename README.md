Репозиторий содержит решение Домашнее задание к занятию 1.5 
Клиент-серверное взаимодействие. Blocking и Non-Blocking IO. 

Решение оформлено в виде многомодульного проекта Maven.

Модуль `complicated-calc` содержит решение для Задача 1. Тяжелые вычисления. Реализация 
использует потоки ввода-вывода из java.io и является логически блокирующим I/O. 
Такая реализация выбрана исходя из того, что по условиям задачи клиенту ничего не нужно 
делать, пока идет обмен данными и выполнение сервером обработки. Клиенту нужен конечный результат.
Соответственно не следует использовать более сложные и более навороченные средства java.nio 

Модуль `space-removal` содержит решение для Задача 2. Долой пробелы. Реализация
использует потоки каналы и буферы из java.nio но логически является все равно блокирующим I/O. Детальные 
пояснения почему так, в комментариях в коде клиента `space-removal/NioClient.java`

Модуль `nio-pipe` содержит дополнительный пример, который реализует полностью и логически 
и технически неблокирующий I/O. Причем эта его "неблокируемость" очень наглядно видна в результатах
вывода System.out как на серверной стороне, так и на клиентской. 
Суть реализованной задачи в следующем: Есть клиент и сервер. Клиент только передает данные на сервер.
Сервер только принимает данные от клиента и отображает. При этом размер строки данных очень 
большой - около 10МБайт. В процессе выполнения кода очень наглядно видна суть non-blocking I/O, а именно то, что
обеспечивается возможность получить для какого-либо использования часть уже переданных данных, до полного завершения
передачи. Детальные пояснения приведены в комментариях в коде сервера и клиента. 
Большой текстовый файл для пример так же включен в репозиторий. 
Мне лично, работа именно по такой задаче помогла разобраться в тонкостях non-blocking I/O 
