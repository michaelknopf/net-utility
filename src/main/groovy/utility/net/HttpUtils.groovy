package utility.net

class HttpUtils {

    static Map<String, ?> queryStringToMap(String queryString, boolean unique = true) {
        Map queryMap = [:]
        for (String kvPair : queryString.split('&')) {
            kvPair.split('=').with{
                // get key
                String k = it[0]
                if (unique) {
                    queryMap[k] = it[1]
                } else {
                    if (!(k in queryMap)) {
                        queryMap[k] = []
                    }
                    // append value
                    queryMap[k] << it[1]
                }
            }
        }
        return queryMap
    }

}
