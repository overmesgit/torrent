Учебная реализация подобия BitTorrent протокола
Принципы работы взяты с https://ru.wikipedia.org/wiki/BitTorrent_(%D0%BF%D1%80%D0%BE%D1%82%D0%BE%D0%BA%D0%BE%D0%BB)

Как это работает у меня:
http://youtu.be/jzOz2ZZCGJU

Передача всех данных в текстовом формате в кодировке utf-8.
Все передаваемые данные строки.

Сервер находится по адресу 185.53.130.222:50505
Попробуйте закачать данные по этому торрент файлу:
-1046146689:-1299459300:965287967:1026612457:-1757314556:151251897:-1716523059:741387148:-1491044468:407405590:-872008613

Все работает через жопу, так что все может упасть

Существуе 4 основные сущности:

* 1. Server (регистрирует Peer, хранит их список и передает по запросу Downloader'ам)
* 2. Peer (создает Torrent file, регистрируется на сервере и производит раздачу данных)
* 3. Downloader (получает Torrent file, подключается к серверу и получает данные с Peer'ов)
* 4. Torrent file (хэшированное представление данных)

далее и везде hash - число, получаемое после вызова хэширующей функции

**Torrent file:**

Строка вида:
hash:sub_data_hash_1: ……. :sub_data_hash_n
полученная с помощью хэширующей функции, стандартная реализация будте предоставлена.

Пример:
2081709300:-995075484:-1822253299:394351321:-1338556733:484850113:427097428:184964084:-1048190922:1028727964

Спецификации хэширующей функции:
значение hash - стандартный hash от всей строки
значение sub_data_hash - данные делятся на десят частей, длинна каждой части Math.ceil(full_len/10)
для каждой части вычисляется стандартный String hash


**Downloader:**
подключается к заданному серверу и порту, посылая сообщение “get\n”
получает с сервера список пиров в виде (ip:port\nip:port\n….ip:port\nstop\n)
опрашивает всех пиров на наличие данных с хэшем
	запрос (hash\n)
	получает ответ (hash:true\n) or (hash:false\n)
запрашивает у пиров часть данных по хэшу
	запрос (hash:sub_hash_of_data\n)
    ответ (hash:sub_hash_of_data:ДАННЫЕ\nstop\n) or (hash:false\n)

**Peer:**
Регистрируется на сервере, посылая сообщение с порта на котором будут приниматься клиенты “register\n”
слушает входящие сообщения
получает хэш данных(hash\n) и отвечает об их наличии (hash:true\n) or (hash:false\n)
отдает данные по определенному хэшу (hash:sub_data_hash\nstop\n)

Это позволит вам запустить два клиента и раздать самому себе какие-нибудь данные.

**Server**
слушает подключения
если запрос (register\n), то запоминает подключившийся пиров
отдает список пиров ip:port\nstop\n



Спецификация хэш функции:

    public ArrayList<String> splitDataToPeaces(String s) {
        int peacesCount = 10;
        ArrayList<String> result = new ArrayList<String>();
        int step = (int)Math.round(Math.ceil((float)s.length()/peacesCount));
        for (int currentPeaceIndex = 0; currentPeaceIndex < peacesCount; currentPeaceIndex += 1) {
            int from = step * currentPeaceIndex;
            if (from > s.length()) {
                break;
            }
            int to = step * (currentPeaceIndex + 1) > s.length() ? s.length() : step * (currentPeaceIndex + 1);
            String currentPeace = s.substring(from, to);
            result.add(currentPeace);
        }
        return result;
    }

    public HashMap<Integer, String> getHashMap(String s) {
        int peacesCount = 10;
        HashMap<Integer, String> result = new HashMap<Integer, String>();
        ArrayList<String> splitData = splitDataToPeaces(s);
        for (String peace: splitData) {
            result.put(peace.hashCode(), peace);
        }
        return result;
    }

    public String getTorrentFile(String s) {
        StringBuilder result = new StringBuilder();
        ArrayList<String> splitData = splitDataToPeaces(s);

        result.append(String.format("%s:", s.hashCode()));
        for (String peace: splitData) {
            result.append(String.format("%s:", peace.hashCode()));
        }
        return result.toString().substring(0, result.length() - 1);
    }
