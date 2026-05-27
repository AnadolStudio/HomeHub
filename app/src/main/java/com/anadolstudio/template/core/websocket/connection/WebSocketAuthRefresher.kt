package com.anadolstudio.template.core.websocket.connection

fun interface WebSocketAuthRefresher {

    /**
     * Вызывается при получении `auth_invalid` от сервера.
     * Должен обновить креденшл и вернуть новый access_token,
     * либо null — если refresh не удался (сессия протухла).
     */
    suspend fun refresh(): String?
}
