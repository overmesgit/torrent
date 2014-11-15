Реализация BitTorrent протокола
Передача всех данных в текстовом формате в кодировке utf-8
Все передаваемые данные строки

далее hash - число, получаемое после вызова хэширующей функции


хэширующая функция :
значение hash - стандартный hash от всей строки
значение sub_data_hash - данные делятся на десят частей, длинна каждой части full_len/10, для каждой части вычисляется стандартный hash

Для получения информации клиенту необходимо получить каким либо образом торрент файл вида:
hash:sub_data_hash_1: ……. :sub_data_hash_n

Загрузчик данных(downloader):
подключается к заданному серверу, посылая сообщение “get\n”
получает с сервера список пиров в виде (nip:port\nip:port\n….ip:port\nstop\n)
опрашивает всех пиров на наличие данных с хэшем
	запрос (hash\n)
	ответ (hash:true\n) or (hash:false\n)
запрашивает у пиров часть данных по хэшу
	(hash:sub_hash_of_data\n)
плучает данные
	(hash:sub_hash_of_data:ДАННЫЕ)

Раздатчик данных(peer):
Регистрирутеся на сервере, посылая сообщение “register:listen_port\n”
слушает входящие сообщения
получает хэш данных(hash\n) и отвечает об их наличии (hash:true\n) or (hash:false\n)
отдает данные по определенному хэшу (hash:sub_data_hash\n)

Это позволит вам запустить два клиента и раздать самому себе какие-нибудь данные.

Сервер:
слушает подключения
запоминает подключившихся пиров
отдает список пиров(ip:port\n)



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
